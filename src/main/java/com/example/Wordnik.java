package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import com.google.gson.Gson;

/**
 * Fetches the definitions and synonyms of an english word using the Wordnik
 * REST API
 *
 * @author Bhavyai Gupta
 * @version 1.0.0
 * @since June 27, 2021
 */
public class Wordnik {
    private static AppIO appIO = AppIO.getInstance();
    private static String baseURL = "https://api.wordnik.com/v4/word.json/";
    private static Charset charset = java.nio.charset.StandardCharsets.UTF_8;

    // definition specific
    private static String definitionSearchType = "definitions";
    private static boolean definitionUseCanonical = true;
    private static int resultLimit = 100;
    private static boolean includeRelated = false;
    private static String sourceDictionaries = "wordnet";
    private static boolean includeTags = false;

    // synonym specific
    private static String synonymSearchType = "relatedWords";
    private static boolean synonymUseCanonical = true;
    private static String relationshipTypes = "synonym";
    private static int limitPerRelationshipType = 100;

    /**
     * Don't let anyone instantiate this class
     */
    private Wordnik() {
    }

    /**
     * Wrapper method to get the wordnik API key from ManageKey
     *
     * @return the API key or null
     */
    private static String getApiKey() {
        ManageKey mk = ManageKey.getInstance();

        // if ManageKey couldn't read the keys File, return null so that its methods are
        // not accessed
        if (mk == null) {
            return null;
        }

        // get the apikey for enum wordnik_v4
        String apiKey = mk.retrieveApiKey(SupportedApi.wordnik_v4);

        return apiKey;
    }

    /**
     * Create the URL to fetch definitions from wordnik
     *
     * @return URL for sending definition
     */
    private static URL createDefinitionQuery(String word) {
        String apiKey = getApiKey();

        if (apiKey == null) {
            appIO.printf("%n%n[%s] Could not find API key associated with Wordnik", ColorText.text("FAIL", Color.RED));
            return null;
        }

        String query = String.format("%s/%s", URLEncoder.encode(word, charset), definitionSearchType);

        String parameters = String.format(
                "limit=%d&includeRelated=%s&sourceDictionaries=%s&useCanonical=%s&includeTags=%s&api_key=%s",
                resultLimit, Boolean.toString(includeRelated), sourceDictionaries,
                Boolean.toString(definitionUseCanonical), Boolean.toString(includeTags), apiKey);

        try {
            // creating the URL based on above strings
            URL urlquery = new URL(baseURL + query + "?" + parameters);
            return urlquery;
        }

        catch (MalformedURLException e) {
            appIO.printf("%n%n[%s] URL is malformed. Please try again later", ColorText.text("FAIL", Color.RED));
            return null;
        }
    }

    /**
     * Create the URL to fetch synonyms from wordnik
     *
     * @return URL for sending synonym
     */
    private static URL createSynonymQuery(String word) {
        String apiKey = getApiKey();

        if (apiKey == null) {
            appIO.printf("%n%n[%s] Could not find API key associated with Wordnik", ColorText.text("FAIL", Color.RED));
            return null;
        }

        String query = String.format("%s/%s", URLEncoder.encode(word, charset), synonymSearchType);

        String parameters = String.format("useCanonical=%s&relationshipTypes=%s&limitPerRelationshipType=%d&api_key=%s",
                Boolean.toString(synonymUseCanonical), relationshipTypes, limitPerRelationshipType, apiKey);

        try {
            // creating the URL based on above strings
            URL urlquery = new URL(baseURL + query + "?" + parameters);
            return urlquery;
        }

        catch (MalformedURLException e) {
            appIO.printf("%n%n[%s] URL is malformed. Please try again later", ColorText.text("FAIL", Color.RED));
            return null;
        }
    }

