package com.xpto.timer.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TimerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // The timer component works ONLY as a consumer.
        // By default, the timer's delay is 1s, which can be configured.

        from("timer:my-timer?period=3000&repeatCount=2&time=2022-09-03 12:06:50")
                // period=3000 => Execute a call every 3s
                // repeatCount => Define how many calls will be executed. By default, it will execute indefinitely.
                // time=yyyy-MM-dd HH:mm:ss or yyyy-MM-dd'T'HH:mm:ss => Sets the time when this timer will be executed: NOTE: Check the time formats that I need to use.

                .log("Hour: ${date:now:HH:mm:ss}");

    }
}
