package com.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;

/**
 * Class to manage the API key for the Power-Dict
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 27, 2021
 */
public class ManageKey {
    static private AppIO appIO = AppIO.getInstance();
    private File keysFile;
    private HashMap<SupportedApi, String> keys;

    /**
     * The instance variable containing the one and only object of ManageKey
     */
    private static ManageKey instanceVar = null;

    /**
     * ManageKey follows Singleton design pattern
     *
     * @return the instance of this ManageKey
     * @throws IOException if the keys file cannot be read or created
     */
    public static ManageKey getInstance() {
        try {
            if (instanceVar == null)
                instanceVar = new ManageKey();

            return instanceVar;
        }

        catch (IOException e) {
            appIO.printf(e.getMessage());
            return null;
        }
    }

    /**
     * Private constructor to restrict instantiating by foreign functions
     */
    private ManageKey() throws IOException {
        // set the location of the keysFiles
        String defaultPath = "keys";
        this.keysFile = new File(defaultPath);

        this.keys = new HashMap<SupportedApi, String>();

        // if the keysFile can be read, read it to store the keys in the memory
        if (this.keysFile.isFile() && this.keysFile.canRead()) {
            if (this.read() == false) {
                throw new IOException(String.format("%n%n[%s] \"%s\" file cannot be read",
                        ColorText.text("FAIL", Color.RED), this.keysFile.getAbsolutePath()));
            }
        }

        // if for permissions the keysFile cannot be read
        else if ((this.keysFile.isFile() == true) && (this.keysFile.canRead() == false)) {
            throw new IOException(String.format(
                    "%n%n[%s] \"%s\" file cannot be read. Please make sure Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), this.keysFile.getAbsolutePath()));
        }

        // if the keysFile does not exist, create it
        else {
            try {
                if (this.write() == false) {
                    throw new IOException("Cannot create a new file");
                }
            }

            catch (IOException | SecurityException e) {
                throw new IOException(String.format(
                        "%n%n[%s] \"%s\" file could not be created. Please make sure Power-Dict has appropriate permissions",
                        ColorText.text("FAIL", Color.RED), this.keysFile.getAbsolutePath()));
            }
        }
    }

    /**
     * Read the File keysFile into HashMap keys
     *
     * @return <code>true</code> if successfully deserialized, <code>false</code>
     *         otherwise
     */
    @SuppressWarnings("unchecked")
    private boolean read() {
        boolean flag = false;

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(this.keysFile)))) {
            // clean the old keys (although not required as we are assigning a new object)
            this.keys.clear();

            // read the object from the file
            Object obj = ois.readObject();

            if (obj instanceof HashMap<?, ?>) {
                this.keys = (HashMap<SupportedApi, String>) obj;

                flag = true;
            }
        }

        catch (FileNotFoundException e) {
            appIO.printf("%n%n[%s] Cannot find \"%s\". Please try again later", ColorText.text("FAIL", Color.RED),
                    appIO.fetchCanonical(this.keysFile));
        }

        catch (IOException e) {
            appIO.printf(
                    "%n%n[%s] Unable to read keys from \"%s\". Please make sure file isn't corrupted and Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), appIO.fetchCanonical(this.keysFile));

            // try to reset the file by writing again
            String choice = appIO.readLine("%n%n[%s] Would you like to reset the file (N/y)? ",
                    ColorText.text("QUES", Color.YELLOW));

            // reset the file again if user says yes
            if (!(choice == "" || choice == null)) {
                if (choice.charAt(0) == 'y' || choice.charAt(0) == 'Y') {
                    appIO.printf("%n%n[%s] Repairing %s", ColorText.text("INFO", Color.BLUE),
                            appIO.fetchCanonical(this.keysFile));

                    if (this.write() == true) {
                        flag = true;
                        appIO.printf("%n%n[%s] Repair successful", ColorText.text("DONE", Color.GREEN));
                    }

                    else {
                        appIO.printf("%n%n[%s] Repair unsuccessful", ColorText.text("FAIL", Color.RED));
                    }

                    appIO.readLine("%n%n%nPress enter to configure API keys");
                }
            }
        }

        catch (ClassNotFoundException e) {
            appIO.printf("%n%n[%s] Class not found during casting. Please make sure \'%s\" isn't corrupted",
                    ColorText.text("FAIL", Color.RED), appIO.fetchCanonical(this.keysFile));
        }

        return flag;
    }

