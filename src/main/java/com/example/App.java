package com.example;

import org.fusesource.jansi.AnsiConsole;

/**
 * The Power-Dict app
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 27, 2021
 */
public class App {
    private AppIO appIO;
    private VocabIndexer vi;

    /**
     * Constructor to initialize the object variables with default values
     */
    public App() {
        this.appIO = AppIO.getInstance();
        this.vi = VocabIndexer.getInstance();
        AnsiConsole.systemInstall();
    }

    /**
     * Power-Dict menu
     */
    public void appMenu() {
        // priming input ch
        int choice = 9;

        while (choice != 0) {
            this.appIO.clearConsole();

            this.appIO.printf("%n%nWelcome to Power Dict");
            this.appIO.printf("%n---------------------");
            this.appIO.printf("%n%n[1] Search a word");
            this.appIO.printf("%n%n[2] Print history");
            this.appIO.printf("%n%n[3] Remove a word from history");
            this.appIO.printf("%n%n[4] Clear all history");
            this.appIO.printf("%n%n[5] Configure API key");
            this.appIO.printf("%n%n[0] Exit");

            try {
                this.appIO.printf("%n%n%n[%s] Please enter your choice: ", ColorText.text("QUES", Color.YELLOW));
                choice = Integer.parseInt(this.appIO.readLine());

                switch (choice) {
                    // search (and if successful, cache) a word
                    case 1: {
                        String word = this.appIO.readLine("%n%n[%s] Enter the word: ",
                                ColorText.text("QUES", Color.YELLOW));

                        // trim to remove spaces
                        word = word.trim();

                        if (word.equals("") || word == null) {
                            this.appIO.printf("%n%n[%s] Unable to search empty strings. Please try again later.",
                                    ColorText.text("FAIL", Color.RED));
                        }

                        else {
                            this.appIO.clearConsole();
                            this.vi.search(word);
                        }

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // print history
                    case 2: {
                        this.appIO.clearConsole();

                        this.vi.printAll();

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // remove a word from history
                    case 3: {
                        this.appIO.clearConsole();

                        String word = this.appIO.readLine("%n%n[%s] Enter the word to remove from history: ",
                                ColorText.text("QUES", Color.YELLOW));

                        // trim to remove spaces
                        word = word.trim();

                        if (word.equals("") || word == null) {
                            this.appIO.printf("%n%n[%s] Unable to remove empty strings. Please try again later.",
                                    ColorText.text("FAIL", Color.RED));
                        }

                        else {
                            if (this.vi.remove(word)) {
                                this.appIO.printf("%n%n[%s] \'%s\' removed from history",
                                        ColorText.text("DONE", Color.GREEN), word);
                            }

                            else {
                                this.appIO.printf(
                                        "%n%n[%s] Couldn't remove \'%s\' from history. Please try again later",
                                        ColorText.text("FAIL", Color.RED), word);
                            }
                        }

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // clear history
                    case 4: {
                        this.appIO.clearConsole();

                        // ask for user confirmation
                        String choice_clear = this.appIO.readLine(
                                "%n%n[%s] Do you really want to clear history and the cached results (y/N)? ",
                                ColorText.text("QUES", Color.YELLOW));

                        // is user doesn't say anything, dont clear history
                        if ((choice_clear == null || choice_clear.equals(""))) {
                            this.appIO.printf("%n%n[%s] History not cleared", ColorText.text("DONE", Color.GREEN));
                        }

                        // if user said something, check what he said
                        else {
                            // if user says yes
                            if (choice_clear.charAt(0) == 'Y' || choice_clear.charAt(0) == 'y') {
                                if (this.vi.removeAll()) {
                                    this.appIO.printf("%n%n[%s] History cleared", ColorText.text("DONE", Color.GREEN));
                                }

                                else {
                                    this.appIO.printf("%n%n[%s] Some items could not be removed from history",
                                            ColorText.text("FAIL", Color.RED));
                                }
                            }

                            // if user says no
                            else {
                                this.appIO.printf("%n%n[%s] History not cleared", ColorText.text("DONE", Color.GREEN));
                            }
                        }

                        this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // manage api keys
                    case 5: {
                        this.appIO.clearConsole();

                        // check the exceptions that might occur when reading the keysFile
                        ManageKey mk = ManageKey.getInstance();

                        if (mk == null) {
                            this.appIO.printf("%n%n[%s] Cannot read or create keys file",
                                    ColorText.text("FAIL", Color.RED));

                            this.appIO.readLine("%n%n%nPress enter to return to the menu ");
                        }

                        else {
                            mk.configMenu();
                        }

                    }
                        break;

                    // exit by changing choice
                    case 0: {
                        choice = 0;
                    }
                        break;

                    default: {
                        throw new NumberFormatException();
                    }
                }
            }

            catch (NumberFormatException e) {
                this.appIO.printf("%n%n[%s] Please enter a valid choice", ColorText.text("FAIL", Color.RED));
                this.appIO.readLine("%n%n%nPress enter to return to the menu ");
            }

            catch (java.util.NoSuchElementException e) {
                this.appIO.printf("%n%n[%s] Input stream has been closed. Bye.", ColorText.text("FAIL", Color.RED));
                Runtime.getRuntime().exit(0);
            }
        }
    }

    public static void main(String[] args) {
        App main = new App();
        main.appMenu();
    }
}
