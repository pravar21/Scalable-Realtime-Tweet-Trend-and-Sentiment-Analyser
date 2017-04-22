/**
 * Copyright 2013 Twitter, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package com.tweettrends.pravar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class FilterStreamExample {
    private final static Semaphore sharedSemaphore = new Semaphore(1);

    public static void run(String consumerKey, String consumerSecret, String token, String secret,SimpleQueueService simpleQueueService) throws InterruptedException {
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        // add some track terms
        endpoint.trackTerms(Lists.newArrayList("modi", "India", "Trump", "New York", "English", "London", "Tuesday Motivation","Celtics","GA06"));
        endpoint.languages(Lists.newArrayList("en"));

        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
        // Authentication auth = new BasicAuth(username, password);

        // Create a new BasicClient. By default gzip is enabled.
        Client client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        // Establish a connection
        client.connect();

        while (true) {
            // Do whatever needs to be done with messages
            for (int msgRead = 0; msgRead < 1000; msgRead++) {
                String msg = queue.take();


                try {
                    JSONObject tweet = new JSONObject(msg);
                    if (tweet.has("coordinates")) {
                        String geoInfo = tweet.get("coordinates").toString();
                        if (!geoInfo.equals("null")) {
                            simpleQueueService.sendMessage(msg);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //client.stop();

        }
    }

    public static void main(String[] args) {
        final SimpleQueueService simpleQueueService = new SimpleQueueService();
        final List<Future<String>> results = new ArrayList<Future<String>>();
        final HashMap<Future<String>,Message> map = new HashMap<Future<String>,Message>(100);
        SimpleNotificationService simpleNotificationService = new SimpleNotificationService();
        simpleNotificationService.subscribeToTopic();

        try {
            //simpleQueueService.createQueue();
            simpleQueueService.listQueues();
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FilterStreamExample.run("9sB7Y7zyxFTgEpk87ZwuZMFZR", "TPvVJJ09FhQeduDR10xJw8t5LJ4i75uu6GYQefVtHt7ebUTgZi",
                            "840399362987560960-MTKPBj2U67boTVP4ug6LWiUdvksF0gO", "adanfdOhMgPmil1TsWpD1vKvfdY6ErRVX2xCqPS6NgaEF", simpleQueueService);
                }catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }).start();

        ExecutorService pool = Executors.newCachedThreadPool();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Entered runnable");
                while (true) {
                    Iterator<Future<String>> iterator = results.iterator();

                    try {
                        sharedSemaphore.acquire();
                        while (iterator.hasNext()){
                            Future<String> result = iterator.next();
                            String sentiment = result.get();
                            Message message = map.get(result);
                            // send sentiment and message to SNS
                            System.out.println("Notifying SNS");
                            //simpleNotificationService.publishToTopic(message.getBody());
                            iterator.remove();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }finally {
                        sharedSemaphore.release();
                    }

                }
            }
        });
        thread.start();

        while(true) {
            List<Message> messages = simpleQueueService.receiveMessages();
            if(messages!=null) {
                System.out.println("Received Messages!");

                for(Message msg : messages){
                    System.out.println("Message body : "+msg.getBody());
                }
                simpleQueueService.deleteMessages(messages);

                for (Message message : messages) {
                    Worker worker = new Worker(message);
                    Future<String> result = pool.submit(worker);

                    try {
                        sharedSemaphore.acquire();
                        results.add(result);
                        map.put(result,message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        sharedSemaphore.release();
                    }

                }
            }
        }

    }
}
