package com.cn.server;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

/**
 * Hello world!
 *
 */
@Slf4j
public class ServerDemo
{
    public static void main( String[] args )
    {
        TcpServer server = TcpServer.create().handle((i,o)->
                {
                    Mono<Void> then = i.receive().then();
                    log.info("=================发送消息=================");
                    i.receive().asString();

                    return then;
                }

                );
        DisposableServer server1 = server.host("localhost").port(8080).bindNow();
//        DisposableServer server2 = server.host("localhost").port(8081).bindNow();
//        Mono.when(server1.onDispose(),server2.onDispose()).block();
        server1.onDispose().block();
    }
}
