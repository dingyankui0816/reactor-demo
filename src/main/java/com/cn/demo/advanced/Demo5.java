package com.cn.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @Description 并行化 Flux
 * @Author: Levi.Ding
 * @Date: 2023/4/11 14:50
 * @Version V1.0
 */
@Slf4j
public class Demo5 {


    /**
     * @Description: 分片并行订阅 ,每一个订阅绑定一个 {@link reactor.core.scheduler.Scheduler#createWorker()}
     *
     *  {@link reactor.core.publisher.ParallelRunOn#subscribe(reactor.core.CoreSubscriber[])} ->
     *  {@link reactor.core.publisher.ParallelSource#subscribe(reactor.core.CoreSubscriber[])}
     *
     *
     *  doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#advanced-parallelizing-parralelflux">
     *
     *
     * @author Levi.Ding
     * @date 2023/4/11 16:05
     * @param args :
     * @return : void
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Flux.range(1,10).parallel(2).runOn(Schedulers.boundedElastic()).subscribe(i -> log.info("ThreadName : {},i : {}",Thread.currentThread().getName(),i));
        TimeUnit.SECONDS.sleep(5);
        Flux.range(1,10).parallel(2).runOn(Schedulers.boundedElastic()).subscribe(i -> log.info("ThreadName : {},i : {}",Thread.currentThread().getName(),i));

        System.in.read();
    }
}
