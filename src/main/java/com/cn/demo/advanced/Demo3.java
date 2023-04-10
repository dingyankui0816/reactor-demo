package com.cn.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

/**
 * @Description Flux.ConnectableFlux
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#advanced-broadcast-multiple-subscribers-connectableflux">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/10 10:48
 * @Version V1.0
 */
@Slf4j
public class Demo3 {

    public static void main(String[] args) throws InterruptedException {

//        connect();
        autoConnect();


    }

    /**
     * @Description: {@link Flux#publish()}，先构建流，当流构建成功后。可以选择订阅的数量，手动调用 {@link ConnectableFlux#connect} 将
     * {@link reactor.core.publisher.FluxPublish.PublishSubscriber} 订阅到 {@link  reactor.core.publisher.FluxPublish#source} 中
     * 实现多个订阅者的延迟订阅
     * @author Levi.Ding
     * @date 2023/4/10 11:32
     * @return : void
     */
    public static void connect() throws InterruptedException {
        Flux<Integer> flux = Flux.range(1, 3).doOnSubscribe(s -> log.info("订阅"));
        ConnectableFlux<Integer> publish = flux.publish(1);

        publish.subscribe(i->log.info("subscribe1 i : {}",i));

        publish.subscribe(i->log.info("subscribe2 i : {}",i));

        log.info("连接前");
        TimeUnit.SECONDS.sleep(2);
        publish.connect();
        log.info("连接后");
    }

    /**
     * @Description: 当订阅者达到最小订阅数量自动执行订阅
     * @author Levi.Ding
     * @date 2023/4/10 11:38
     * @return : void
     */
    public static void autoConnect() throws InterruptedException {
        Flux<Integer> flux = Flux.range(1, 3).doOnSubscribe(s -> log.info("订阅"));
        Flux<Integer> connect = flux.publish(1).autoConnect(2);

        log.info("连接第一个订阅者");
        Disposable subscribe = connect.subscribe(i -> log.info("subscribe1 i : {}", i));
        subscribe.dispose();
        TimeUnit.SECONDS.sleep(2);
        log.info("连接第二个订阅者");
        connect.subscribe(i->log.info("subscribe2 i : {}",i));

    }



}
