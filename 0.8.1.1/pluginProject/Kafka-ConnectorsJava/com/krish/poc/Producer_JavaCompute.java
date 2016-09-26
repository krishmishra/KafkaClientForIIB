package com.krish.poc;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;



public class Producer_JavaCompute extends MbJavaComputeNode {
	private static kafka.javaapi.producer.Producer<String, String> producer;
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		String broker = "localhost:9092";
		String serializer = "kafka.serializer.StringEncoder";
		String topic = "test";
		String outputmsg = "";
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);

			MbElement env = inAssembly.getGlobalEnvironment().getRootElement();
			MbElement brokerList = env.getFirstElementByPath("brokerList");			
			
			if (brokerList == null ) {
				broker = getUserDefinedAttribute("brokerList").toString();
			}
			else{
				broker = brokerList.getValueAsString();
			}


			MbElement Serializer = env
					.getFirstElementByPath("Serializer");
			 
			if (Serializer == null ) {
				serializer = "kafka.serializer.StringEncoder";
			}
			else{
				serializer = Serializer.getValueAsString();
			}
			
			MbElement Topic = env.getFirstElementByPath("topic");
			
			if (Topic == null ) {
				topic = getUserDefinedAttribute("topic").toString();
			}
			else{
				topic = Topic.getValueAsString();
			}
			MbElement Message = env.getFirstElementByPath("Message");
			
			if (Message == null ) {
				 				
				byte [] inputMsgAsBytes = inMessage.getRootElement().getLastChild().toBitstream(null,null,null,0,1208,0);
				outputmsg  = new String(inputMsgAsBytes);
			}
			else{
				outputmsg = Message.getValueAsString();
			}

			Properties properties = new Properties();
			properties.setProperty("metadata.broker.list", broker);
			properties.setProperty("serializer.class", serializer);
			properties.put("request.required.acks", "1");


			producer = new kafka.javaapi.producer.Producer<String, String>(
					new ProducerConfig(properties));

			try {

			
				KeyedMessage<String, String> data = new KeyedMessage<String, String>(
						topic, outputmsg);

				producer.send(data);	
				
			} catch (Throwable throwable) {
				producer.close();
			}

		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be
			// handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		producer.close();
		out.propagate(outAssembly);
	}

}
