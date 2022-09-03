package com.xpto.direct.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DirectRoute extends RouteBuilder {

    private static final String PATH = "direct/files/input";

    @Override
    public void configure() throws Exception {
        // Direct: Basically realises the communication between components and/or routes.
        // It is synchronous. If I need asynchronous I need to use the SEDA component.

        from("file:" + PATH )
                .to("direct:log-file");

        from("direct:log-file")
                .log("Log: ${header.CamelFileName}")
                .process(new FileProcessor());
    }
}

class FileProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        File file = exchange.getIn().getBody(File.class);
        System.out.println("My FileProcessor is: " + file.getName());
    }
}
