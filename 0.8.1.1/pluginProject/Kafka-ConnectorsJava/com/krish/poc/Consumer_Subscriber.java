package com.krish.poc;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import java.util.Random;

public class Consumer_Subscriber extends MbJavaComputeNode {
	String Topic = getUserDefinedAttribute("topic").toString();

	ConsumerConnector consumerConnector;

	public static void main(String args[]) {
	}

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alt");
		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;

		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);

			Properties properties = new Properties();
			properties.put("zookeeper.connect",
					getUserDefinedAttribute("bootStrapServer").toString());
			properties.put("group.id", getUserDefinedAttribute("groupId")
					.toString());
			properties.put("zookeeper.session.timeout.ms", "6000");
			properties.put("zookeeper.connectiontimeout.ms", "120");
			properties.put("zookeeper.sync.time.ms", "2100");
			properties.put("consumer.timeout.ms", "100000");
			properties.put("auto.commit.interval.ms", "9000");
			properties.put("auto.offset.reset", "smallest");
			ConsumerConfig consumerConfig = new ConsumerConfig(properties);
			consumerConnector = Consumer
					.createJavaConsumerConnector(consumerConfig);

			if (properties.getProperty("group.id") == null) {
				properties.setProperty("group.id",
						"group-" + new Random().nextInt(100000));
			}
			Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
			topicCountMap.put(Topic, new Integer(1));
			Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector
					.createMessageStreams(topicCountMap);
			KafkaStream<byte[], byte[]> stream = consumerMap.get(Topic).get(0);
			ConsumerIterator<byte[], byte[]> it = stream.iterator();
			String myMsg = "";
			MbMessage outputXMLMbMessage = new MbMessage();
			if (!it.isEmpty()) {
				while (it.hasNext()) {
					// System.out.println(new String(it.next().message()));
					myMsg = new String(it.next().message());
					// if (myMsg.contains("<")) {
					outputXMLMbMessage.getRootElement()
							.createElementAsLastChildFromBitstream(
									myMsg.getBytes(), MbXMLNSC.PARSER_NAME,
									null, null, null, 0, 0, 0);
					MbMessage outMessage1 = new MbMessage(outputXMLMbMessage);
					outAssembly = new MbMessageAssembly(inAssembly, outMessage1);
					out.propagate(outAssembly);
					// }
				}
			}

		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;

		} catch (ConsumerTimeoutException e) {
			// No Message left at topic
			// consumerConnector.commitOffsets();
			// consumerConnector.shutdown();
			// alt.propagate(outAssembly);
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be
			// handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		} finally {
			consumerConnector.commitOffsets();
			consumerConnector.shutdown();
			// System.out.println("After closing KafkaConsumer");
		}
	}

}
