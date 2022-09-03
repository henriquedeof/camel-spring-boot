package com.xpto.file.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileRoute extends RouteBuilder {

    private static final String PATH = "file/files/";
    //private static final String PATH2 = "C:\\1-Henrique\\Desenvolvimento\\workspace\\Cursos Camel\\camel-spring-boot\\file\\files\\";

    @Override
    public void configure() throws Exception {
        // I also could have: from("file//:" + PATH2 + "input?delete=true"). It also works.
        //from("file:" + PATH + "input?delete=true") // Listening to the 'files/input' directory and it is cut/past to the 'output' folder. The 'delete=true' means to remove the '.camel' folder from the 'input' folder
        //from("file:" + PATH + "input?move=${date:now:yyyyMMdd}/copy-${file:name}") // Listening to the 'files/input' directory. Then create a folder with the current date and add the file to it. The process is copy/paste.
        from("file:" + PATH + "input?noop=true&recursive=true&delete=true") // 'noop=true' avoids processing files that are the same and also executes copy/paste (not cut/paste).
                                        // 'recursive=true' take into consideration all the folders and subfolders on the specified path.
                                        // input?excludeExt=true ignore files with the .png and .txt extensions (they are not moved to the output folder).
                                                // NOTE: There is also the 'includeExt=png,txt,OtherExtensions' possibility.
                                        // input?timeUnit=SECONDS/MINUTES&initialDelay=10 => The FIRST scanning will be executed after 10s or 10min.
                                        // input?delay=10000&repeatCount=3 => Verify the 'input' folder every 10s and just 3 times. I believe without the 'repeatCount' it would execute the folder every 10s indefinitely.
                                        // input?filterFile=${file:size} < 500 => Just process files that have less than 500 bytes.

//                .choice()
//                    .when(simple("${header.CamelFileLength} < 500")) // If file is lesser than 500 bytes the call the fileComponent
//                        .to("bean:fileComponent")
//                    .otherwise() // Else call FileProcessor
//                        .process(new FileProcessor())
//                .endChoice()

                .log("Filename: ${header.CamelFileName} - Path: ${header.CamelFilePath} - Filename: ${file:name}")
                //.bean("fileComponent") // Call the FileComponent class (added to the Spring context) and execute its method. I should have just one method implemented
                //.bean("fileComponent", "log2") // Call the FileComponent class (added to the Spring context) and execute the method log2(). This way I can have more than one method on this bean.
                //.process(new FileProcessor())
                //.to("file://" + PATH + "output"); // If a file is added to the 'input' folder, it is moved to the 'output' folder.
                .to("file://" + PATH + "output?flatten=true"); // Considering 'recursive=true', on the 'input' folder, the 'flatten=true' will put all the files JUST on the 'output' folder, without copying and will not take into consideration the other folders that may exist on the input folder.
    }
}

@Component
class FileComponent {
    public void log(File file) {
        System.out.println("FileComponent test is: " + file.getName());
    }
//    public String log2(String file) {
//        System.out.println("FileComponent test is: " + file);
//        return "empty";
//    }
}

class FileProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println("My Processor: " + exchange.getIn().getBody());
    }
}
