package com.example;

import java.io.Console;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.File;

/**
 * Class to retrieve reader and writer objects for Power Dict.
 *
 * This class is to be used as a factory for IO objects, to make future changes
 * easy.
 *
 * @author Bhavyai Gupta
 * @version 1.5.0
 * @since May 29, 2021
 */
public class AppIO {
    /**
     * The underlying console object, with which all IO is done
     */
    private Console console;

    /**
     * The instance variable containing the one and only object of AppIO
     */
    private static AppIO instanceVar = null;

    /**
     * AppIO follows Singleton design pattern
     *
     * @return The AppIO object
     */
    public static AppIO getInstance() {
        if (instanceVar == null)
            instanceVar = new AppIO();

        return instanceVar;
    }

    /**
     * Constructs a console object. Exits the program if the Power-Dict is not
     * started in the console mode
     */
    private AppIO() {
        try {
            this.console = System.console();

            if (this.console == null) {
                throw new IOException("Application not started in console mode!");
            }
        }

        catch (IOException e) {
            System.err.printf("%n%n[%s] %s", ColorText.text("FAIL", Color.RED), e.getMessage());
            System.err.printf("%n%n[%s] Cannot proceed further!", ColorText.text("QUIT", Color.RED));
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Method to write formatted strings to the console.
     *
     * @param fmt  A format string as described in Format string syntax.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     * @return This console
     */
    public Console printf(String fmt, Object... args) {
        return this.console.format(fmt, args);
    }

    /**
     * Retrieve a PrintWriter object for writing the output to the console
     *
     * @return The printwriter associated with this console
     */
    public Writer getWriter() {
        return this.console.writer();
    }

    /**
     * Method to provide a formatted prompt, and then read a single line of text
     * from the console.
     *
     * @param fmt  A format string as described in Format string syntax.
     * @param args Arguments referenced by the format specifiers in the format
     *             string.
     * @return A string containing the line read from the console
     */
    public String readLine(String fmt, Object... args) {
        return this.console.readLine(fmt, args);
    }

    /**
     * Method to read a single line of text from the console.
     *
     * @return A string containing the line read from the console
     */
    public String readLine() {
        return this.readLine("");
    }

    /**
     * Retrieve a reader object for reading the input from console
     *
     * @return The reader associated with this console
     */
    public Reader getReader() {
        return this.console.reader();
    }

    /**
     * Clear the input/output area
     */
    public void clearConsole() {
        this.console.printf("\033\143");
    }

    /**
     * Get a String from user, representing a valid directory on the system
     *
     * @param prompt The prompt to display to user before accepting input
     * @return A String representing a valid directory on the system
     */
    public String getDirectory(String prompt) {
        String temp;
        File ftemp;

        while (true) {
            temp = this.readLine("%n%n[%s] %s: ", ColorText.text("QUES", Color.YELLOW), prompt);

            try {
                // filtering out empty strings and null values
                if (temp.equals("") || temp == null) {
                    throw new IOException();
                }

                /*
                 * Adding extra slash because directories like D:, E: are canonically
                 * represented as D:\, E:\, while other directories are represented like
                 * D:\Folder, without \ at the end.
                 *
                 * If backward slash is not used, D: or E: would be read as working directory by
                 * the File() constructor. May be its a bug?
                 */
                if (temp.charAt(temp.length() - 1) != File.separatorChar)
                    temp = temp + File.separator;

                ftemp = new File(temp);

                /*
                 * reassigning because the check File.isDirectory() doesn't work properly if an
                 * empty string is passed. Empty argument means working directory as printed by
                 * File.getAbsolutePath(), but the File.isDirectory() always returns false.
                 *
                 * So solution is to -> [1] Filter out the empty strings, because they are
                 * usually entered by mistake [2] Reassign the string entered by user with
                 * File.getAbsolutePath(), to avoid other possible unexpected failures
                 *
                 * Edit - changed File.getAbsolutePath() to File.getCanonicalPath()
                 */
                temp = ftemp.getCanonicalPath();
                ftemp = new File(temp);

                if (ftemp.isDirectory()) {
                    // only way to break the while(true) loop is to enter a valid directory
                    return temp;
                }

                else {
                    throw new IOException();
                }
            }

            catch (IOException e) {
                this.printf("%n%n[%s] Cannot find \"%s\". Please enter a valid directory.",
                        ColorText.text("FAIL", Color.RED), temp);
            }
        }
    }

    /**
     * This method is to clear the clutter of writing try-catch everytime when
     * there's a need to get the canonical path of a file
     *
     * @param f The File whose canonical path is to be fetched
     * @return if possible canonical path to the File parameter, else absolute path
     */
    public String fetchCanonical(File f) {
        String path = "";

        try {
            path = f.getCanonicalPath();
        }

        catch (IOException e) {
            path = f.getAbsolutePath();
            this.printf("%n%n[%s] Error in fetching canonical path, falling back to absolute path \"%s\"",
                    ColorText.text("FAIL", Color.RED), path);
        }

        return path;
    }
}
