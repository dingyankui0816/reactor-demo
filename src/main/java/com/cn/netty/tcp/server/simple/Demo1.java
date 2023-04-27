package com.cn.netty.tcp.server.simple;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

/**
 * @Description server starting and stopping
 *
 * doc <a href="https://projectreactor.io/docs/netty/release/reference/index.html#tcp-server">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/27 10:41
 * @Version V1.0
 */
@Slf4j
public class Demo1 {


    public static void main(String[] args) {
//        tempCreateServer();
//        eagerCreateServer();
        writeServerToClient();
    }

    /**
     * @Description: 选择一个临时端口 创建Server
     *
     * 创建流程与 netty 一样，仅增加了reactor 的适配
     *
     * {@link reactor.netty.transport.ServerTransport#bind()}
     * TransportConnector.bind(config, new AcceptorInitializer(acceptor), local, isDomainSocket)
     * 			                  .subscribe(disposableServer);
     *
     * @author Levi.Ding
     * @date 2023/4/27 15:15
     * @return : void
     */
    public static void tempCreateServer(){
        DisposableServer server = TcpServer.create().bindNow();
        server.onDispose().block();
    }

    /**
     * @Description: 创建一个固定端口的 server
     * @author Levi.Ding
     * @date 2023/4/27 15:27
     * @return : void
     */
    public static void createServer(){
        DisposableServer server = TcpServer
                .create()
                .host("localhost")
                .port(30333)
                .bindNow();
        server.onDispose().block();
    }


    /**
     * @Description: 创建多个server
     * @author Levi.Ding
     * @date 2023/4/27 15:30
     * @return : void
     */
    public static void createManyServer(){
        DisposableServer server1 = TcpServer
                .create()
                .host("localhost")
                .port(30333)
                .bindNow();

        DisposableServer server2 = TcpServer
                .create()
                .host("localhost")
                .port(30334)
                .bindNow();
        Mono.when(server1.onDispose(), server2.onDispose()).block();
    }

    /**
     * @Description:
     *
     * handle((i, o) -> i.receive().then()) todo
     *
     * .warmup() todo
     *
     * @author Levi.Ding
     * @date 2023/4/27 16:16
     * @return : void
     */
    public static void eagerCreateServer(){

        TcpServer server = TcpServer.create().handle((i, o) -> i.receive().then());
        server.warmup().block();
        DisposableServer disposableServer = server.bindNow();

        disposableServer.onDispose().block();

    }

    /**
     * @Description: 将server 数据写入到 client端
     * @author Levi.Ding
     * @date 2023/4/27 16:20
     * @return : void
     */
    public static void writeServerToClient(){
        TcpServer.create()
                .host("localhost")
                .port(33300)
                .handle((i, o) -> o.sendString(Mono.fromSupplier(()->"Hello World!").doOnSuccess( c -> log.info("sendMessage"))))
                .bindNow()
                .onDispose()
                .block();
    }
}
