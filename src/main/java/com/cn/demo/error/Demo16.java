package com.cn.demo.error;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;

/**
 * @Description Flux retry
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_error_handling_operators">
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/27 14:37
 * @Version V1.0
 */
@Slf4j
public class Demo16 {

    public static void main(String[] args) throws IOException, InterruptedException {
//        retry();
        retryWhen();
//        testSinks();
    }

    /**
     * @Description: 流 重试逻辑
     *
     * 流程 {@link reactor.core.publisher.FluxRetry#subscribeOrReturn(reactor.core.CoreSubscriber)} 中 parent.resubscribe(); 拆分上下文流，上文流的订阅者产生逻辑，由 {@link reactor.core.publisher.FluxRetry.RetrySubscriber} 控制(用于重试，重试时会重新生成上层订阅流) ->
     * {@link reactor.core.publisher.FluxRetry.RetrySubscriber#resubscribe()} 中 source.subscribe(this); 初始化上游订阅流 ->
     * 执行上下文流逻辑 -> 遇到Error时 (从上层流 -> 下层流执行 {@link org.reactivestreams.Subscriber#onError(java.lang.Throwable)}) ->
     * 执行 {@link reactor.core.publisher.FluxRetry.RetrySubscriber#onError(java.lang.Throwable)} 中 resubscribe();
     *  清理当前流保存的前一次流执行的垃圾数据 {@link reactor.core.publisher.FluxRetry.RetrySubscriber#resubscribe()} 中
     *  produced = 0L;
     * 	produced(c);
     * -> 执行重试
     *
     *
     * @author Levi.Ding
     * @date 2023/3/27 17:10
     * @return : void
     */
    public static void retry() {
        Flux.range(1, 10)
                .delayElements(Duration.ofSeconds(1), Schedulers.newSingle("Levi"))
                .map(i -> {
                        i = i * 2;
                        if (i == 10) {
                            throw new RuntimeException("Test onError");
                        }
                        return i;
                    }
                ).doOnError(e -> log.info("doOnError!")).retry(1).subscribe(i -> log.info("i : {}", i), e -> log.info("", e), () -> log.info("complete!"));

    }


    /**
     * @Description: retryWhen 先拆分流上下文 将流分为多个部分:
     * 1、业务逻辑流 {业务上层流} -> {@link reactor.core.publisher.FluxRetryWhen.RetryWhenMainSubscriber} -> {业务下层流}
     * 2、业务异常信息流(缓存) {@link  reactor.core.publisher.SinkManyEmitterProcessor.EmitterInner} (缓存每一次异常信息) -> {业务重试条件流} -> {@link  reactor.core.publisher.FluxRetryWhen.RetryWhenOtherSubscriber} (重试)
     * 3、业务异常重试流(触发重试)  {@link reactor.core.publisher.SinkManyEmitterProcessor.EmitterInner}  ->  {业务重试条件流} -> {@link reactor.core.publisher.Operators.DeferredSubscription}
     *
     * 执行顺序 :
     *
     * {业务上层流} (error) -> {@link reactor.core.publisher.FluxRetryWhen.RetryWhenMainSubscriber#onError(Throwable)} ->
     * 清理前一次 {业务上层流} 数据 produced(p); -> 业务异常信息流(缓存)
     * 缓存: {@link  reactor.core.publisher.SinkManyEmitterProcessor.EmitterInner#emitNext(Object, Sinks.EmitFailureHandler)}
     * -> 执行 业务异常重试流(触发重试)
     * 执行:
     *
     * {@link reactor.core.publisher.Operators.DeferredSubscription#request(long)} -> {业务重试条件流} -> {@link  reactor.core.publisher.SinkManyEmitterProcessor.EmitterInner#request(long)}}
     * 向上层获取异常数据
     * ->
     * {@link  reactor.core.publisher.SinkManyEmitterProcessor#drain()} 中 inner.actual.onNext(v); 向下层传输数据 -> {业务重试条件流} (所有接着往下传输的流都会进行重试) -> {@link  reactor.core.publisher.FluxRetryWhen.RetryWhenOtherSubscriber#onNext(Object)} 中 main.resubscribe(t) 进行 {业务上层流}重试
     *
     * @author Levi.Ding
     * @date 2023/3/28 17:47
     * @return : void
     */
    public static void retryWhen(){
        Flux.range(1, 10)
//                .delayElements(Duration.ofSeconds(1), Schedulers.newSingle("Levi"))
                .map(i -> {
                            i = i * 2;
                            if (i == 10) {
                                throw new RuntimeException("Test onError");
                            }
                            return i;
                        }
                ).doOnError(e -> log.info("doOnError!"))
                .retryWhen(Retry.from(r -> r.take(3)))
                .subscribe(i -> log.info("i : {}", i), e -> log.info("", e), () -> log.info("complete!"));
    }

    /**
     * @Description:  Sinks.many().multicast().onBackpressureBuffer()
     *
     *  创建一个多订阅者类型的Sink,配置了一个背压缓冲区（当订阅者处理不过来用于缓存）
     *
     * completionSignal.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST); 执行流程
     * {@link reactor.core.publisher.InternalManySink#emitNext(java.lang.Object, reactor.core.publisher.Sinks.EmitFailureHandler)} ->
     * {@link reactor.core.publisher.SinkManySerialized#tryEmitNext(java.lang.Object)} ->
     * {@link reactor.core.publisher.SinkManyEmitterProcessor#tryEmitNext(java.lang.Object)} ->
     * 初始化队列 q = Queues.<T>get(prefetch).get(); -> 将当前Sink#next()的数据 offer 到 queue中 ->
     * {@link reactor.core.publisher.SinkManyEmitterProcessor#drain()} ->
     * 获取所有subscribers 对比所有的 requested 属性，获取最小的请求数量 maxRequested = Math.min(maxRequested, r); ->
     * 循环将 requested 数量的生产数据 加入到 所有 subscribers中  inner.actual.onNext(v);
     *
     * @author Levi.Ding
     * @date 2023/3/28 16:41
     * @return : void
     */
    public static void testSinks() throws IOException, InterruptedException {
        Sinks.Many<String> completionSignal = Sinks.many().multicast().onBackpressureBuffer();
        Scanner scan = new Scanner(System.in);
        new Thread(() -> {

            while (true){
                String s = scan.nextLine();
                if (s.equals("end")){
                    synchronized (Demo16.class){
                        Demo16.class.notifyAll();
                    }
                    return;
                }
                completionSignal.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST);

            }
        }).start();

        completionSignal.asFlux()
                .subscribe(i -> log.info("1 {}",i));

        completionSignal.asFlux()
                .subscribe(i -> log.info("2 {}",i));
        synchronized (Demo16.class){
            Demo16.class.wait();
        }
    }


}
