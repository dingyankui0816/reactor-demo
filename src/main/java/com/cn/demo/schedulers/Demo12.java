package com.cn.demo.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxPublishOn;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

/**
 * @Description Flux publishOn
 *
 * 影响发布操作的执行线程，不影响订阅操作的执行线程
 *
 * 执行顺序  
 * {@link reactor.core.publisher.FluxRange#subscribe(Subscriber)} -> {@link FluxPublishOn.PublishOnSubscriber#onSubscribe(Subscription)}
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/1 16:29
 * @Version V1.0
 */
@Slf4j
public class Demo12 {

    public static void main(String[] args) throws IOException {
        Flux.range(1, 2).map(i -> {
                    log.info("ThreadName:{} map1 , i:{}", Thread.currentThread().getName(), i);
                    return i * 2;
                })
                .doOnRequest(n -> log.info("ThreadName:{} request1 , i:{}", Thread.currentThread().getName(), n))
                .doOnNext(i -> log.info("ThreadName:{} next1 , i:{}", Thread.currentThread().getName(), i))
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("ThreadName:{} map2 , i:{}", Thread.currentThread().getName(), i);
                    return i * 2;
                })
                .doOnRequest(n -> log.info("ThreadName:{} request2 , i:{}", Thread.currentThread().getName(), n))
                .doOnNext(i -> log.info("ThreadName:{} next2 , i:{}", Thread.currentThread().getName(), i))
                .subscribe(i -> {
                    log.info("ThreadName:{} subscribe , i:{}", Thread.currentThread().getName(), i);
                });

        System.in.read();
    }
}
