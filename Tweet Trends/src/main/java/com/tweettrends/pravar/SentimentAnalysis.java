package com.tweettrends.pravar;

import sun.net.www.protocol.https.HttpsURLConnectionImpl;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by PRAVAR on 19-04-2017.
 */
public class SentimentAnalysis {


    public String analyzeSentiment(String tweetInJson) {
        String tweetText = null;
        String response = null;
        String sentiment = null;
        try {
            tweetText = extractTextField(tweetInJson);
            String watsonUrl = makeUrl(tweetText);
            response = downloadUrl(watsonUrl);
            sentiment = parseSentimentFromResponse(response);
            System.out.println("Tweet Text = "+tweetText);
            System.out.println("Sentiment = "+sentiment);
        }catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return sentiment;

    }
    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpsURLConnectionImpl urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpsURLConnectionImpl) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String makeUrl(String inputText) throws UnsupportedEncodingException {
        String firstPart="https://watson-api-explorer.mybluemix.net/natural-language-understanding/api/v1/analyze?version=2017-02-27&";

        /*String[] words = inputText.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }*/
        String secondPart="text="+ URLEncoder.encode(inputText, "UTF-8");

        String thirdPart="&features=sentiment&return_analyzed_text=false&clean=true&fallback_to_raw=true&concepts.limit=8&emotion.document=true&entities.limit=50&entities.emotion=false&entities.sentiment=false&keywords.limit=50&keywords.emotion=false&keywords.sentiment=false&relations.model=en-news&semantic_roles.limit=50&semantic_roles.entities=false&semantic_roles.keywords=false&sentiment.document=true";
        return (firstPart+secondPart+thirdPart);
    }

    private String extractTextField(String message) throws JSONException {
        String tweetText = null;
        JSONObject tweet = new JSONObject(message);
        if (tweet.has("text")) {
            tweetText = tweet.get("text").toString();
        }
        return tweetText ;
    }

    private String parseSentimentFromResponse(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject sentiment = (JSONObject)jsonObject.get("sentiment");
        JSONObject document = (JSONObject)sentiment.get("document");
        return document.get("label").toString();
    }
}
