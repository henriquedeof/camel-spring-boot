package com.xpto.kafka.converter;

import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

@Component
public class BookConverter {

    // {"name":"HENRIQUE BOOK", "author":"HENRIQUE FERNANDES", "available":"true"}

    @Handler
    public void converter() {

    }

}
