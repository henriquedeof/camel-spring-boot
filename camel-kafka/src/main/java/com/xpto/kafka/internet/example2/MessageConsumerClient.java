/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xpto.kafka.internet.example2;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageConsumerClient {

	// EXAMPLE FROM: https://github.com/godbolerr/camel-example-kafka

	private static final Logger LOG = LoggerFactory.getLogger(MessageConsumerClient.class);

	private MessageConsumerClient() {
	}

	public static void main(String[] args) throws Exception {

		LOG.info("About to run Kafka-camel integration...");

		CamelContext camelContext = new DefaultCamelContext();

		// Add route to send messages to Kafka

		camelContext.addRoutes(new RouteBuilder() {
			public void configure() {
				//PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class); // Original implementation
				PropertiesComponent pc = getContext().getComponent(null, null);
				pc.setLocation("classpath:application.properties");

				log.info("About to start route: Kafka Server -> Log ");

				from("kafka:{{kafka.host}}:{{kafka.port}}?" +
						"topic={{consumer.topic}}&" +
						"maxPollRecords={{consumer.maxPollRecords}}&" +
						"consumersCount={{consumer.consumersCount}}&" +
						"seekToBeginning={{consumer.seekToBeginning}}&" +
						"groupId={{consumer.group}}")
						.routeId("FromKafka").log("${body}");
			}
		});
		camelContext.start();

		// let it run for 5 minutes before shutting down
		Thread.sleep(5 * 60 * 1000);

		camelContext.stop();
	}

}
