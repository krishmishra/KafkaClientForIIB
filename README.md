# KafkaClientForIIB

This project is pugin project to create custom plugin for IIB (Websphere message broker) to publish message to kafka topic and consume message from kafka topic. For Kafka version 0.8.0.1 is this plugin is developed later version will be released soon. 

Steps to get Plugin working in IIB tool kit.

method 1: Deploying dependent jar with project
1) place  KafkaClientForIIB/0.8.1.1/plugInJar/Kafka-Connectors_1.0.0.201608241529.jar in C:\Program Files (x86)\IBM\IntegrationToolkit90\plugins folder.

2) Create libray and add jars from KafkaClientForIIB/0.8.1.1/sharedLib/

3) import KafkaClientForIIB/0.8.1.1/testProject/ 

4) add Library to project reference, Change node peoperties eg. kafaka server addressport and etc and deploy.


method 2:n Place dependent Jar is shared directory of broker

1) place jar from KafkaClientForIIB/0.8.1.1/sharedLib/ to shared library of message broker.
a) windows C:\ProgramData\IBM\MQSI\shared-classes  b) linux run this command to get path "mqsilist <integrationNodeName>"
2) import KafkaClientForIIB/0.8.1.1/testProject/ 
3) Deploy after changing required inputs, default values are populated on node.


Publish node.

