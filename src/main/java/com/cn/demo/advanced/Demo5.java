package com.cn.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @Description 并行化 Flux
 *
 *   doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#advanced-parallelizing-parralelflux">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/11 14:50
 * @Version V1.0
 */
@Slf4j
public class Demo5 {



    public static void main(String[] args) throws IOException, InterruptedException {
//        testParallel();
        testParallelThenMerge();
    }

    /**
     * @Description: 分片并行订阅 ,每一个订阅绑定一个 {@link reactor.core.scheduler.Scheduler#createWorker()}
     *
     *  {@link reactor.core.publisher.ParallelRunOn#subscribe(reactor.core.CoreSubscriber[])} ->
     *  {@link reactor.core.publisher.ParallelSource#subscribe(reactor.core.CoreSubscriber[])}
     *
     *  多订阅者基于轮训的负载均衡策略处理
     *  {@link reactor.core.publisher.ParallelSource.ParallelSourceMain#drain()}
     *
     *
     *
     *
     * @author Levi.Ding
     * @date 2023/4/11 16:05
     * @param
     * @return : void
     */
    public static void testParallel() throws InterruptedException, IOException {
        Flux.range(1,10).parallel(2).runOn(Schedulers.boundedElastic()).subscribe(i -> log.info("ThreadName : {},i : {}",Thread.currentThread().getName(),i));
        TimeUnit.SECONDS.sleep(5);
        Flux.range(1,10).parallel(2).runOn(Schedulers.boundedElastic()).subscribe(i -> log.info("ThreadName : {},i : {}",Thread.currentThread().getName(),i));

        System.in.read();
    }

    /**
     * @Description: sequential 使用的线程是第一个 获取到 wip 锁的 线程
     * {@link reactor.core.publisher.ParallelMergeSequential.MergeSequentialMain#onNext(reactor.core.publisher.ParallelMergeSequential.MergeSequentialInner, java.lang.Object)}
     * @author Levi.Ding
     * @date 2023/4/23 11:14
     * @return : void
     */
    public static void testParallelThenMerge() throws InterruptedException, IOException {
        Flux.range(1,10).parallel(2).runOn(Schedulers.boundedElastic())
                .map(i -> i *2 ).sequential()
                .subscribe(i -> log.info("ThreadName : {},i : {}",Thread.currentThread().getName(),i));
        System.in.read();
    }





}
