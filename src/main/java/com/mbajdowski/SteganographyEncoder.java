package com.mbajdowski;


import com.sun.istack.internal.NotNull;
import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class SteganographyEncoder {
    private int bitsFromColor;
    private int mask;
    private BufferedImage bi;

    public SteganographyEncoder(@NotNull BufferedImage bufferedImage) {
        new SteganographyEncoder(bufferedImage, 2);
    }

    public SteganographyEncoder(@NotNull BufferedImage bufferedImage, int bitsFromColor) {
        setBitsFromColor(bitsFromColor);
        this.bi = bufferedImage;
    }

    public BufferedImage encodeString(String message) throws InvalidArgumentException {
        if (message.length() == 0) {
            throw new InvalidArgumentException(new String[]{"Message can not be empty!"});
        }
        char[] characters = message.toCharArray();
        byte[] bytes = new byte[characters.length];
        for (int i = 0; i < characters.length; i++) {
            bytes[i] = (byte) characters[i];
        }

        return encode(bytes);
    }

    public String decodeString() {
        StringBuffer sb = new StringBuffer();
        byte[] decodedByteArray = decode();

        for (int i = 0; i < decodedByteArray.length; i++) {
            char character = (char) decodedByteArray[i];
            if (character < 32 || character > 126) break;
            sb.append(character);
        }

        return sb.toString();
    }

    public BufferedImage encodeFile(File file) throws IOException {
        byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
        byte[] sizeBytes = intToByteArray(bytes.length);

        char[] nameChars = file.getName().toCharArray();
        byte[] nameBytes = new byte[nameChars.length];
        for (int i = 0; i < nameChars.length; i++) {
            nameBytes[i] = (byte) nameChars[i];
        }
        byte[] sizeNameBytes = intToByteArray(nameBytes.length);

        byte[] finalBytes = new byte[4 + 4 + nameBytes.length + bytes.length];
        System.arraycopy(sizeNameBytes, 0, finalBytes, 0, 4);
        System.arraycopy(sizeBytes, 0, finalBytes, 4, 4);
        System.arraycopy(nameBytes, 0, finalBytes, 8, nameBytes.length);
        System.arraycopy(bytes, 0, finalBytes, 8 + nameBytes.length, bytes.length);

        return encode(finalBytes);
    }

    public File decodeFile() throws DecodignException {
        byte[] bytes = decode();
        int nameSize = byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4));
        if(nameSize<=0||nameSize>(bytes.length/4)){
            throw new DecodignException("NameSize", nameSize);
        }
        int fileSize = byteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
        if(fileSize<0||fileSize>(bytes.length/4)){
            throw new DecodignException("DecodedFileSize", fileSize);
        }
        if(nameSize+fileSize>(bytes.length/4)){
            throw new DecodignException("NameSize and DecodedFileSize", nameSize+fileSize);
        }
        byte[] nameBytes = Arrays.copyOfRange(bytes, 8, 8 + nameSize);
        byte[] fileBytes = Arrays.copyOfRange(bytes, 8 + nameSize, 8 + nameSize + fileSize);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameBytes.length; i++) {
            sb.append((char) nameBytes[i]);
        }
        String name = sb.toString();
        File file = new File("decoded_" + name);
        try {
            FileUtils.writeByteArrayToFile(file, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public int getBitsFromColor() {
        return bitsFromColor;
    }

    public void setBitsFromColor(int bitsFromColor) {
        checkBitsFromColor(bitsFromColor);
        this.bitsFromColor = bitsFromColor;
        mask = calculateMask(bitsFromColor);
    }

    public int getMaxNoOfBytes(int[] pixels) {
        return Math.floorDiv(pixels.length * bitsFromColor * 3, 8);
    }

    private BufferedImage encode(byte[] bytes) {
        int[] pixels = this.bi.getRGB(0, 0, this.bi.getWidth(), this.bi.getHeight(), null, 0, this.bi.getWidth());
        int maxNoOfBytes = getMaxNoOfBytes(pixels);
        if (bytes.length > maxNoOfBytes) {
            throw new IllegalArgumentException("File to big, max no of bytes: " + maxNoOfBytes);
        }

        int smallMask = (int) (Math.pow(2, bitsFromColor) - 1);
        int curColor = 2;
        int curPix = 0;
        int charOffset = 0;

        pixels[0] &= mask;
        for (int i = 0; i < bytes.length; i++) {
            while (charOffset < 8) {
                if (curColor < 0) {
                    curColor = 2;
                    curPix++;
                    pixels[curPix] &= mask;
                }

                char temp = (char) ((bytes[i] >> 8 - bitsFromColor - charOffset) & smallMask);
                pixels[curPix] |= (temp << curColor * 8);

                charOffset += bitsFromColor;
                curColor--;
            }
            charOffset %= 8;
        }

        BufferedImage bufferedImage = new BufferedImage(this.bi.getWidth(), this.bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, this.bi.getWidth(), this.bi.getHeight(), pixels, 0, this.bi.getWidth());
        return bufferedImage;
    }

    private byte[] decode() {
        int[] pixels = this.bi.getRGB(0, 0, this.bi.getWidth(), this.bi.getHeight(), null, 0, this.bi.getWidth());
        int maxNoOfBytes = getMaxNoOfBytes(pixels);
        byte[] result = new byte[maxNoOfBytes];
        int smallMask = (int) (Math.pow(2, bitsFromColor) - 1);
        int curColor = 2;
        int curPix = 0;
        int charOffset = 0;

        for (int i = 0; i < maxNoOfBytes; i++) {
            byte oneByte = 0;
            while (charOffset < 8) {
                if (curColor < 0) {
                    curColor = 2;
                    curPix++;
                }
                char temp = (char) (pixels[curPix] >> (8 * curColor) & smallMask);
                oneByte |= temp << 8 - bitsFromColor - charOffset;

                charOffset += bitsFromColor;
                curColor--;
            }
            result[i] = oneByte;
            charOffset %= 8;
        }
        return result;
    }

    private void checkBitsFromColor(int bitsFromColor) {
        if (!Arrays.asList(1, 2, 4, 8).contains(bitsFromColor)) {
            throw new IllegalArgumentException("Number of used bits from color must be in set {1,2,4,8}");
        }
    }

    private int calculateMask(int bitsFromColor) {
        int temp = (int) (Math.pow(2, bitsFromColor) - 1);
        int mask = 0;
        for (int i = 0; i < 3; i++) {
            mask <<= 8;
            mask |= temp;
        }
        return ~mask;
    }

    private byte[] intToByteArray(int integer) {
        byte[] result = new byte[4];
        for (int i = 3; i >= 0; i--) {
            result[3 - i] = (byte) (integer >> (i * 8));
        }
        return result;
    }

    private int byteArrayToInt(byte[] bytes) {
        if (bytes.length != 4) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result <<= 8;
            result |= bytes[i];
        }

        return result;
    }
}
