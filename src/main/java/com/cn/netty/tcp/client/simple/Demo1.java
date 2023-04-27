package com.cn.netty.tcp.client.simple;

import reactor.netty.tcp.TcpClient;

/**
 * @Description tcp-client
 *
 * <a href="https://projectreactor.io/docs/netty/release/reference/index.html#tcp-client">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/27 16:24
 * @Version V1.0
 */
public class Demo1 {

    public static void main(String[] args) {
        createClient();
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
     * @Description: 创建客户端
     * @author Levi.Ding
     * @date 2023/4/27 16:35
     * @return : void
     */
    public static void createClient(){
        TcpClient.create().host("localhost").port(33300).connectNow().onDispose().block();
    }

}
