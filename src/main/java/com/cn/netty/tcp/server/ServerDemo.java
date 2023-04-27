package com.cn.netty.tcp.server;

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
    public static void main( String[] args ) {
        TcpServer server = TcpServer.create();
        DisposableServer server1 = server.host("localhost").port(8080).bindNow();
        server1.onDispose().block();
    }
}
