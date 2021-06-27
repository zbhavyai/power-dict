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
import java.util.Iterator;

/**
 * Stores the history of successful search of words and file name of their
 * cached data
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 21, 2021
 */
public class VocabIndexer {
    private AppIO appIO;

    /**
     * hashmap to hold vocab words and where they are serialized
     */
    private HashMap<String, String> index;

    /**
     * index is serialized and deserialized from this file
     */
    private File indexFile;

    /**
     * The instance variable containing the one and only object of VocabIndexer
     */
    private static VocabIndexer instanceVar = null;

    /**
     * VocabIndexer follows Singleton design pattern
     *
     * @return the instance of this VocabIndexer
     */
    public static VocabIndexer getInstance() {
        if (instanceVar == null)
            instanceVar = new VocabIndexer();

        return instanceVar;
    }

    /**
     * Private constructor to restrict instantiating by foreign functions
     */
    private VocabIndexer() {
        this.appIO = AppIO.getInstance();
        this.index = new HashMap<>();
        String defaultPath = "index";
        this.indexFile = new File(defaultPath);

        File historyFolder = new File("history");
        historyFolder.mkdirs();

        // if the indexFile can be read, read it to store the indexFile in the memory
        if (this.indexFile.isFile() && this.indexFile.canRead()) {
            this.read();
        }

        // if for permissions the indexFile cannot be read
        else if ((this.indexFile.isFile() == true) && (this.indexFile.canRead() == false)) {
            this.appIO.printf("%n%n[%s] \"%s\" file cannot be read", ColorText.text("FAIL", Color.RED),
                    this.appIO.fetchCanonical(this.indexFile));
        }

        // if the indexFile does not exist, create it
        else {
            try {
                if (this.write() == false) {
                    throw new IOException("Cannot create a new file");
                }
            }

            catch (IOException | SecurityException e) {
                this.appIO.printf(
                        "%n%n[%s] \"%s\" file could not be created. Please make sure Power-Dict has appropriate permissions",
                        ColorText.text("FAIL", Color.RED), this.appIO.fetchCanonical(this.indexFile));
            }
        }
    }

