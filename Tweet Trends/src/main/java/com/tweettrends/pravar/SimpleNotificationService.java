package com.tweettrends.pravar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;

/**
 * Created by PRAVAR on 21-04-2017.
 */
public class SimpleNotificationService {

    private static final String topicArn = "arn:aws:sns:us-east-1:084177367647:Tweet-Sentiment-Analyzed";
    private final AmazonSNSClient snsClient;
    private final AWSCredentials credentials;

    public SimpleNotificationService() {
        try {
            this.credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        this.snsClient = new AmazonSNSClient(this.credentials);
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void subscribeToTopic(){
        SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", "http://192.168.0.9:8080");
        snsClient.subscribe(subRequest);

        //get request id for SubscribeRequest from SNS metadata
        System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
    }

    public void publishToTopic(String message){
        PublishRequest publishRequest = new PublishRequest(topicArn, message);
        PublishResult publishResult = snsClient.publish(publishRequest);

        //print MessageId of message published to SNS topic
        System.out.println("MessageId - " + publishResult.getMessageId());
    }
}
