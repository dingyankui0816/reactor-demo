package com.cn.demo.schedulers;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description 线程调度器
 *
 * Reactor 中 {@link reactor.core.scheduler.Schedulers} 的使用
 *
 * 无执行上下文（Schedulers.immediate()）：在处理时，提交Runnable 将直接执行，有效地在当前运行它们Thread（可以看作是“空对象”或无操作Scheduler）。
 *
 * 单个可重复使用的线程 ( Schedulers.single())。请注意，此方法对所有调用者重复使用相同的线程，直到调度程序被释放。
 * 如果您想要每次调用专用线程，请Schedulers.newSingle()为每次调用使用。
 *
 * 有界弹性线程池 ( Schedulers.boundedElastic())。与其前身一样elastic()，它会根据需要创建新的工作池并重用闲置的工作池。
 * 闲置时间过长（默认为 60 秒）的工作池也会被处理掉。与其elastic()前身不同，它对其可以创建的支持线程数有上限（默认为 CPU 核心数 x 10）。
 * 单个线程达到上限后提交的最多 100 000 个任务将被排入队列，并在线程可用时重新安排（当使用延迟调度时，延迟在线程可用时开始）。
 * 这是 I/O 阻塞工作的更好选择。 Schedulers.boundedElastic()是一种方便的方法，可以为阻塞进程提供自己的线程，这样它就不会占用其他资源。
 *
 * 为并行工作调整的固定工作池 ( Schedulers.parallel())。它会创建与您拥有的 CPU 内核一样多的工作人员。
 *
 *
 * {@link reactor.core.scheduler.Scheduler#schedule(Runnable)} 具体实现
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#schedulers">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/27 11:44
 * @Version V1.0
 */
@Slf4j
public class Demo11 {


    public static void main(String[] args) throws IOException, InterruptedException {
        //无执行上下文
//        immediate();
        //单个重复线程
//        single();
//        专用线程
//        newSingle();
//        有界弹性线程池
        boundedElastic();
        //固定池
//        parallel();
    }


    /**
     * @Description: 无执行上下文（Schedulers.immediate()）：在处理时，提交Runnable 将直接执行，有效地在当前运行它们Thread（可以看作是“空对象”或无操作Scheduler）。
     * @author Levi.Ding
     * @date 2023/2/27 13:48
     * @return : void
     */
    public static void immediate() throws IOException {
        Flux<Integer> range = Flux.range(1, 10);

        new Thread(() -> {

            range.subscribeOn(Schedulers.immediate()).subscribe((i) -> log.info("ThreadName : {} , Flux subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
        System.in.read();
    }

    /**
     * @Description: 单个可重复使用的线程 ( Schedulers.single())。请注意，此方法对所有调用者重复使用相同的线程，直到调度程序被释放。
     * @author Levi.Ding
     * @date 2023/2/27 13:55
     * @return : void
     */
    public static void single() throws IOException {
        Flux<Integer> range = Flux.range(1, 10);

        new Thread(() -> {

            range.subscribeOn(Schedulers.single()).subscribe((i) -> log.info("ThreadName : {} , Flux1 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();

        new Thread(() -> {

            range.subscribeOn(Schedulers.single()).subscribe((i) -> log.info("ThreadName : {} , Flux2 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
        System.in.read();

    }

    /**
     * @Description: 如果您想要每次调用专用线程，请Schedulers.newSingle()为每次调用使用。
     * @author Levi.Ding
     * @date 2023/2/27 14:05
     * @return : void
     */
    public static void newSingle() throws IOException, InterruptedException {
        Flux<Integer> range = Flux.range(1, 10);

        new Thread(() -> {

            range.subscribeOn(Schedulers.newSingle("Levi")).subscribe((i) -> log.info("ThreadName : {} , Flux1 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
        TimeUnit.SECONDS.sleep(1);
        new Thread(() -> {

            range.subscribeOn(Schedulers.newSingle("Levi")).subscribe((i) -> log.info("ThreadName : {} , Flux2 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
        System.in.read();
    }

    /**
     * @Description:
     *
     * 有界弹性线程池 ( Schedulers.boundedElastic())。与其前身一样elastic()，它会根据需要创建新的工作池并重用闲置的工作池。
     * 闲置时间过长（默认为 60 秒）的工作池也会被处理掉。与其elastic()前身不同，它对其可以创建的支持线程数有上限（默认为 CPU 核心数 x 10）。
     * 单个线程达到上限后提交的最多 100000 个任务将被排入队列，并在线程可用时重新安排（当使用延迟调度时，延迟在线程可用时开始）。
     * 这是 I/O 阻塞工作的更好选择。 Schedulers.boundedElastic()是一种方便的方法，可以为阻塞进程提供自己的线程，这样它就不会占用其他资源。
     *
     * 线程执行流程
     * {@link reactor.core.scheduler.Schedulers#BOUNDED_ELASTIC_SUPPLIER} ->
     * {@link reactor.core.scheduler.BoundedElasticScheduler#init()} ->
     * {@link reactor.core.scheduler.BoundedElasticScheduler.BoundedServices#eviction()}   初始化定时器用于检测当前 {@link reactor.core.scheduler.BoundedElasticScheduler.BoundedState} 是否需要销毁 ->
     * 提交 Runnable ->
     * {@link reactor.core.scheduler.BoundedElasticScheduler#schedule(Runnable)} ->
     * {@link reactor.core.scheduler.BoundedElasticScheduler.BoundedServices#pick()} 获取 {@link reactor.core.scheduler.BoundedElasticScheduler.BoundedState} ，不存在可用， 则基于 maxThreads创建/任务最少的 获取 ->
     * {@link reactor.core.scheduler.Schedulers#directSchedule(ScheduledExecutorService, Runnable, Disposable, long, TimeUnit)} 执行任务
     *
     * 注) {@link reactor.core.scheduler.BoundedElasticScheduler.BoundedState} 只会在 dispose 后才释放任务执行完信号，直到所有执行完毕后才会进行空闲释放检查
     *
     * @author Levi.Ding
     * @date 2023/2/27 14:37
     * @return : void
     */
    public static void boundedElastic() throws InterruptedException, IOException {
        Flux<Integer> range = Flux.range(1, 10);
        new Thread(() -> {

            range.subscribeOn(Schedulers.boundedElastic()).doOnRequest(n -> log.info("ThreadName : {} , Flux1 doOnRequest i : {}",Thread.currentThread().getName(),n)).subscribeOn(Schedulers.boundedElastic()).subscribe((i) -> log.info("ThreadName : {} , Flux1 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
//        TimeUnit.SECONDS.sleep(1);
//        new Thread(() -> {
//
//            range.subscribeOn(Schedulers.boundedElastic()).subscribe((i) -> log.info("ThreadName : {} , Flux2 subscribe i : {}",Thread.currentThread().getName(),i));
//        }).start();
        System.in.read();
    }

    /**
     * @Description: 为并行工作调整的固定工作池 ( Schedulers.parallel())。它会创建与您拥有的 CPU 内核一样多的工作人员。
     * @author Levi.Ding
     * @date 2023/2/28 15:29
     * @return : void
     */
    public static void parallel() throws InterruptedException, IOException {
        Flux<Integer> range = Flux.range(1, 10);
        new Thread(() -> {

            range.subscribeOn(Schedulers.parallel()).subscribe((i) -> log.info("ThreadName : {} , Flux1 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
        TimeUnit.SECONDS.sleep(10);
        new Thread(() -> {

            range.subscribeOn(Schedulers.parallel()).subscribe((i) -> log.info("ThreadName : {} , Flux2 subscribe i : {}",Thread.currentThread().getName(),i));
        }).start();
        System.in.read();
    }
}
