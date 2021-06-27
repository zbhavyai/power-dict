package com.example;

/**
 * Holds the response structure from the wordnik synonynms
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 27, 2021
 */
public class WordnikSyn {
    /**
     * fixed in our urlquery - same-context
     */
    public String relationshipType;

    /**
     * synonyms of the word
     */
    public String[] words;
}
