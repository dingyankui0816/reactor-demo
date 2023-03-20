package com.cn.demo.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

/**
 * @Description Flux publishOn
 *
 * 影响后续调用的任务线程，之前的任务线程并不会变更;
 *
 *
 * 执行顺序  
 * {@link reactor.core.publisher.FluxRange#subscribe(Subscriber)} -> {@link reactor.core.publisher.FluxPublishOn.PublishOnSubscriber#onSubscribe(Subscription)}
 * -> {@link reactor.core.publisher.LambdaSubscriber#onSubscribe(Subscription)} -> {@link reactor.core.publisher.FluxPublishOn.PublishOnSubscriber#request(long)} 调用 trySchedule(this, null, null); 开启线程
 * 开启线程后直接执行 {@link reactor.core.publisher.FluxPublishOn.PublishOnSubscriber#onSubscribe(Subscription)} 中 s.request(Operators.unboundedOrPrefetch(prefetch)); 代码。向上请求数据
 * 上层生成数据后会调用 a.onNext((int) i); 向下提供数据。当走到 {@link reactor.core.publisher.FluxPublishOn.PublishOnSubscriber#onNext(Object)} 会将数据放到 Queue中
 * 开启线程后 {@link reactor.core.publisher.FluxPublishOn.PublishOnSubscriber#run()} 会一直阻塞 Queue 的获取，直到获取数据后，向下流转数据至 {@link reactor.core.publisher.LambdaSubscriber#onNext(Object)}
 *
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_the_publishon_method">
 *
 * 基于上述流程发现，PublishOn 方法只会影响 上游往下游传输数据时的线程切换 onNext();
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
