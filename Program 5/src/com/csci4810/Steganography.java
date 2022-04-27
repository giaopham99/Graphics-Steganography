package com.csci4810;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;

/**
 * Source:
 * https://dyclassroom.com/image-processing-project/how-to-get-and-set-pixel-value-in-java
 * https://www.w3schools.com/java/java_files_create.asp
 * https://mkyong.com/java/java-convert-string-to-binary/
 */
public class Steganography {
    int width;
    int height;

    private File file = null;
    private BufferedImage imageFile = null;
    private BufferedImage newImage = null;
    private String fullText;

    private final String TAG = "***"; // Tag to let program know message boundaries

    /**
     * Method to convert a character into an 8-bit binary string
     * @param character character to be converted to binary string
     * @return 8-bit binary string
     */
    private String charToBinary(char character){
        // Convert ASCII to Binary
        String binary = Integer.toBinaryString(character);
        while(binary.length()<8)
            binary = "0" + binary;

        return binary;
    }

    /**
     * Method to check to see if there is a message hidden in the given picture
     * @return true if the image contains the opening tags, otherwise return false
     */
    private boolean isMessage(){
        String tag = "";
        // 2 Pixels for each character
        for (int x=0; x < TAG.length()*2; x+=2){
            int pixel = imageFile.getRGB(x,0);
            String rgb = "";

            // First Half of a Character
            int alpha1 = (pixel >> 24) & 0xff;
            int red1 = (pixel >> 16) & 0xff;
            int green1 = (pixel >> 8) & 0xff;
            int blue1 = pixel & 0xff;
            // Parse the LSB
            rgb += alpha1 % 2;
            rgb += red1 % 2;
            rgb += green1 % 2;
            rgb += blue1 % 2;

            // Second Half of a Character
            pixel = imageFile.getRGB(x+1,0);
            int alpha2 = (pixel >> 24) & 0xff;
            int red2 = (pixel >> 16) & 0xff;
            int green2 = (pixel >> 8) & 0xff;
            int blue2 = pixel & 0xff;
            // Parse the LSB
            rgb += alpha2 % 2;
            rgb += red2 % 2;
            rgb += green2 % 2;
            rgb += blue2 % 2;

            char currentChar = (char)Integer.parseInt(rgb, 2); // Convert binary to character
            tag += Character.toString(currentChar);

        }
        if(tag.equals(TAG))
            return true;
        else
            return false;
    }

    private boolean isMessageVer2(){
        String tag = "";
        // 1 Pixel for each character
        for (int x=0; x < TAG.length(); x++){
            int pixel = imageFile.getRGB(x,0);
            String rgb = "";

            // First Half of a Character
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;
            // Parse the LSB
            rgb += Integer.toBinaryString(alpha % 4);
            rgb += Integer.toBinaryString(red % 4);
            rgb += Integer.toBinaryString(green % 4);
            rgb += Integer.toBinaryString(blue % 4);

            char currentChar = (char)Integer.parseInt(rgb, 2); // Convert binary to character
            tag += Character.toString(currentChar);

        }
        if(tag.equals(TAG))
            return true;
        else
            return false;
    }

    /**
     * Method to initialize a given image with or without a hidden message
     * @param fileName path to the file containing the image
     */
    private void readImage(String fileName){
        try{
            file = new File("./DataFiles/" + fileName);
            this.imageFile = ImageIO.read(file);
        }catch(IOException e){
            System.out.println(e);
        }
    }

    /**
     * Method to read in the text from a given file
     * @param fileName path to the file containing the text
     */
    private void readText(String fileName){
        String filePath = "./DataFiles/" + fileName;
        // Starter code given by https://www.w3schools.com/java/java_files_read.asp
        try{
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            fullText = "";
            while(reader.hasNextLine()){
                String data = reader.nextLine();
                data += "\n";
                fullText += data;
            }
        }catch(FileNotFoundException e){
            System.out.println("Error: File was not found.");
            e.printStackTrace();
        }
    }

    /**
     * Method to insert a bit from the binary representation of a character into the least significant bit of each pixel.
     * 4 bits fit into each pixel and it takes 2 pixels to represent 1 character.
     */
    private void setNewPixels(){
        fullText = TAG + fullText + TAG; // Set tag
        int index = 0; // Index of text file

        for(int y=0; y < height; y++){
            for (int x=0; x < width-1; x+=2, index++){
                int pixel1 = imageFile.getRGB(x,y);
                int pixel2 = imageFile.getRGB(x+1,y);
                // HEX: 0xff is int(255) or 00000000 00000000 00000000 11111111 in binary
                // For 32 bits, Alpha-Red-Green-Blue
                // 2 pixels per character

                // Change pixels
                if(index < fullText.length()){
                    String binary = charToBinary(fullText.charAt(index));
                    // First Half of a Character
                    int alpha = (pixel1 >> 24) & 0xff;
                    int red = (pixel1 >> 16) & 0xff;
                    int green = (pixel1 >> 8) & 0xff;
                    int blue = pixel1 & 0xff;
                    int[] newRGB = setLSB(alpha, red, green, blue, binary.substring(0,4));
                    int newColor = (newRGB[0]<<24) | (newRGB[1]<<16) | (newRGB[2]<<8) | newRGB[3]; // OR
                    newImage.setRGB(x,y,newColor);

                    // Second Half of a Character
                    alpha = (pixel2 >> 24) & 0xff;
                    red = (pixel2 >> 16) & 0xff;
                    green = (pixel2 >> 8) & 0xff;
                    blue = pixel2 & 0xff;
                    newRGB = setLSB(alpha, red, green, blue, binary.substring(4,8));
                    newColor = (newRGB[0]<<24) | (newRGB[1]<<16) | (newRGB[2]<<8) | newRGB[3]; // OR
                    newImage.setRGB(x+1,y,newColor);
                }
                else{
                    newImage.setRGB(x,y,pixel1);
                    newImage.setRGB(x+1,y,pixel2);
                }
            }
        }
    }

