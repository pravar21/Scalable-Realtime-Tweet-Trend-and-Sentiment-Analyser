package Sample;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by PRAVAR on 22-04-2017.
 */
public class HelloWorld extends HttpServlet{
    private PublicKey publicKey;
    public static String getMessage() {
        return "Hello, world";
    }

    /*protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException{
        //Get the message type header.
        String messagetype = request.getHeader("x-amz-sns-message-type");
        //If message doesn't have the message type header, don't process it.
        if (messagetype == null)
            return;

        // Parse the JSON message in the message body
        // and hydrate a Message object with its contents
        // so that we have easy access to the name/value pairs
        // from the JSON message.
        Scanner scan = new Scanner(request.getInputStream());
        StringBuilder builder = new StringBuilder();
        while (scan.hasNextLine()) {
            builder.append(scan.nextLine());
        }
        Notification msg = readMessageFromJson(builder.toString());

        // The signature is based on SignatureVersion 1.
        // If the sig version is something other than 1,
        // throw an exception.
        if (msg.getSignatureVersion().equals("1")) {
            // Check the signature and throw an exception if the signature verification fails.
            if (isMessageSignatureValid(msg))
                log.info(">>Signature verification succeeded");
            else {
                log.info(">>Signature verification failed");
                throw new SecurityException("Signature verification failed.");
            }
        }
        else {
            log.info(">>Unexpected signature version. Unable to verify signature.");
            throw new SecurityException("Unexpected signature version. Unable to verify signature.");
        }

        // Process the message based on type.
        if (messagetype.equals("Notification")) {
            //TODO: Do something with the Message and Subject.
            //Just log the subject (if it exists) and the message.
            String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
            if (msg.getSubject() != null)
                logMsgAndSubject += " Subject: " + msg.getSubject();
            logMsgAndSubject += " Message: " + msg.getMessage();
            log.info(logMsgAndSubject);
        }
        else if (messagetype.equals("SubscriptionConfirmation"))
        {
            //TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics
            //that you want to enable to add this endpoint as a subscription.

            //Confirm the subscription by going to the subscribeURL location
            //and capture the return value (XML message body as a string)
            Scanner sc = new Scanner(new URL(msg.getSubscribeURL()).openStream());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
            }
            log.info(">>Subscription confirmation (" + msg.getSubscribeURL() +") Return value: " + sb.toString());
            //TODO: Process the return value to ensure the endpoint is subscribed.
        }
        else if (messagetype.equals("UnsubscribeConfirmation")) {
            //TODO: Handle UnsubscribeConfirmation message.
            //For example, take action if unsubscribing should not have occurred.
            //You can read the SubscribeURL from this message and
            //re-subscribe the endpoint.
            log.info(">>Unsubscribe confirmation: " + msg.getMessage());
        }
        else {
            //TODO: Handle unknown message type.
            log.info(">>Unknown message type.");
        }
        log.info(">>Done processing message: " + msg.getMessageId());
    }*/

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException{
        //Get the message type header.
        String messagetype = request.getHeader("x-amz-sns-message-type");
        //If message doesn't have the message type header, don't process it.
        if (messagetype == null)
            return;

        // Parse the JSON message in the message body
        // and hydrate a Message object with its contents
        // so that we have easy access to the name/value pairs
        // from the JSON message.
        Scanner scan = new Scanner(request.getInputStream());
        StringBuilder builder = new StringBuilder();
        while (scan.hasNextLine()) {
            builder.append(scan.nextLine());
        }

        Notification notification = new Notification(builder.toString());
        notification.processMessage(false);
    }

    /*private static Notification readMessageFromJson(String json) throws IOException
    {
        System.out.println("readMessageFromJson");
        Notification m = new Notification();

        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createParser(json);

        jp.nextToken();
        while (jp.nextToken() != JsonToken.END_OBJECT)
        {
            String name = jp.getCurrentName();
            jp.nextToken();
            if ("Type".equals(name))
                m.setType(jp.getText());
            else if ("Message".equals(name))
                m.setMessage(jp.getText());
            else if ("MessageId".equals(name))
                m.setMessageId(jp.getText());
            else if ("SubscribeURL".equals(name))
                m.setSubscribeURL(jp.getText());
            else if ("UnsubscribeURL".equals(name))
                m.setUnsubscribeURL(jp.getText());
            else if ("Subject".equals(name))
                m.setSubject(jp.getText());
            else if ("Timestamp".equals(name))
                m.setTimestamp(jp.getText());
            else if ("TopicArn".equals(name))
                m.setTopicArn(jp.getText());
            else if ("Token".equals(name))
                m.setToken(jp.getText());
            else if ("Signature".equals(name))
                m.setSignature(jp.getText());
            else if ("SignatureVersion".equals(name))
                m.setSignatureVersion(jp.getText());
            else if ("SigningCertURL".equals(name))
                m.setSigningCertURL(jp.getText());

        }
        System.out.println("readMessageFromJson-End");
        return m;
    }*/


}
