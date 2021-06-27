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
import java.io.Serializable;

/**
 * Stores the vocab word, and provides methods to serialize/deserialize Vocab
 * objects
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 27, 2021
 */
public class Vocab implements Serializable {
    // generated via serialver
    private static final long serialVersionUID = 1071553591311788248L;

    // actual fields to be serialized/deserialized
    public String word;
    public String[] definition;
    public String synonyms;

    // no need to declare static fields as transient
    static private String basepath = "history";
    static private String fileExtension = ".ser";
    static private AppIO appIO = AppIO.getInstance();

    /**
     * deserialize the word from the file "filename"
     *
     * @param filename the filename in which the word is serialized
     * @return an object of <code>Vocab</code> if deserialization is successful,
     *         <code>null</code> otherwise
     */
    public static Vocab read(String filename) {
        Vocab vocab = null;

        // create the to-be filename: basepath/filename.ser
        File f = new File(basepath + File.separator + filename + fileExtension);

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))) {
            // read the object from the file
            Object obj = ois.readObject();

            if (obj instanceof Vocab) {
                vocab = (Vocab) obj;
            }
        }

        catch (FileNotFoundException e) {
            appIO.printf("%n%n[%s] Cannot find \"%s\". Please try again later", ColorText.text("FAIL", Color.RED),
                    appIO.fetchCanonical(f));
        }

        catch (IOException e) {
            appIO.printf(
                    "%n%n[%s] Unable to read from \"%s\". Please make sure file isn't corrupted and Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), appIO.fetchCanonical(f));
        }

        catch (ClassNotFoundException e) {
            appIO.printf("%n%n[%s] Class not found during casting. Please make sure \"%s\" isn't corrupted",
                    ColorText.text("FAIL", Color.RED), appIO.fetchCanonical(f));
        }

        return vocab;
    }

    /**
     * serialize vocab into file "filename"
     *
     * @param filename the filename in which the word will be serialized
     * @param vocab    the object to serialize
     * @return <code>true</code> if serialization is successful, <code>false</code>
     *         otherwise
     */
    public static boolean write(Vocab vocab, String filename) {
        boolean flag = false;

        // create the to-be filename: basepath/filename.ser
        File f = new File(basepath + File.separator + filename + fileExtension);

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
            oos.writeObject(vocab);
            oos.flush();
            flag = true;
        }

        catch (FileNotFoundException e) {
            appIO.printf("%n%n[%s] Cannot find \"%s\". Please try again later", ColorText.text("FAIL", Color.RED),
                    appIO.fetchCanonical(f));
        }

        catch (IOException e) {
            appIO.printf(
                    "%n%n[%s] Unable to read from \"%s\". Please make sure file isn't corrupted and Power-Dict has appropriate permissions",
                    ColorText.text("FAIL", Color.RED), appIO.fetchCanonical(f));
        }

        return flag;
    }

    /**
     * Remove a cache of the vocab from the disk
     *
     * @param filename the name of the file to be removed
     * @return <code>true</code> if removal is successful, <code>false</code>
     *         otherwise
     */
    public static boolean remove(String filename) {
        boolean flag = false;

        File f = new File(basepath + File.separator + filename + fileExtension);

        if (f.delete() == true) {
            flag = true;
        }

        return flag;
    }

    /**
     * Method to print the word, its definitions, and all the synonyms
     */
    public void print() {
        appIO.printf("%n%n%n%s%n%n%s%n", ColorText.text("Word -", Color.MAGENTA), this.word);

        if (this.definition.length > 0) {
            appIO.printf("%n%n%s%n", ColorText.text("Definitions -", Color.MAGENTA));

            for (int i = 0; i < this.definition.length; i++) {
                appIO.printf("%n%d. %s", (i + 1), this.definition[i]);
            }
        }

        if (this.synonyms.length() > 0) {
            appIO.printf("%n%n%n%s%n%n%s", ColorText.text("Synonyms -", Color.MAGENTA), this.synonyms);
        }
    }
}
