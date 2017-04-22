package com.tweettrends.pravar;

import com.amazonaws.services.sqs.model.Message;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by PRAVAR on 18-04-2017.
 */
class Worker implements Callable<String> {
    private final Message message;

    public Worker(Message message) {
        this.message = message;
    }

    @Override

    public String call() throws Exception {

            //extract text from message

            //send text to alchemy and return result
        SentimentAnalysis sentimentAnalysis = new SentimentAnalysis();
        String sentiment = sentimentAnalysis.analyzeSentiment(message.getBody());

        return sentiment;
    }
}
