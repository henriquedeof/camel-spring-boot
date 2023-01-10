package com.xpto.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
//@EnableKafka // Annotation just on Consumer. Producer does NOT need this annotation. If I am using Sprong Boot, then maybe I don't need this anotation
                // @SpringBootTest("spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")  for testing
@Configuration
public class ConsumerKafkaConfig { // implements Deserializer { // KafkaTopicConfig

    // Deserialization: https://stackoverflow.com/questions/55190052/how-to-use-camel-avro-consumer-producer || https://stackoverflow.com/questions/70950582/deserialize-json-with-camel-routes

    @Autowired
    private KafkaProperties kafkaProperties; // DO I NEED THIS? REMOVE FOR TESTING

//    This method may used with  org.apache.kafka.common.serialization.Deserializer
//    @Override
//    public Object deserialize(String topic, byte[] data) {
//        try {
//            if (data == null){
//                logger.info("Null received at deserializing");
//                return null;
//            }
//            return objectMapper.readValue(new String(data, "UTF-8"), MyMessage.class);
//        } catch (Exception e) {
//            logger.error("Deserialization exception: " + e.getMessage());
//        }
//        return null;
//    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory jsonKafkaListenerContainerFactory() { // NOTE: USE ConcurrentKafkaListenerContainerFactoryConfigurer FOR TESTING AS WELL.
    //public ConcurrentKafkaListenerContainerFactory<String, String> jsonKafkaListenerContainerFactory() { // Implementation to use Message Filter for Listener

        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory(); // Original implementation
        factory.setConsumerFactory(jsonConsumerFactory()); // Original implementation: Setting the Consumer Factory of this Listener

//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>(); // Implementation to use Message Filter for Listener
//        factory.setConsumerFactory(consumerFactory()); // Implementation to use Message Filter for Listener: Setting the Consumer Factory of this Listener
//        factory.setRecordFilterStrategy(record -> record.value().contains("\"available\":\"false\""));

        // ================== NOT USING deserialization using BATCH for JSON ==================
        factory.setMessageConverter(new JsonMessageConverter()); // This line does deserialization without batch

        // ================== Setting deserialization using batch for JSON ==================
        //factory.setMessageConverter(new BatchMessagingMessageConverter(new JsonMessageConverter()));
        //factory.setBatchListener(true);
        // ==================================================================================

        // Adding Message Filter for Listener

        return factory;
    }

    @Bean
    public ConsumerFactory jsonConsumerFactory() {
        Map<String, Object> configs = new HashMap();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory(configs);

        // Extras examples from the internet
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, localhost:9092);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  org.apache.kafka.common.serialization.StringDeserializer);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  org.apache.kafka.common.serialization.StringDeserializer);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }


//    ===== BAELDUNG EXAMPLE: Adding Message Filter for Listeners, where all the messages matching the filter will be discarded. https://www.baeldung.com/spring-kafka =====
//    It is a good example of using Kafka and Spring Boot in general
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> filterKafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.setRecordFilterStrategy(
//                record -> record.value().contains("World"));
//        return factory;
//    }
//
//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
//
//
//    @KafkaListener(topics = "topicName", containerFactory = "filterKafkaListenerContainerFactory")
//    public void listenWithFilter(String message) {
//        System.out.println("Received Message in filtered listener: " + message);
//    }
//
//========================================================================================================================================================================
//========================================================================================================================================================================
// ANOTHER EXAMPLE

//    @Bean(name = "kafkaListenerContainerFactory")
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConcurrency(Integer.parseInt(threads));
//        factory.setBatchListener(true);
//        factory.setConsumerFactory(kafkaConsumerFactory());
//        factory.getContainerProperties().setPollTimeout(Long.parseLong(pollTimeout));
//        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.BATCH);
//
//        if(true) {
//            factory.setRecordFilterStrategy(new RecordFilterStrategy<String, String>() {
//
//                @Override
//                public boolean filter(ConsumerRecord<String, String> consumerRecord) {
//                    if(consumerRecord.key().equals("ETEST")) {
//                        return false;
//                    }
//                    else {
//                        return true;
//                    }
//                }
//            });
//        }
//        return factory;
//    }
//    NOTE: adding to @Deadpool comments. It will work fine but it will not commit offset. so we will keep getting same message again, but it will not consume.
//    we need to set factory.setAckDiscarded(true); before setting factory.setRecordFilterStrategy() so that it will discard and commit offset.





}