    /**
     * Method to retrieve the least significant bit of each color channel in each pixel and convert to a message
     */
    private void getMessage(){
        fullText = "";
        int count = 0;

        if(isMessage()){
            outerloop: for(int y=0; y < height; y++){
                for (int x=TAG.length()*2; x < width-1; x+=2){
                    int pixel = imageFile.getRGB(x,y);
                    String rgb = "";

                    // First Half of a Character
                    int alpha1 = (pixel >> 24) & 0xff;
                    int red1 = (pixel >> 16) & 0xff;
                    int green1 = (pixel >> 8) & 0xff;
                    int blue1 = pixel & 0xff;
                    // Parse the LSB
                    rgb += alpha1 % 2;
                    rgb += red1 % 2;
                    rgb += green1 % 2;
                    rgb += blue1 % 2;

                    // Second Half of a Character
                    pixel = imageFile.getRGB(x+1,y);
                    int alpha2 = (pixel >> 24) & 0xff;
                    int red2 = (pixel >> 16) & 0xff;
                    int green2 = (pixel >> 8) & 0xff;
                    int blue2 = pixel & 0xff;
                    // Parse the LSB
                    rgb += alpha2 % 2;
                    rgb += red2 % 2;
                    rgb += green2 % 2;
                    rgb += blue2 % 2;

                    char currentChar = (char)Integer.parseInt(rgb, 2); // Convert binary to character
                    String character = Character.toString(currentChar);

                    // Check for ending tag
                    String check = Character.toString(TAG.charAt(count));
                    if(character.equals(check))
                        count++;
                    else
                        fullText += character;

                    if(count == TAG.length())
                        break outerloop;
                }
            }
        }else{
            fullText = "[No Message Found]";
        }
    }

    /**
     * Method to retrieve the 2 least significant bit of each color channel in each pixel and convert to a message
     */
    private void getMessageVer2(){
        fullText = "";
        int count = 0;

        if(isMessageVer2()){
            outerloop: for(int y=0; y < height; y++){
                for (int x=TAG.length(); x < width-1; x++){
                    int pixel = imageFile.getRGB(x,y);
                    String rgb = "";
                    String temp = "";

                    int alpha = (pixel >> 24) & 0xff;
                    int red = (pixel >> 16) & 0xff;
                    int green = (pixel >> 8) & 0xff;
                    int blue = pixel & 0xff;

                    // Parse the LSB
                    temp = Integer.toBinaryString(alpha % 4);
                    if(temp.length() % 2 == 1)
                        temp = "0" + temp;
                    rgb += temp;

                    temp = Integer.toBinaryString(red % 4);
                    if(temp.length() % 2 == 1)
                        temp = "0" + temp;
                    rgb += temp;

                    temp = Integer.toBinaryString(green % 4);
                    if(temp.length() % 2 == 1)
                        temp = "0" + temp;
                    rgb += temp;

                    temp = Integer.toBinaryString(blue % 4);
                    if(temp.length() % 2 == 1)
                        temp = "0" + temp;
                    rgb += temp;

                    char currentChar = (char)Integer.parseInt(rgb, 2); // Convert binary to character
                    String character = Character.toString(currentChar);

                    // Check for ending tag
                    String check = Character.toString(TAG.charAt(count));
                    if(character.equals(check))
                        count++;
                    else
                        fullText += character;

                    if(count == TAG.length())
                        break outerloop;
                }
            }
        }else{
            fullText = "[No Message Found]";
        }
    }

    /**
     * Method to insert 2 bits from the binary representation of a character into the 2 least significant bits of each pixel.
     * It takes 1 pixel to represent 1 character.
     */
    private void setNewPixelsVer2(){
        fullText = TAG + fullText + TAG; // Set tag
        int index = 0; // Index of text file

        for(int y=0; y < height; y++){
            for (int x=0; x < width-1; x++, index++){
                int pixel1 = imageFile.getRGB(x,y);
                // HEX: 0xff is int(255) or 00000000 00000000 00000000 11111111 in binary
                // For 32 bits, Alpha-Red-Green-Blue

                // Change pixels
                if(index < fullText.length()){
                    String binary = charToBinary(fullText.charAt(index));

                    int alpha = (pixel1 >> 24) & 0xff;
                    int red = (pixel1 >> 16) & 0xff;
                    int green = (pixel1 >> 8) & 0xff;
                    int blue = pixel1 & 0xff;
                    int[] newRGB = setLSBVer2(alpha, red, green, blue, binary);
                    int newColor = (newRGB[0]<<24) | (newRGB[1]<<16) | (newRGB[2]<<8) | newRGB[3]; // OR
                    newImage.setRGB(x,y,newColor);
                }
                else{
                    newImage.setRGB(x,y,pixel1);
                }
            }
        }
    }

