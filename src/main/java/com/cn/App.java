package com.cn;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.time.Duration;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    public static void main( String[] args )
    {
        Flux.range(1, 10)
                .log()
                .concatMap(x -> Mono.delay(Duration.ofMillis(100)), 1) // simulate that processing takes time
                .blockLast();
    }
}
