package com.fooddeliveryapp.util;

import java.io.File;
import java.util.Scanner;

public final class ConsoleInput {

    private static Scanner scanner;
    private static boolean readingFromFile = false;

    // Static block initializes the file reader if input.txt exists
    static {
        try {
            File file = new File("input.txt");
            if (file.exists()) {
                scanner = new Scanner(file);
                readingFromFile = true;
                System.out.println("[INFO] Reading automated input from 'input.txt'...");
            } else {
                scanner = new Scanner(System.in);
            }
        } catch (Exception e) {
            scanner = new Scanner(System.in);
        }
    }

    private ConsoleInput() {}

    private static String readNextLine() {
        if (readingFromFile) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) { // Allows # comments in txt file
                    System.out.println(line); // Echo the file input to console
                    return line;
                }
            }
            System.out.println("\n[INFO] End of 'input.txt'. Switching to manual keyboard input.\n");
            scanner = new Scanner(System.in);
            readingFromFile = false;
        }
        try {
            return scanner.nextLine().trim();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("\nGoodbye! Have a great day!");
            System.exit(0);
            return ""; // required by compiler; System.exit(0) ensures this is never reached
        }
    }

    public static String getString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = readNextLine();
            if (!line.isEmpty()) {
                return line; // buffer bug solve
            }
        }
    }

    public static int getInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(getString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    public static long getLong(String prompt) {
        while (true) {
            try {
                return Long.parseLong(getString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    public static double getDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(getString(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal.");
            }
        }
    }
}