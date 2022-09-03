package com.xpto.filewatch.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class FileWatchRoute extends RouteBuilder {

    private static final String PATH = "file-watch/files/input";
    //private static final String PATH = "C:\\1-Henrique\\Desenvolvimento\\workspace\\Cursos Camel\\camel-spring-boot\\file-watch\\files\\input\\";

    @Override
    public void configure() throws Exception {
        // The file-watch component monitors the events that happen to files and folders on the monitored folder (in this example the folder is 'input').
        // By default, the file-watch component is recursive
        from("file-watch:" + PATH)
                    // recursive=false => Do not watch anything inside of subfolders.
                    // events=MODIFY => Just evaluate events that are of MODIFY type
                .log("Event happened: ${header.CamelFileEventType} - File: ${header.CamelFileName}");
    }
}