    /**
     * Save the HashMap keys into File keysFile
     *
     * @return <code>true</code> if successfully deserialized, <code>false</code>
     *         otherwise
     */
    private boolean write() {
        boolean flag = false;

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(this.keysFile)))) {

            oos.writeObject(this.keys);
            oos.flush();
            flag = true;
        }

        catch (FileNotFoundException e) {
            appIO.printf("%n%n[%s] Cannot find \"%s\". Please try again later", ColorText.text("FAIL", Color.RED),
                    appIO.fetchCanonical(this.keysFile));
        }

        catch (IOException e) {
            appIO.printf(
                    "%n%n[%s] Unable to save keys to \"%s\". Please make sure Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), appIO.fetchCanonical(this.keysFile));
        }

        return flag;
    }

    /**
     * Add an apikey to the Power-Dict
     *
     * @param label  the label of the apikey
     * @param apikey the API key
     * @return <code>true</code> if successfully deserialized, <code>false</code>
     *         otherwise
     */
    private boolean addKey(SupportedApi label, String apikey) {
        apikey = apikey.trim();

        this.keys.put(label, apikey);
        return this.write();
    }

    /**
     * Remove a stored API key from the Power-Dict
     *
     * @param label the label of the apikey to be removed
     * @return <code>true</code> if successfully deserialized, <code>false</code>
     *         otherwise
     */
    private boolean removeKey(SupportedApi label) {
        String test = this.keys.remove(label);

        if (test == null) {
            return false;
        }

        else {
            return this.write();
        }
    }

    /**
     * Print all the labels of the API keys stored by the Power-Dict
     */
    @SuppressWarnings("unused")
    private void print() {
        Set<SupportedApi> all_labels = this.keys.keySet();

        for (SupportedApi label : all_labels) {
            appIO.printf("%n- \"%s\"", label.toString());
        }
    }

    /**
     * Retrieve the API key stored by the Power-Dict
     *
     * @param label the label of the API key to be retrieved
     * @return the API key
     */
    public String retrieveApiKey(SupportedApi label) {
        return this.keys.get(label);
    }

    /**
     * Menu for managing API keys - add/remove/reveal
     */
    public void configMenu() {
        // priming input ch
        int choice = 9;

        while (choice != 0) {
            appIO.clearConsole();

            appIO.printf("%n%nManage Keys");
            appIO.printf("%n-----------");
            appIO.printf("%n%n[1] Add/overwrite the Wordnik API key");
            appIO.printf("%n%n[2] Remove the Wordnik API key");
            appIO.printf("%n%n[3] Reveal the Wordnik API key");
            appIO.printf("%n%n[0] Return to the previous the menu");

            try {
                appIO.printf("%n%n%n[%s] Please enter your choice: ", ColorText.text("QUES", Color.YELLOW));
                choice = Integer.parseInt(appIO.readLine());

                switch (choice) {
                    // add
                    case 1: {
                        appIO.clearConsole();

                        String apikey = appIO.readLine("%n%n[%s] Enter the API key for Wordnik: ",
                                ColorText.text("QUES", Color.YELLOW));

                        // trim to remove spaces
                        apikey = apikey.trim();

                        if (apikey.equals("") || apikey == null) {
                            appIO.printf("%n%n[%s] Unable to add an empty API key. Please try again later.",
                                    ColorText.text("FAIL", Color.RED));
                        }

                        else {
                            if (this.addKey(SupportedApi.wordnik_v4, apikey)) {
                                appIO.printf("%n%n[%s] Key added successfully", ColorText.text("DONE", Color.GREEN));
                            }

                            else {
                                appIO.printf("%n%n[%s] Unable to add key. Please try again later.",
                                        ColorText.text("FAIL", Color.RED));
                            }
                        }

                        appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // remove
                    case 2: {
                        appIO.clearConsole();

                        // ask for user confirmation
                        String choice_clear = appIO.readLine(
                                "%n%n[%s] Do you really want to remove the saved key (y/N)? ",
                                ColorText.text("QUES", Color.YELLOW));

                        // is user doesn't say anything, dont clear history
                        if ((choice_clear == null || choice_clear.equals(""))) {
                            appIO.printf("%n%n[%s] Key not removed", ColorText.text("DONE", Color.GREEN));
                        }

                        // if user said something, check what he said
                        else {
                            // if user says yes
                            if (choice_clear.charAt(0) == 'Y' || choice_clear.charAt(0) == 'y') {
                                if (this.removeKey(SupportedApi.wordnik_v4)) {
                                    appIO.printf("%n%n[%s] Key removed successfully",
                                            ColorText.text("DONE", Color.GREEN));
                                }

                                else {
                                    appIO.printf(
                                            "%n%n[%s] Unable to remove key. Please check if it exists or try again later.",
                                            ColorText.text("FAIL", Color.RED));
                                }
                            }

                            // if user says no
                            else {
                                appIO.printf("%n%n[%s] Key not removed", ColorText.text("DONE", Color.GREEN));
                            }
                        }

                        appIO.readLine("%n%n%nPress enter to return to the menu ");
                    }
                        break;

                    // reveal/retrieve
                    case 3: {
                        appIO.clearConsole();

                        String key = this.retrieveApiKey(SupportedApi.wordnik_v4);

                        if (key != null) {
                            appIO.printf("%n%n[%s] Wordnik API Key = \"%s\"", ColorText.text("INFO", Color.BLUE), key);
                        }

                        else {
                            appIO.printf("%n%n[%s] No Wordnik API key is stored", ColorText.text("INFO", Color.BLUE));
                        }

                        appIO.readLine("%n%n%nPress enter to return to the menu ");
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
                appIO.printf("%n%n[%s] Please enter a valid choice", ColorText.text("FAIL", Color.RED));
                appIO.readLine("%n%n%nPress enter to return to the menu ");
            }

            catch (java.util.NoSuchElementException e) {
                appIO.printf("%n%n[%s] Input stream has been closed. Bye.", ColorText.text("FAIL", Color.RED));
                Runtime.getRuntime().exit(0);
            }
        }
    }
}