    /**
     * Method to set the 2 Least Significant Bits of each color channel of each pixel
     * @param alpha value representing the alpha channel
     * @param red value representing the red channel
     * @param green value representing the green channel
     * @param blue value representing the blue channel
     * @param binary 8-bit string representation a character to be encoded
     * @return int array containing new values for each color channel with modified LSB
     */
    private int[] setLSBVer2(int alpha, int red, int green, int blue, String binary){
        int[] color = new int[]{alpha,red,green,blue};
        int[] result = new int[4];

        // Starting code: https://stackoverflow.com/questions/4674006/set-specific-bit-in-byte
        // Set the Least Significant Bit
        for(int i=0, count = 0; count<result.length;i+=2,count++){
            if(binary.substring(i,i+2).equals("01")){
                result[count] = color[count] | 1;
                result[count] = result[count] & ~2;
            }else if(binary.substring(i,i+2).equals("00")){
                result[count] = color[count] & ~1;
                result[count] = result[count] & ~2;
            }else if(binary.substring(i,i+2).equals("10")){
                result[count] = color[count] | 2;
                result[count] = result[count] & ~1;
            }else if(binary.substring(i,i+2).equals("11")){
                result[count] = color[count] | 3;
            }
        }
        return result;
    }

    /**
     * Method to set the Least Significant Bit of each color channel of each pixel
     * @param alpha value representing the alpha channel
     * @param red value representing the red channel
     * @param green value representing the green channel
     * @param blue value representing the blue channel
     * @param binary 4-bit string representation of half a character to be encoded
     * @return int array containing new values for each color channel with modified LSB
     */
    private int[] setLSB(int alpha, int red, int green, int blue, String binary){
        int[] color = new int[]{alpha,red,green,blue};
        int[] result = new int[4];

        // Starting code: https://stackoverflow.com/questions/4674006/set-specific-bit-in-byte
        // Set the Least Significant Bit to 1 or 0 based on binary string
        for(int i=0; i<result.length;i++){
            if(Character.getNumericValue(binary.charAt(i)) == 1){
              result[i] = color[i] | 1;
            }else if(Character.getNumericValue(binary.charAt(i)) == 0){
                result[i] = color[i] & ~1;
            }
        }
        return result;
    }

    /**
     * Method to encode a message into an image and produce a new image with the encoded message.
     * @param textFile path to the text file containing the message
     * @param imageFile path to the image file to put the message in
     * @param newFile name of the new image with the encoded message
     * @param isVer2 true if it is the second implementation
     * @return true if the encoding is complete
     */
    public boolean encode(String textFile, String imageFile, String newFile, boolean isVer2){
        // Read Text File
        readText(textFile);

        // Read Image
        readImage(imageFile);

        // Set Dimensions
        this.width = this.imageFile.getWidth();
        this.height = this.imageFile.getHeight();

        // Keep the width even
        if(this.width % 2 == 1)
            this.width -= 1;

        // Create new image
        newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if(isVer2)
            setNewPixelsVer2();
        else
            setNewPixels();

        // Write Image to File
        // Source: https://stackoverflow.com/questions/42057720/how-do-you-generate-an-image-of-a-specific-size-in-java
        try{
            ImageIO.write(newImage, "png", new File("./DataFiles/" + newFile));
        } catch(IOException e){
            System.out.println("Error: File cannot be created.");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Method to decode a message from a given image and produce a new text file containing the message
     * @param imageFile path to the image file containing the encoded message
     * @param newFile name of the new text file containing the message extracted from the picture
     * @param isVer2 true if it is the second implementation
     */
    public void decode(String imageFile, String newFile, boolean isVer2){
        // Read Image
        readImage(imageFile);

        this.width = this.imageFile.getWidth();
        this.height = this.imageFile.getHeight();

        // Keep the width even
        if(this.width % 2 == 1)
            this.width -= 1;

        // Extract the Least Significant Bit
        if(isVer2)
            getMessageVer2();
        else
            getMessage();

        // Create Text File
        // Source: https://www.w3schools.com/java/java_files_create.asp
        try{
            File file = new File("./DataFiles/" + newFile);
            if(file.createNewFile()){
                FileWriter writer = new FileWriter("./DataFiles/" + newFile);
                writer.write(fullText);
                writer.close();
            }else
                System.out.println("Sorry. This file already exists.");
        } catch(IOException e){
            System.out.println("Error: File cannot be created.");
            e.printStackTrace();
        }
    }
}
