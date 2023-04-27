package com.cn.netty.tcp.client;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

/**
 * @Description 客户端链接
 * @Author: Levi.Ding
 * @Date: 2023/2/1 16:22
 * @Version V1.0
 */
@Slf4j
public class ClientDemo {

    public static void main(String[] args) {
        Connection c = TcpClient.create().host("localhost").port(8080)
                .handle((i,o)->
                        o.sendString(Mono.just("Hello World!"))
                )

                .connectNow();

        c.onDispose().block();

    }
}
