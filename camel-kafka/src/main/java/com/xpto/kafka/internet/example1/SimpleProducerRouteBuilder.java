package com.xpto.kafka.internet.example1;

import org.apache.camel.builder.RouteBuilder;

public class SimpleProducerRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		// This is a Producer example

		String topicName = "topic=javainuse-topic";
		String kafkaServer = "kafka:localhost:9092";
		String zooKeeperHost = "zookeeperHost=localhost&zookeeperPort=2181";
		String serializerClass = "serializerClass=kafka.serializer.StringEncoder";

		String toKafka = new StringBuilder().append(kafkaServer).append("?").append(topicName).append("&")
				.append(zooKeeperHost).append("&").append(serializerClass).toString();

		from("file:C:/inbox?noop=true").split().tokenize("\n").to(toKafka);
	}
}