    /**
     * Deserialize the HashMap<String, String> from the file indexFile into index
     *
     * @return <code>true</code> if successfully deserialized, <code>false</code>
     *         otherwise
     */
    @SuppressWarnings("unchecked")
    private boolean read() {
        boolean flag = false;

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(this.indexFile)))) {
            // clean the old index (although not required as we are assigning a new object)
            this.index.clear();

            // read the object from the file
            Object obj = ois.readObject();

            if (obj instanceof HashMap<?, ?>) {
                this.index = (HashMap<String, String>) obj;

                flag = true;
            }
        }

        catch (FileNotFoundException e) {
            this.appIO.printf("%n%n[%s] Cannot find \"%s\". Please try again later",
                    ColorText.text("FAIL", Color.RED), this.appIO.fetchCanonical(this.indexFile));
        }

        catch (IOException e) {
            this.appIO.printf(
                    "%n%n[%s] Unable to read history from \"%s\". Please make sure file isn't corrupted and Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), this.appIO.fetchCanonical(this.indexFile));
        }

        catch (ClassNotFoundException e) {
            this.appIO.printf("%n%n[%s] Class not found during casting. Please make sure \"%s\" isn't corrupted",
                    ColorText.text("FAIL", Color.RED), this.appIO.fetchCanonical(this.indexFile));
        }

        return flag;
    }

    /**
     * Serialize the HashMap<String, String> from index to indexFile
     *
     * @return <code>true</code> if successfully serialized, <code>false</code>
     *         otherwise
     */
    private boolean write() {
        boolean flag = false;

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(this.indexFile)))) {

            oos.writeObject(this.index);
            oos.flush();
            flag = true;
        }

        catch (FileNotFoundException e) {
            this.appIO.printf("%n%n[%s] Cannot find \"%s\". Please try again later",
                    ColorText.text("FAIL", Color.RED), this.appIO.fetchCanonical(this.indexFile));
        }

        catch (IOException e) {
            this.appIO.printf(
                    "%n%n[%s] Unable to save history to \"%s\". Please make sure Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), this.appIO.fetchCanonical(this.indexFile));
        }

        return flag;
    }

    /**
     * Searches the word from the history and displays the cached result. If the
     * word is not found in cache, it goes online to retrieve the results
     *
     * @param word the word whose meaning and synonyms is to be searched
     */
    public void search(String word) {
        Vocab vocab;

        // if the word is in history, show cached results
        if (this.index.containsKey(word)) {
            vocab = Vocab.read(this.index.get(word));

            this.appIO.printf("%n[%s] %s", ColorText.text("INFO", Color.BLUE), "showing cached results");
            vocab.print();
        }

        // if the word is not in history, search online
        else {
            vocab = new Vocab();

            vocab.word = word;
            vocab.definition = Wordnik.getDefinitions(word);

            // if the definitions was successful, then only search for synonyms
            if (vocab.definition != null) {
                vocab.synonyms = Wordnik.getSynonyms(word);

                this.appIO.printf("%n[%s] %s", ColorText.text("INFO", Color.BLUE), "showing online results");
                vocab.print();

                // add the word to history and cache the results
                this.add(vocab);
            }
        }
    }

    /**
     * insert a word into the Power-Dict. If the word already exists, the word and
     * its data will replace the existing information in the same serialized file
     *
     * @param word the word to be inserted or replaced
     * @return <code>true</code> if insertion is successful, <code>false</code>
     *         otherwise
     */
    private boolean add(Vocab vocab) {
        // get a unique name for storing vocab
        String filename = this.getUniqueName();

        // cache the vocab word using the unique filename
        Vocab.write(vocab, filename);

        // update the history (in VocabIndexer) in memory
        this.index.put(vocab.word, filename);

        // save the VocabIndexer on disk
        return this.write();
    }

    /**
     * Remove the word from the history and deletes its cached results
     *
     * @param word the word to be removed from history and cache
     * @return <code>true</code> is removal is successful, <code>false</code>
     *         otherwise
     */
    public boolean remove(String word) {
        // if deleting the cache is successful
        if (Vocab.remove(this.index.get(word))) {
            // then delete the history
            this.index.remove(word);

            // save the history
            return this.write();
        }

        // else return false
        else {
            return false;
        }
    }

    /**
     * Clear the history and clear all cached data (kind of a reset)
     *
     * @return <code>true</code> is clear is successful, <code>false</code>
     *         otherwise
     */
    public boolean removeAll() {
        // flagAll to track overall deletion
        boolean flagAll = false;

        // get the Iterator over all the stored words
        Iterator<String> i = this.index.keySet().iterator();

        // if there is no history, just return true
        if (!i.hasNext()) {
            flagAll = true;
            return flagAll;
        }

        while (i.hasNext()) {
            String word_to_remove = i.next();
            String cache_to_remove = this.index.get(word_to_remove);

            // flag to track individual deletion
            boolean flag = false;

            // removing vocab from cache
            flag = Vocab.remove(cache_to_remove);

            if (flag) {
                // removing vocab from history
                i.remove();
            }

            // updating the master flagAll
            flagAll = flagAll || flag;
        }

        this.write();

        return flagAll;
    }

    /**
     * Print all the words stored by the Power-Dict.
     */
    public void printAll() {
        // get the Iterator over all the keys (all the words stored)
        Iterator<String> i = this.index.keySet().iterator();

        if (i.hasNext()) {
            this.appIO.printf("%n%s%n", ColorText.text("History -", Color.MAGENTA));

            while (i.hasNext()) {
                String word = i.next();

                this.appIO.printf("%n- %s", word);
            }
        }

        else {
            this.appIO.printf("%n%s", ColorText.text("No history available", Color.MAGENTA));
        }
    }

    /**
     * Generates and returns a random string
     *
     * @return a string of length six containing [A-Z]
     */
    private String getUniqueName() {
        boolean isUnique = false;
        StringBuilder uniqueName = new StringBuilder();

        int min = (int) 'A';
        int max = (int) 'Z';
        char random;

        while (isUnique != true) {
            // loop to generate a six letter string
            for (int i = 0; i < 6; i++) {
                // get a random character from 'A' to 'Z'
                random = (char) (Math.random() * (max - min + 1) + min);

                // append the character to the string
                uniqueName.append(String.valueOf(random));
            }

            // check if the string generated is not already contained
            if (this.index.containsValue(uniqueName.toString()) == false) {
                isUnique = true;
            }
        }

        return uniqueName.toString();
    }
}
