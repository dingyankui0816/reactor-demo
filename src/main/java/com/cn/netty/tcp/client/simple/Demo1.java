package com.cn.netty.tcp.client.simple;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

/**
 * @Description tcp-client
 *
 * <a href="https://projectreactor.io/docs/netty/release/reference/index.html#tcp-client">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/27 16:24
 * @Version V1.0
 */
@Slf4j
public class Demo1 {

    public static void main(String[] args) {
//        createClient();
        eagerCreateClient();
    }

    /**
     * @Description: 创建客户端
     * @author Levi.Ding
     * @date 2023/4/27 16:35
     * @return : void
     */
    public static void tempCreateClient(){
        TcpClient.create().connectNow().onDispose().block();
    }




    /**
     * @Description: 创建客户端 (连接至指定服务端)
     * @author Levi.Ding
     * @date 2023/4/27 16:35
     * @return : void
     */
    public static void createClient(){
        Connection conn = TcpClient.create()
                .host("localhost")
                .port(33300)
                .connectNow();
        conn
                .onDispose()
                .block();
    }

    /**
     * @Description:
     *
     * TcpClient.handle todo
     *
     * TcpClient.warmup todo
     *
     * @author Levi.Ding
     * @date 2023/5/4 15:03
     * @return : void
     */
    public static void eagerCreateClient(){

        TcpClient client = TcpClient
                .create()
                .host("localhost")
                .port(33300)
                .handle((i, o) -> o.sendString(Mono.just("Hello!")));
        client.warmup().block();
        Connection connection = client.connectNow();

         connection.onDispose().block();

    }


    /**
     * @Description
     *
     * @author Levi.Ding
     * @date 2023/5/4 15:03
     * @return : void
     */
    public static void writeClientToServer(){

        TcpClient client = TcpClient
                .create()
                .host("localhost")
                .port(33300)
                .handle((i, o) -> o.sendString(Mono.just("Hello!")));
        client.warmup().block();
        Connection connection = client.connectNow();

        connection.onDispose().block();

    }

}
