package com.csci4810;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String imageFile;
        String textFile;
        String newFile;

        Scanner keyboard = new Scanner(System.in);
        String command = "";

        Steganography controller = new Steganography();

        while(!command.equals("0") && !command.equals("q")) {
            System.out.println("1) Version 1: Encode a message into a picture.");
            System.out.println("2) Version 1: Decode a message from a picture.");
            System.out.println("3) Version 2: Encode a message into a picture.");
            System.out.println("4) Version 2: Decode a message from a picture.");
            System.out.println("0) Quit");
            command = keyboard.next();
            switch (command) {
                case "0":
                    break;
                case "q":
                    break;
                case "1":
                    System.out.println("Enter a Text File containing the message: ");
                    keyboard.nextLine();
                    textFile = keyboard.nextLine();
                    System.out.println("Enter an Image File to store message in: ");
                    imageFile = keyboard.nextLine();
                    System.out.println("Enter a NEW name for the encoded image file: ");
                    newFile = keyboard.nextLine();
                    System.out.println("Encoding...");
                    controller.encode(textFile, imageFile, newFile, false);
                    System.out.println("Your file has been successfully encoded. A new image called " + newFile +
                            " is placed inside the DataFiles Directory");
                    break;
                case "2":
                    System.out.println("Enter an Image File with the encoded message: ");
                    keyboard.nextLine();
                    imageFile = keyboard.nextLine();
                    System.out.println("Enter a NEW name for the decoded message: ");
                    newFile = keyboard.nextLine();
                    System.out.println("Decoding...");
                    controller.decode(imageFile, newFile,false);
                    System.out.println("Your file has been successfully decoded. A new text file called " + newFile +
                            " is placed inside the DataFiles Directory");
                    break;
                case "3":
                    System.out.println("Enter a Text File containing the message: ");
                    keyboard.nextLine();
                    textFile = keyboard.nextLine();
                    System.out.println("Enter an Image File to store message in: ");
                    imageFile = keyboard.nextLine();
                    System.out.println("Enter a NEW name for the encoded image file: ");
                    newFile = keyboard.nextLine();
                    System.out.println("Encoding...");
                    controller.encode(textFile, imageFile, newFile, true);
                    System.out.println("Your file has been successfully encoded. A new image called " + newFile +
                            " is placed inside the DataFiles Directory");
                    break;
                case "4":
                    System.out.println("Enter an Image File with the encoded message: ");
                    keyboard.nextLine();
                    imageFile = keyboard.nextLine();
                    System.out.println("Enter a NEW name for the decoded message: ");
                    newFile = keyboard.nextLine();
                    System.out.println("Decoding...");
                    controller.decode(imageFile, newFile, true);
                    System.out.println("Your file has been successfully decoded. A new text file called " + newFile +
                            " is placed inside the DataFiles Directory");
                    break;
            }
        }
        System.exit(0);
    }
}
