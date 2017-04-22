package com.tweettrends.pravar;

import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the
 * AWS SDK for Java.
 *
 * Prerequisites: You must have a valid Amazon Web
 * Services developer account, and be signed up to use Amazon SQS. For more
 * information about Amazon SQS, see http://aws.amazon.com/sqs.
 *
 * Fill in your AWS access credentials in the provided credentials file
 * template, and be sure to move the file to the default location
 * (~/.aws/credentials) where the sample code will load the credentials from.
 *
 * WARNING: To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
/**
 * Created by PRAVAR on 18-04-2017.
 */
public class SimpleQueueService {
    private final AWSCredentials credentials;
    private final AmazonSQS sqs;
    private String tweetQueueUrl;

    public SimpleQueueService() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        try {
            this.credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        this.sqs = new AmazonSQSClient(this.credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usEast1);
    }

    public void createQueue(){
        System.out.println("Creating a new SQS queue called TweetQueue.\n");
        CreateQueueRequest createQueueRequest = new CreateQueueRequest("TweetQueue");
        tweetQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
    }

    public void listQueues(){
        System.out.println("Listing all queues in your account.\n");
        for (String queueUrl : sqs.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queueUrl);
            tweetQueueUrl = queueUrl;
        }
    }

    public void sendMessage(String msg){
        System.out.println("Sending a message to TweetQueue.\n");
        sqs.sendMessage(new SendMessageRequest(tweetQueueUrl, msg));
    }

    public List<Message> receiveMessages(){
        //System.out.println("Receiving messages from TweetQueue.\n");
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(tweetQueueUrl);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            System.out.println("  Message");
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
            for (Entry<String, String> entry : message.getAttributes().entrySet()) {
                System.out.println("  Attribute");
                System.out.println("    Name:  " + entry.getKey());
                System.out.println("    Value: " + entry.getValue());
            }
        }
        return messages.size()!=0?messages:null;
    }

    public void deleteMessages(List<Message> messages){
        System.out.println("Deleting messages.\n");
        for(Message message:messages) {
            String messageReceiptHandle = message.getReceiptHandle();
            sqs.deleteMessage(new DeleteMessageRequest(tweetQueueUrl, messageReceiptHandle));
        }
    }

    public void deleteQueue(){
        System.out.println("Deleting the test queue.\n");
        sqs.deleteQueue(new DeleteQueueRequest(tweetQueueUrl));
    }

}
