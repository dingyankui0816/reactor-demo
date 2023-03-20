package com.cn.demo.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * @Description Flux subscribeOn
 *
 * subscribeOn 可以更改当前流所有上下文执行计算任务线程
 * 如果存在多个 subscribeOn 只需关注最早的 subscribeOn 方法所使用的线程。 即该线程为执行任务真正的线程信息
 *
 * 执行流程
 * {@link reactor.core.publisher.FluxSubscribeOn#subscribeOrReturn(CoreSubscriber)} 生成 {@link reactor.core.publisher.FluxSubscribeOn.SubscribeOnSubscriber} (用于线程切换的类) ->
 * 执行 {@link  reactor.core.publisher.LambdaSubscriber#onSubscribe(Subscription)} -> {@link reactor.core.publisher.FluxSubscribeOn.SubscribeOnSubscriber#request(long)} 保存属性值 Operators.addCap(REQUESTED, this, n);
 * 执行 {@link reactor.core.publisher.FluxSubscribeOn#subscribeOrReturn(CoreSubscriber)} 中 worker.schedule(parent); 在线程池中执行任务 {@link reactor.core.publisher.FluxSubscribeOn.SubscribeOnSubscriber#run()}
 * 执行 {@link reactor.core.publisher.FluxCreate#subscribe(Consumer)} ->  {@link reactor.core.publisher.FluxSubscribeOn.SubscribeOnSubscriber#onSubscribe(Subscription)} 调用 requestUpstream(r, s); 在切换的线程中执行 任务计算 (worker.schedule(() -> s.request(n));)
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_the_subscribeon_method">
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/16 11:36
 * @Version V1.0
 */
@Slf4j
public class Demo13 {


    public static void main(String[] args) throws IOException {
        Flux<Object> subscribe = Flux.create(i -> {
                    for (int i1 = 0; i1 < 10; i1++) {
                        log.info("ThreadName:{} create,i:{}", Thread.currentThread().getName(), i1);
                        i.next(i1);
                    }
                    i.complete();
                })
                .doOnRequest(n -> log.info("ThreadName:{} Subscribe Before 1,i:{}", Thread.currentThread().getName(), n))
                .subscribeOn(Schedulers.boundedElastic()).map(i -> {
                    log.info("ThreadName:{} map,i:{}", Thread.currentThread().getName(), i);
                    return (int) i + 10;
                });


        subscribe.doOnRequest(n -> log.info("ThreadName:{} Subscribe After 1,i:{}", Thread.currentThread().getName(), n)).subscribeOn(Schedulers.newSingle("Levi")).limitRate(1).subscribe(i -> log.info("ThreadName:{} subscribe 1,i:{}", Thread.currentThread().getName(), i));
//        subscribe.subscribe(i->log.info("ThreadName:{} subscribe 1,i:{}",Thread.currentThread().getName(),i));


        System.in.read();
    }
}
