# Data in Image
Data in image is a project which allow you to encode and decode text messages and files inside an image.
Project is divided into two parts:
* Steganography encode - which is the engine for encoding and decoding information from and to image
* Simple UI - which helps to use the steganography engine  

## How to use it
1. Clone repository
2. Build it using maven
3. Run fat jar

## Modes
DataInImage works in four modes:
1. Encode textual message into image (txt->img)
2. Decode textual message from the file (img->txt)
3. Encode file into image (file->img)
4. Decode file from image (img->file)

## Options
1. The user can specify the image to encode to
2. User can specify how many bits per color in pixel wants to dedicate to encode the message
Possible options are powers of two (1, 2, 4, 8). The more bites will be used the bigger change to the original image will be done

## How does it work
Steganography is a practice of hiding information inside image. Follow [THIS](https://en.wikipedia.org/wiki/Steganography) link to find more 

**FLOW:**  
DataInImage is working in the following way:
1. First image is transformed into an array of pixels
2. Every pixel is built from four bytes, from which 3 of the contains information about three colours: RED, GREEN and BLUE
3. To store additional information inside an image we are taking least significant bits from each colour and treat them as a storage place. The change to the colour is too small to be visible to the human eye. 
(DataInImage give possibility to the user to use more than least significant bit to store more data, but this will result in changes that can be visible on image).
4. At the end the text message or a given file is also transformed into a bytes array and stored using bits from colour.

**STRUCTURE**  
Text message: 
```
[int - nrOfChars][char * nrOfChars - message]
```
File: 
```
[int - filenameSize][int - fileSize][byte * filenameSize - filename][byte * fileSize - file]
```

## Licence
[MIT License](https://github.com/MBajdowski/DataInImage/blob/master/LICENSE.txt)