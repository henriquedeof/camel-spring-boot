package com.xpto.kafka.processor;

import com.xpto.kafka.model.Book;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.errorhandler.DeadLetterChannel;
import org.springframework.stereotype.Component;

@Component
public class BookProcessor implements Processor {

    // {"name":"HENRIQUE BOOK", "author":"HENRIQUE FERNANDES", "available":"true"}

    @Override
    public void process(Exchange exchange) throws Exception {

        Object body = exchange.getIn().getBody();
        //Book body = exchange.getIn().getBody(Book.class);
        System.out.println("Exchange body" + body);
        System.out.println(exchange.getIn().getHeaders()); // {CamelAwsSqsAttributes={}, CamelAwsSqsMD5OfBody=a622a2844d439b1b99571d478bd21b9e, CamelAwsSqsMessageAttributes={}, CamelAwsSqsMessageId=a4fa9b2f-9453-4e6e-b968-aeb3c6310b18, CamelAwsSqsReceiptHandle=AQEBssC27L8y4F3r03dv6LB7zTU7ABRIQDzcasOPDyVVVh5TmqpDa5hsdVjWCy4pHnq49HUqZIKQxolwostFqnTVrlah2uIcXUm5LGo9HKh5qXixjABR4SWg4qt/1/+/f1Pq6Si5wBJRRS/mbYYPfsrUQUc9lBxn1ifykrf/+WAe3m5tteigg6qaZ0fbYgQZpeZiJE8caS8IUNDCtNGTa+CsahxJpM+AvXflDcc/9Qc+8NmJz1rqg0rRcKnHz391JLgaFZFmFZvR7oWo3IGhiS5xotMS6IuIBYGMnA9lgeDtqvKysqeBLcvxoz9sA86TlXXbvTRSbQiGWp9PbeHsJAr5gOeOdtsfE9yS1Bz5MBkzFJ4FGQqhcC+81kRWBL3l4z//a0ogk4WG9k8wBm7UgQ1yvw==, CamelRedelivered=true, CamelRedeliveryCounter=4, CamelRedeliveryMaxCounter=4}
        throw new Exception("----- MEU LIVRO EXCEPTION -----");
        //DeadLetterChannel  d = new DeadLetterChannel();

        // If I want a processor that handles failures: https://camel.apache.org/manual/exception-clause.html#_using_a_processor_as_a_failure_handler
//        onException(MyFunctionalException.class)
//                .process(new MyFunctionFailureHandler())
//                ...
//        ---- On the processor class
//        Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
//        assertNotNull(caused);
//        here you can do what you want, but Camel regards this exception as handled, and this processor as a failure handler, so it won't do redeliveries. So this is the end of this route.


    }

}