    /**
     * Fetches the GET response using the urlquery
     *
     * @param urlquery the URL formed for definitions or synonyms
     * @return if successful, the String representing the response, otherwise
     *         <code>null</code>
     */
    private static String getResponse(URL urlquery) {
        try {
            if (urlquery == null) {
                // urlquery can be null due to API key related issues
                return null;
            }

            // create a connection HTTPURLConnection object
            HttpURLConnection connection = (HttpURLConnection) urlquery.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // if the connection got some valid response
            if (connection.getResponseCode() == 200) {
                InputStream in = urlquery.openConnection().getInputStream();

                // read all the response
                // --------------------------------------------------------------------------------
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();

                String currentLine;

                while ((currentLine = br.readLine()) != null) {
                    response.append(currentLine);
                }

                br.close();
                // --------------------------------------------------------------------------------

                return response.toString();
            }

            // bad request
            else if (connection.getResponseCode() == 400) {
                appIO.printf("%n%n[%s] Bad request. Please try again later", ColorText.text("FAIL", Color.RED));
                return null;
            }

            // API key is invalid
            else if (connection.getResponseCode() == 401) {
                appIO.printf("%n%n[%s] Invalid credentials. Please check if the Wordnik API key is valid",
                        ColorText.text("FAIL", Color.RED));
                return null;
            }

            // definitions/synonyms not found
            else if (connection.getResponseCode() == 404) {
                if (urlquery.toString().indexOf("definitions") > 0) {
                    appIO.printf("%n%n[%s] No definitions found", ColorText.text("INFO", Color.BLUE));
                }

                /*
                 * The code shouldn't reach on this else. If definitions are not found, the
                 * VocabIndexer won't send any request for checking of synonyms
                 */
                else {
                    appIO.printf(String.format("%n%n[%s] No synonyms found", ColorText.text("FAIL", Color.RED)));
                }

                return null;
            }

            // too many requests
            else if (connection.getResponseCode() == 429) {
                appIO.printf("Too many requests. Please try again later");
                return null;
            }

            // response code unencountered until now
            else {
                System.err.println("Response = " + connection.getResponseCode());
                return null;
            }
        }

        catch (ProtocolException e) {
            appIO.printf("%n%n[%s] Only GET requests allowed. Please make sure Power-Dict's code hasn't been modified",
                    ColorText.text("FAIL", Color.RED));
            return null;
        }

        catch (UnknownHostException e) {
            appIO.printf("%n%n[%s] Please make sure you are connected to the internet",
                    ColorText.text("FAIL", Color.RED));
            return null;
        }

        catch (IOException e) {
            appIO.printf(e.getMessage());
            return null;
        }
    }

    /**
     * Returns an array of definitions for the <code>word</code> if successfully
     * found
     *
     * @param word the word for which definitions are requested
     * @return arrays of strings if found, <code>null</code> otherwise
     */
    public static String[] getDefinitions(String word) {
        String response = getResponse(createDefinitionQuery(word.toLowerCase()));

        if (response == null) {
            // no need to print this line as relevant errors are already displayed
            // appIO.printf("%n%n[%s] Could not fetch definitions for %s",
            // ColorText.text("FAIL", Color.RED), word);
            return null;
        }

        // formatting the response
        // --------------------------------------------------------------------------------
        Gson g = new Gson();

        // the response contains definitions in a well-defined structure (replicated in
        // WordnikDef class). Passing array of objects of WordnikDef
        // to Gson
        WordnikDef def[] = g.fromJson(response.toString(), WordnikDef[].class);

        // as many definitions as the number of results
        String defString[] = new String[def.length];

        for (int i = 0; i < def.length; i++) {
            defString[i] = def[i].text;
        }
        // --------------------------------------------------------------------------------

        return defString;
    }

    /**
     * Returns a comma separated string of synonyms for the <code>word</code> if
     * successfully found
     *
     * @param word the word for which synonyms are requested
     * @return string if found, <code>null</code> otherwise
     */
    public static String getSynonyms(String word) {
        String response = getResponse(createSynonymQuery(word.toLowerCase()));

        if (response == null) {
            // no need to print this line as relevant errors are already displayed
            // appIO.printf("%n%n[%s] Could not fetch synonyms for %s",
            // ColorText.text("FAIL", Color.RED), word);
            return null;
        }

        // formatting the response
        // --------------------------------------------------------------------------------
        Gson g = new Gson();

        // the response contains definitions in a well-defined structure (replicated
        // in WordnikSyn class). Passing array of objects of WordnikSyn
        // to Gson
        WordnikSyn syn[] = g.fromJson(response.toString(), WordnikSyn[].class);

        StringBuilder synString = new StringBuilder();

        for (int i = 0; i < syn.length; i++) {
            for (int j = 0; j < syn[i].words.length; j++) {
                synString.append(syn[i].words[j]);

                if (j < syn[i].words.length - 1) {
                    synString.append(", ");
                }
            }
        }
        // --------------------------------------------------------------------------------

        return synString.toString();
    }
}
