package com.mbajdowski.encoder;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class SteganographyEncoderTest {

    private static BufferedImage beforeTestImg;
    private static BufferedImage afterStringTestImg;
    private static BufferedImage afterFileTestImg;
    private static BufferedImage toLongNameEncodedImg;
    private static File beforeTestFile;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private SteganographyEncoder steganographyEncoder;

    @BeforeClass
    public static void initialize() throws IOException {
        beforeTestImg =
                ImageIO.read(SteganographyEncoderTest.class.getResource("/com.mbajdowski/encoder/beforeTestImg.png"));
        afterStringTestImg =
                ImageIO.read(
                        SteganographyEncoderTest.class.getResource("/com.mbajdowski/encoder/afterStringTestImg.png"));
        afterFileTestImg =
                ImageIO.read(
                        SteganographyEncoderTest.class.getResource("/com.mbajdowski/encoder/afterFileTestImg.png"));
        toLongNameEncodedImg = ImageIO.read(
                SteganographyEncoderTest.class.getResource("/com.mbajdowski/encoder/fileWithEncodedTooLongName.png"));
        beforeTestFile =
                new File(SteganographyEncoderTest.class.getResource("/com.mbajdowski/encoder/beforeTestFile.txt")
                                 .getFile());
    }

    @Before
    public void setUp() {
        steganographyEncoder = new SteganographyEncoder(beforeTestImg);
    }

    @After
    public void tearDown() {
        new File("src/test/resources/com.mbajdowski/encoder/decoded_beforeTestFile.txt").delete();
    }

    @Test
    public void emptyConstructorShouldSetBitsFromColorToProperValue() {
        int expected = 2;

        int actual = steganographyEncoder.getBitsFromColor();

        assertEquals(expected, actual);
    }

    @Test
    public void encodeStringShouldEncodeValidString() throws InvalidArgumentException {
        String testString = "Test String";
        int[] expectedPixels = afterStringTestImg
                .getRGB(0, 0, afterStringTestImg.getWidth(), afterStringTestImg.getHeight(), null, 0,
                        afterStringTestImg.getWidth());

        BufferedImage result = steganographyEncoder.encodeString(testString);

        int[] resultPixels = result.getRGB(0, 0, result.getWidth(), result.getHeight(), null, 0, result.getWidth());
        assertArrayEquals(expectedPixels, resultPixels);
    }

    @Test
    public void encodeStringShouldThrowExceptionIfMessageTooLong() throws InvalidArgumentException {
        String tooLongString =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer congue nulla a ipsum rhoncus mollis" +
                        ". Curabitur eu varius lacus. Pellentesque finibus nunc eget sapien accumsan efficitur sed in" +
                        " nullam.";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("File to big, max no of bytes: 75");


        steganographyEncoder.encodeString(tooLongString);
    }

    @Test
    public void encodeStringShouldThrowExceptionIfMessageEmpty() throws InvalidArgumentException {
        String emptyString = "";
        expectedEx.expect(InvalidArgumentException.class);
        expectedEx.expectMessage("Message can not be empty!");

        steganographyEncoder.encodeString(emptyString);
    }

    @Test
    public void decodeStringShouldReturnProperStringIfValidImg() {
        String expected = "Test String";
        SteganographyEncoder se = new SteganographyEncoder(afterStringTestImg);

        String result = se.decodeString();

        assertEquals(expected, result);
    }

    @Test
    public void encodeFileShouldEncodeValidFile() throws IOException {
        int[] expectedPixels = afterFileTestImg
                .getRGB(0, 0, afterFileTestImg.getWidth(), afterFileTestImg.getHeight(), null, 0,
                        afterFileTestImg.getWidth());

        BufferedImage result = steganographyEncoder.encodeFile(beforeTestFile);

        int[] resultPixels = result.getRGB(0, 0, result.getWidth(), result.getHeight(), null, 0, result.getWidth());
        assertArrayEquals(expectedPixels, resultPixels);
    }

    @Test
    public void decodeFileShouldReturnProperFileIfValidImgAndHasProperName() throws DecodingException, IOException {
        SteganographyEncoder se = new SteganographyEncoder(afterFileTestImg);
        String path = "src/test/resources/com.mbajdowski/encoder/";

        File resultFile = se.decodeFile(path);

        assertTrue(resultFile.getName().startsWith("decoded_"));
        assertTrue(FileUtils.contentEquals(beforeTestFile, resultFile));
    }

    @Test
    public void decodeFileShouldThrowDecodeExceptionIfEncodedNameTooLong() throws DecodingException {
        SteganographyEncoder se = new SteganographyEncoder(toLongNameEncodedImg);
        String path = "src/test/resources/com.mbajdowski/encoder/";
        expectedEx.expect(DecodingException.class);
        expectedEx.expectMessage("Error while decoding! NameSize has invalid value of: 200");

        se.decodeFile(path);
    }

    @Test
    public void getBitsFromColorShouldReturnProperValue() {
        int expected = 2;

        int actual = steganographyEncoder.getBitsFromColor();

        assertEquals(expected, actual);
    }

    @Test
    public void setBitsFromColorShouldSetUpValueIfValid() {
        int expected = 4;

        steganographyEncoder.setBitsFromColor(expected);

        assertEquals(expected, steganographyEncoder.getBitsFromColor());
    }

    @Test
    public void setBitsFromColorShouldThrowExceptionIfWrongNumberProvided() throws IllegalArgumentException {
        int wrongNumber = 3;
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Number of used bits from color must be in set {1,2,4,8}");

        steganographyEncoder.setBitsFromColor(wrongNumber);
    }

    @Test
    public void getMaxNoOfBytesShouldReturnProperValue() {
        int expected = 75;

        int actual = steganographyEncoder.getMaxNoOfBytes();

        assertEquals(expected, actual);
    }
}