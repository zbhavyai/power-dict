package com.example;

/**
 * Holds the response structure from the wordnik definitions
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 27, 2021
 */
public class WordnikDef {
    /**
     * noun, verb, adjective, etc
     */
    public String partOfSpeech;

    /**
     * definition of the word
     */
    public String text;

    /*
     * Below data from the response is not used in Power-Dict
     */
    public String sourceDictionary;
    public String attributionText;
    public Object labels;
    public Object citations;
    public String word;
    public Object relatedWords;
    public Object exampleUses;
    public Object textProns;
    public Object notes;
    public String attributionUrl;
    public String wordnikUrl;
}
