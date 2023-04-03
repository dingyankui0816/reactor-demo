package com.cn.demo.error;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.Exceptions;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import reactor.util.retry.RetrySpec;

import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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
//        retryWhen();
//        testSinks();
//        retryWhenCustom();
//        retryClassRetrySpec();
//        retryClassRetryBackoffSpec();
        throwShowException();
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
     * @Description: 自定义重试触发条件
     *
     * {@link reactor.util.retry.Retry.RetrySignal#totalRetriesInARow()} 当调用
     * {@link reactor.core.publisher.FluxRetryWhen.RetryWhenMainSubscriber#onNext(java.lang.Object)} 时会初始化该值，
     * 所以{@link reactor.util.retry.Retry.RetrySignal#totalRetriesInARow()} 当首元素处理成功时 该值会被初始化。没有元素处理成功，
     * 则该值和 {@link reactor.util.retry.Retry.RetrySignal#totalRetries()} 意义一样
     *
     * @author Levi.Ding
     * @date 2023/3/31 14:04
     * @return : void
     */
    public static void retryWhenCustom(){
        Flux.range(1,10).map(i -> {
            if (i == 4){
                throw new RuntimeException();
            }
            return i;
        }).retryWhen(new Retry() {
            @Override
            public Publisher<?> generateCompanion(Flux<RetrySignal> retrySignals) {
                return retrySignals.map(r -> {
                    if (r.totalRetries() >3){
                        throw Exceptions.propagate(r.failure());
                    }
                    log.info("retry count : {}",r.totalRetries());
                    return r;
                }).onErrorComplete();
            }
        }).subscribe(i -> log.info("i : {}",i),e -> log.info("e :" ,e),() -> log.info("complete!"));
    }


    /**
     * @Description: RetrySpec 【Retry增强类】
     * {@link reactor.util.retry.RetrySpec}
     * 增强内容: {@link reactor.util.retry.RetrySpec#generateCompanion(reactor.core.publisher.Flux)}
     *
     * 重试执行顺序
     * {@link reactor.util.retry.RetrySpec#applyHooks(Retry.RetrySignal, Mono, Consumer, Consumer, BiFunction, BiFunction, ContextView)}
     * 
     * 
     * @author Levi.Ding
     * @date 2023/3/31 14:44

     * @return : void
     */
    public static void retryClassRetrySpec(){

        Flux.range(1,10).map(i -> {
            if (i == 4){
                throw new RuntimeException("customer exception");
            }
            return i;
        }).retryWhen(RetrySpec.max(4)
                .transientErrors(false)
                .doBeforeRetry(r -> log.info("retry before! "))
                .doAfterRetry(r -> log.info("retry after !"))
                .doBeforeRetryAsync((r)-> Mono.fromSupplier(() -> {log.info("retry before async!"); return r;}).then())
                .doAfterRetryAsync((r)-> Mono.fromSupplier(() -> {log.info("retry after async!"); return r;}).publishOn(Schedulers.newSingle("Levi")).then())
                .onRetryExhaustedThrow((r1,r2)->{throw new RuntimeException("retry max limit ");})
        ).subscribe(i -> log.info("i : {}",i),e -> log.info("e :" ,e),() -> log.info("complete!"));
    }


    /**
     * @Description: RetryBackoffSpec 延迟重试类
     *
     *
     * 增强内容: {@link reactor.util.retry.RetryBackoffSpec#generateCompanion(reactor.core.publisher.Flux)}
     *
     * 延迟逻辑: {@link reactor.util.retry.RetryBackoffSpec#generateCompanion(reactor.core.publisher.Flux)} 中 Mono.delay(effectiveBackoff, backoffSchedulerSupplier.get())
     *
     * RetrySpec.backoff(4,Duration.ofSeconds(1)) -> 重试四次，每次重试间隔时间 1秒 * Math.pow(2,totalRetries);
     *
     * @author Levi.Ding
     * @date 2023/4/3 15:34

     * @return : void
     */
    @SneakyThrows
    public static void retryClassRetryBackoffSpec(){
        Flux.range(1,10).map(i -> {
            if (i == 4){
                throw new RuntimeException("customer exception");
            }
            return i;
        }).retryWhen(RetrySpec.backoff(4,Duration.ofSeconds(1))
                .transientErrors(false)
                .doBeforeRetry(r -> log.info("retry before! "))
                .doAfterRetry(r -> log.info("retry after !"))
                .doBeforeRetryAsync((r)-> Mono.fromSupplier(() -> {log.info("retry before async!"); return r;}).then())
                .doAfterRetryAsync((r)-> Mono.fromSupplier(() -> {log.info("retry after async!"); return r;}).publishOn(Schedulers.newSingle("Levi")).then())
                .onRetryExhaustedThrow((r1,r2)->{throw new RuntimeException("retry max limit ");})

        ).subscribe(i -> log.info("i : {}",i),e -> log.info("e :" ,e),() -> log.info("complete!"));

        System.in.read();

    }

    /**
     * @Description: 捕获显示异常，并将显示异常往下传输 ！
     * @author Levi.Ding
     * @date 2023/4/3 15:58
     * @return : void
     */
    public static void throwShowException(){
        Flux.range(1,10).map(i -> {
            if (i == 4){
                throw  Exceptions.propagate( new IOException("customer exception"));
            }
            return i;
        })
        .onErrorComplete(e -> Exceptions.unwrap(e) instanceof IOException)
        .subscribe(i -> log.info("i : {}",i),e -> log.info("e :" ,e),() -> log.info("complete!"));
    }
}
