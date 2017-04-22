package Sample;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Notification {
	private static final Log log = LogFactory.getLog(Notification.class);
	@JsonProperty("Message")
	private String message;
	@JsonProperty("MessageAttributes")
	private Object messageAttributes;
	@JsonProperty("MessageId")
	private String messageId;
	@JsonProperty("Signature")
	private String signature;
	@JsonProperty("SignatureVersion")
	private String signatureVersion;
	@JsonProperty("SigningCertURL")
	private String signingCertURL;
	@JsonProperty("Subject")
	private String subject;
	@JsonProperty("SubscribeURL")
	private String subscribeURL;
	@JsonProperty("Timestamp")
	private String timestamp;
	@JsonProperty("Token")
	private String token;
	@JsonProperty("TopicArn")
	private String topicArn;
	@JsonProperty("Type")
	private String type;
	@JsonProperty("UnsubscribeURL")
	private String unsubscribeURL;
	private boolean autoSubscribe;
	private PublicKey publicKey;

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}

	public String getTopicArn() {
		return topicArn;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setSubscribeURL(String subscribeURL) {
		this.subscribeURL = subscribeURL;
	}

	public String getSubscribeURL() {
		return subscribeURL;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setSignatureVersion(String signatureVersion) {
		this.signatureVersion = signatureVersion;
	}

	public String getSignatureVersion() {
		return signatureVersion;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setSigningCertURL(String signingCertURL) {
		this.signingCertURL = signingCertURL;
	}

	public String getSigningCertURL() {
		return signingCertURL;
	}

	public void setUnsubscribeURL(String unsubscribeURL) {
		this.unsubscribeURL = unsubscribeURL;
	}

	public String getUnsubscribeURL() {
		return unsubscribeURL;
	}

	public void setMessageAttributes(Object messageAttributes) {
		this.messageAttributes = messageAttributes;
	}

	public Object getMessageAttributes() {
		return messageAttributes;
	}

	public static Notification makeMessage(String json)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, Notification.class);
	}

	public void processMessage(boolean checkSignature) {
		// The signature is based on SignatureVersion 1.
		// If the sig version is something other than 1,
		// throw an exception.
		if (checkSignature) {
			if (getSignatureVersion().equals("1")) {
				// Check the signature and throw an exception if the signature
				// verification fails.
				if (isMessageSignatureValid())
					log.info(">>Signature verification succeeded");
				else {
					log.info(">>Signature verification failed");
					throw new SecurityException(
							"Signature verification failed.");
				}
			} else {
				log.info(">>Unexpected signature version. Unable to verify signature.");
				throw new SecurityException(
						"Unexpected signature version. Unable to verify signature.");
			}
		}
		// Process the message based on type.
		if (getType().equals("Notification")) {
			// TODO: Do something with the Message and Subject.
			// Just log the subject (if it exists) and the message.
			String logMsgAndSubject = ">>Notification received from topic "
					+ getTopicArn();
			if (getSubject() != null)
				logMsgAndSubject += " Subject: " + getSubject();
			logMsgAndSubject += " Message: " + getMessage();
			log.info(logMsgAndSubject);
		} else if (getType().equals("SubscriptionConfirmation")) {
			// TODO: You should make sure that this subscription is from the
			// topic you expect. Compare topicARN to your list of topics
			// that you want to enable to add this endpoint as a subscription.
			// Confirm the subscription by going to the subscribeURL location
			// and capture the return value (XML message body as a string)
			Scanner sc;
			StringBuilder sb = new StringBuilder();
			try {
				sc = new Scanner(new URL(getSubscribeURL()).openStream());
				while (sc.hasNextLine()) {
					sb.append(sc.nextLine());
				}
				sc.close();
			} catch (IOException e) {
				log.error("Could not confirm subsription for " + getTopicArn());
				// throw new IOException(e);
			}
			log.info(">>Subscription confirmation (" + getSubscribeURL()
					+ ") Return value: " + sb.toString());
			// TODO: Process the return value to ensure the endpoint is
			// subscribed.
		} else if (getType().equals("UnsubscribeConfirmation")) {
			// TODO: Handle UnsubscribeConfirmation message.
			// For example, take action if unsubscribing should not have
			// occurred.
			// You can read the SubscribeURL from this message and
			// re-subscribe the endpoint.
			log.info(">>Unsubscribe confirmation: " + getMessage());
		} else {
			// TODO: Handle unknown message type.
			log.info(">>Unknown message type.");
		}
		log.info(">>Done processing message: " + getMessage());
	}

	public Notification() {
	}

	public Notification(String jsonString) {
		setAllValues(jsonString);
	}

	public void setAllValues(String jsonString) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, String> map = mapper.readValue(jsonString,
					new TypeReference<HashMap<String, String>>() {
					});
			this.setType(map.get("Type"));
			this.setMessageId(map.get("MessageId"));
			this.setToken(map.get("Token"));
			this.setTopicArn(map.get("TopicArn"));
			this.setSubject(map.get("Subject"));
			this.setMessage(map.get("Message"));
			this.setSubscribeURL(map.get("SubscribeURL"));
			this.setTimestamp(map.get("Timestamp"));
			this.setSignatureVersion(map.get("SignatureVersion"));
			this.setSignature(map.get("Signature"));
			this.setSigningCertURL(map.get("SigningCertURL"));
			this.setUnsubscribeURL(map.get("UnsubscribeURL"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PublicKey getSigningPublicKey() {
		if (publicKey == null)
			try {
				Pattern snsCertURL = Pattern
						.compile("(?:https)://(sns.[a-zA-Z0-9-]+.(?:amazon(?:aws)?.com(?:.cn)?))/.*");
				Matcher matcher = snsCertURL.matcher(getSigningCertURL());
				if (!matcher.matches())
					throw new IllegalArgumentException(
							"SigningCertURL is not from SNS");
				URL url = new URL(getSigningCertURL());
				InputStream inStream = url.openStream();
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate cert = (X509Certificate) cf
						.generateCertificate(inStream);
				inStream.close();
				publicKey = cert.getPublicKey();
			} catch (Exception e) {
				throw new SecurityException("Verify method failed.", e);
			}
		return publicKey;
	}

	public void setSigningPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public boolean isMessageSignatureValid(PublicKey pubKey) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(pubKey);
			sig.update(getMessageBytesToSign());
			return sig.verify(Base64.decodeBase64(getSignature().getBytes()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}

	public boolean isMessageSignatureValid() {
		return isMessageSignatureValid(getSigningPublicKey());
	}

	public void subscribeHttp() throws IOException {
		if (subscribeURL == null)
			throw new IllegalArgumentException("No SubsribeURL in SNSMessage");
		URL url = new URL(subscribeURL);
		// log.info("subscribeURL " + subscribeURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		// int statusCode = conn.getResponseCode();
		InputStream is;
		try {
			is = conn.getInputStream();
		} catch (Exception e) {
			is = conn.getErrorStream();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String input;
		StringBuffer sb = new StringBuffer();
		while ((input = br.readLine()) != null) {
			sb.append(input);
		}
		snsXmlResponse(sb.toString());
	}

	private String snsXmlResponse(String xml) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			xml = "<?xml version=\"1.0\"?>" + xml;
			Document doc = dBuilder.parse(new InputSource(
					new ByteArrayInputStream(xml.getBytes("utf-8"))));
			String responseType = doc.getDocumentElement().getNodeName();
			System.out.println(responseType);
			NodeList nList;
			String subscriptionArn = null;
			String requestId = null;
			try {
				nList = doc.getElementsByTagName("SubscriptionArn");
				subscriptionArn = nList.item(0).getFirstChild().getNodeValue();
			} catch (NullPointerException e) {
			}
			System.out.println("SubscriptionArn " + subscriptionArn);
			nList = doc.getElementsByTagName("RequestId");
			requestId = nList.item(0).getFirstChild().getNodeValue();
			System.out.println("RequestId " + requestId);
			return requestId;
		} catch (Exception e) {
			return null;
		}
	}

	private byte[] getMessageBytesToSign() {
		byte[] bytesToSign = null;
		if (getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign().getBytes();
		else if (getType().equals("SubscriptionConfirmation")
				|| getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign().getBytes();
		return bytesToSign;
	}

	// Build the string to sign for Notification messages.
	private String buildNotificationStringToSign() {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += getMessageId() + "\n";
		if (getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += getType() + "\n";
		return stringToSign;
	}

	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	private String buildSubscriptionStringToSign() {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += getSubscribeURL() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += getType() + "\n";
		return stringToSign;
	}
}
