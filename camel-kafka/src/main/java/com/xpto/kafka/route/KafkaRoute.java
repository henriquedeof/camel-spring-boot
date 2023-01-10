package com.xpto.kafka.route;

import com.xpto.kafka.model.Book;
import com.xpto.kafka.processor.BookProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.sqs.Sqs2Constants;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JacksonXMLDataFormat;
import org.apache.camel.spi.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class KafkaRoute extends RouteBuilder {
    // {"name":"HENRIQUE BOOK", "author":"HENRIQUE FERNANDES", "available":"true"}
    //private static final String PATH = "camel-kafka/files/input";

    @Autowired
    private BookProcessor bookProcessor;

    @Qualifier("jsonKafkaListenerContainerFactory")
    @Autowired
    private ConcurrentKafkaListenerContainerFactory jsonKafkaListenerContainerFactory;


    @Override
    public void configure() throws Exception {

// ============================================== EXTRA NOTES ============================================================================================
        // NOTE: STATE OF REDELIVERY AS MESSAGE HEADERS => https://camel.apache.org/components/3.20.x/eips/dead-letter-channel.html#_state_of_redelivery_as_message_headers
        // NOTE: Error handler (includes DLQ): https://camel.apache.org/manual/error-handler.html#_scopes
        // NOTE: More about exception handling (Handles onException and errorHandler(deadLetterChannel()) at the same time): https://camel.apache.org/manual/exception-clause.html#_example_using_handled

        // ***** NOTE: All redelivery attempts start at the point of the failure. So the route: https://camel.apache.org/manual/exception-clause.html#_point_of_entry_for_redelivery_attempts *****
                // onException(ConnectException.class)
                //    .from("direct:start")
                //    .process("processor1")
                //    .process("processor2") // <--- throws a ConnectException
                //    .to("mock:theEnd")
                // ***** NOTE: Will retry from processor2 - not the complete route. *****



        // The exchange is sent to the DLQ.
//        errorHandler(deadLetterChannel("activemq:queue:pedidos.DLQ")  // I can also call another route. Example: errorHandler(deadLetterChannel("direct:dead") ... from("direct:dead").log().to("foo:bar")
//                .onPrepareFailure(null) // Before the exchange is sent to the dead letter queue, you can use onPrepare to allow a custom Processor to prepare the exchange, such as adding information why the Exchange failed.
//                                        // https://camel.apache.org/components/3.20.x/eips/dead-letter-channel.html#_calling_a_processor_before_sending_message_to_the_dead_letter_queue_using_onpreparefailure
//
//            )
//        ;
// ========================================================================================================================================================

//        onException(Exception.class) // I can have multiple exceptions:  MyBusinessException.class, onException(Exception.class)
//                .maximumRedeliveries(2) // max attempts. Default value is 0.
//                .redeliveryDelay(2000L) // retry delay
//                .maximumRedeliveryDelay(1000L) // retry maximum delay
//                .handled(true) // Remove the message that has problem out of the route. So it will not propagate to the consumer.
//                                    // It is also removed from the exchange, otherwise, upper routes could see these errors. This way there is no failure returned to client.

                //.onWhen(header("user").isNotNull()) // Here we define our onException to catch Exception when there is a header[user] on the exchange that is not null. NOTE: SEE THE NOTES.txt FILE
                //.useOriginalMessage() // Is used for routing the original input message instead of the current message that potential is modified during routing.
                //.transform().simple("Error reported: ${exception.message} - cannot process this message.");  OR  .transform(exceptionMessage());  OR  .transform().constant("Sorry");
                //.delayPattern("5:1000;10:5000;20:20000") // Redelivery attempt number:  1..4 = 0 millis, 5..9 = 1000 millis, 10..19 = 5000, 20.. = 20000 millis
                //.backOffMultiplier(2) // ???
                //.onExceptionOccurred() // call a custom processor right after an exception was thrown, and the DLQ is about to decide what to do (either to schedule a redelivery, or move the message into the DLQ).
                //.onRedelivery() // When the Dead Letter Channel is doing redeliver its possible to configure a Processor that is executed JUST BEFORE every redelivery attempt
                //.bean(SomeService.class, "orderFailed") // This bean handles the error handling where we can customize the error response using java code.
                //.to("")
        ;
// ========================================================================================================================================================

//        Registry registry = getCamelContext().getRegistry();
//        CamelContext camelContext = getCamelContext();
//        registry.bind("jsonKafkaListenerContainerFactory", ConcurrentKafkaListenerContainerFactory.class);
//        //((org.apache.camel.impl.DefaultCamelContext) camelContext).setRegistry(registry);
//        ((DefaultCamelContext) camelContext).setRegistry(registry);


        errorHandler(deadLetterChannel("aws2-sqs://arn:aws:sqs:ap-southeast-2:420456207741:books-dead-letter-queue?amazonSQSClient=#sqsClient")
                .log("====== ENTRADA DO ERROR HANDLER ======")
                .maximumRedeliveries(4) // max attempts. Default value is 0. On AWS this field relates to maxReceiveCount.
                .redeliveryDelay(2000L) // retry delay
                .maximumRedeliveryDelay(2000L) // retry maximum delay
                .log("====== SAIDA DO ERROR HANDLER ======")
        );

        // requestTimeoutMs (producer), retries(producer),
        // To read many topics: from("kafka:books,magazines,audiobooks?brokers=localhost:9092")
        from("kafka:books?brokers=localhost:29092").routeId("kafka-books-route")
                //.filter(simple())
                .log("[${header.kafka.OFFSET}] [${body}]")

                //================== working with String ==================
                .choice().when(simple("${body} contains 'available'"))// ANOTHER EXAMPLE: .when(simple("${header.amount} > 1000")).to("activemq:bigspender")
                    .log("============= MY BODY CONTAINS AVAILABLE =============")
                .end()

                .choice().when(body().contains("available")) //I could use .simple("${body.available} == true") if I do .unmarshal(new JacksonDataFormat(Book.class))
                        .log("============= MY BODY IS TRUE =============")
                .end()

                .choice()
                .when(body().contains("\"available\":\"false\""))
                    .log("============= MY BODY IS FALSE =============")
                .end()
                //================== END of working with String ==================


//==========================================================================================================================================================================================================
//                All the lines below works if I transform the JSON String that came from Kafka, or any other source, into Book.class. This I set Body with this object
//                .unmarshal(new JacksonDataFormat(Book.class))
//                .choice()
//                .when(simple("${body.available} == 'true'"))//.simple("${body.available} == true") // ANOTHER EXAMPLE: .when(simple("${header.amount} > 1000")).to("activemq:bigspender")
//                .log("============= MY BODY CONTAINS AS TRUE =============")
//                .end()
//                .log("${body.name}")
//                .log("${body.author}")
//                .log("${body.available}")
//==========================================================================================================================================================================================================


                //.log("[${headers}]") // [{CamelMessageTimestamp=1673311875659, kafka.HEADERS=RecordHeaders(headers = [], isReadOnly = false), kafka.KEY=, kafka.OFFSET=25, kafka.PARTITION=0, kafka.TIMESTAMP=1673311875659, kafka.TOPIC=books}]
                //.unmarshal(new JacksonDataFormat(Book.class)) // When unmarshal() is called, it also sets the body. Example how to use unmarshal: https://www.baeldung.com/java-camel-jackson-json-array
                //.log("${body}") // Result is com.xpto.kafka.model.Book@2c7eb5af because I do not have toString() implemented.
//                .process(bookProcessor)
//                .setBody().method()
                //.to("aws2-sqs://books-queue?accessKey=RAW(AKIAWDZJESV6XKCDJWFQ)&secretKey=RAW(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)&region=ap-southeast-2")
                //.to("aws2-sqs://arn:aws:sqs:ap-southeast-2:420456207741:books-queue?amazonSQSClient=#sqsClient")
        ;

        // deleteAfterRead=false
        from("aws2-sqs://arn:aws:sqs:ap-southeast-2:420456207741:books-queue?amazonSQSClient=#sqsClient").routeId("sqs-books-route")
                //.setProperty(Sqs2Constants.SQS_DELETE_FILTERED, constant(true))
//                .log("[${header.CamelAwsSqsAttributes}]") // empty
//                .log("[${header.CamelAwsSqsMessageAttributes}]") // empty
//                .log("[${header.CamelAwsSqsMD5OfBody}]") // a622a2844d439b1b99571d478bd21b9e
//                .log("[${header.CamelAwsSqsMessageId}]") // 98fa7050-bb9e-4c1c-bd7e-18d6d9af4086
//                .log("[${header.CamelAwsSqsReceiptHandle}]") // AQEBlx3MImBEJaOae3eEfmyR7DS0/KQ/I7CRE9g3+evHDPJ1ibOxkM+9O79ppxtSecLuD4rHMWhQ5/AxsQOtykioSbuirkzdjEh21vOC3aNMkzVpnWGVQrWtW7vnoKtGHN/iej8H132IJ35oh+gKL37f+FQ4GKlKANIrgNqkXWiaE5tHqnUwVXT/QaRpchgDZY0LkcqY3bZMggyKPDx/fPGjp08V9SgBiOhZuR2b8SMcPrPqbsXKuVROXEoafefccAsBtPWDOgJwKf7bZRvQiHadmiJBRfkSBqfYEVzkHRW/3mpyPRG3qPpDllnr7Y1Vh40RTDrlCAivss6Y7b+4oNz8vHYdfKIhISBnVilcw59vcsRrYe9fCVP5IVTHdWiUcHzYtzRU0fEzHZyVp2MU26sonA==
//                .log("[${header.CamelAwsSqsDelaySeconds}]") // empty
//                .log("[${header.CamelAwsSqsPrefix}]") // empty
//                .log("[${header.CamelAwsSqsOperation}]") // empty
//                .log("[${header.CamelAwsSqsDeleteFiltered}]") // ?????   ==> Sqs2Constants.SQS_DELETE_FILTERED
//                .log("[${headers}]") // [{CamelAwsSqsAttributes={}, CamelAwsSqsMD5OfBody=a622a2844d439b1b99571d478bd21b9e, CamelAwsSqsMessageAttributes={}, CamelAwsSqsMessageId=a4fa9b2f-9453-4e6e-b968-aeb3c6310b18, CamelAwsSqsReceiptHandle=AQEBssC27L8y4F3r03dv6LB7zTU7ABRIQDzcasOPDyVVVh5TmqpDa5hsdVjWCy4pHnq49HUqZIKQxolwostFqnTVrlah2uIcXUm5LGo9HKh5qXixjABR4SWg4qt/1/+/f1Pq6Si5wBJRRS/mbYYPfsrUQUc9lBxn1ifykrf/+WAe3m5tteigg6qaZ0fbYgQZpeZiJE8caS8IUNDCtNGTa+CsahxJpM+AvXflDcc/9Qc+8NmJz1rqg0rRcKnHz391JLgaFZFmFZvR7oWo3IGhiS5xotMS6IuIBYGMnA9lgeDtqvKysqeBLcvxoz9sA86TlXXbvTRSbQiGWp9PbeHsJAr5gOeOdtsfE9yS1Bz5MBkzFJ4FGQqhcC+81kRWBL3l4z//a0ogk4WG9k8wBm7UgQ1yvw==}]
                .log("[${body}]")


//                .process(bookProcessor)
//                .log("================ APOS SAIDA DA EXCEPTION ==============")
                //.throwException(new IllegalArgumentException("Forced")) // I can use this to test the Error Handling and DLQ
        ;

        // DEAD LETTER QUEUE NAME CREATED: books-dead-letter-queue
//        from("aws2-sqs://arn:aws:sqs:ap-southeast-2:420456207741:books-dead-letter-queue?amazonSQSClient=#sqsClient")
//                .log("===== FETCHING DATA FROM DQL =====")
//                .to("aws2-sqs://arn:aws:sqs:ap-southeast-2:420456207741:books-queue?amazonSQSClient=#sqsClient")
//                .log("===== MESSAGE SENT TO MAIN QUEUE =====")
//        ;


// ==============================================================================================================================================================================


//        RANDOM EXAMPLE OF CONNECTING
//        from("kafka:{{consumer.topic}}?brokers={{kafka.host}}:{{kafka.port}}" + "&consumersCount={{consumer.consumersCount}}" + "&seekTo={{consumer.seekTo}}" + "&groupId={{consumer.group}}")


        // PRODUCING MESSAGES TO KAFKA
//        from("direct:start")
//                .setBody(constant("Message from Camel"))          // Message to send
//                .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
//                .to("kafka:test?brokers=localhost:9092");

// How to send message attributes along with message body on AWS sqs using Camel?  =  https://stackoverflow.com/questions/57972024/how-to-send-message-attributes-along-with-message-body-on-aws-sqs-using-camel
//        from("direct:send-to-sqs")
//                .setBody(simple("${exchangeProperty.ENTITY_JSON}"))
//                //.setProperty("systemName",simple("FINANCE")) // IMPLEMENTATION WITH ERROR
//                .setHeader("systemName",simple("FINANCE")) // CORRECT IMPLEMENTATION
//                .log("body which is to be send to sqs is ${body}")
//                .to("aws-sqs://{my sqs url}:QueueForPOC?" + "amazonSQSClient=#sqsClient&attributeNames=#systemName")
//                .log("entity has been sent to SQS.");

    }
}