package com.cn.demo.simple;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * backpressure
 * reshape requests
 *
 *  背压 通常是指，告诉上游(生产者) 当前数据请求的批次上限 {@link org.reactivestreams.Subscription#request(long)}
 *  背压请求流程 LastSubscription -> ..... -> FirstSubscription
 *  重塑 通常由上游(生产者)设定，即可更改下游(消费者)决定的批次上限 {@link reactor.core.publisher.FluxBuffer.BufferExactSubscriber#request(long n)}
 *
 *  预取 {@link reactor.core.publisher.FluxConcatMap.ConcatMapImmediate#onSubscribe} 中 s.request(Operators.unboundedOrPrefetch(prefetch));
 *  预取优化 {@link reactor.core.publisher.FluxConcatMap.ConcatMapImmediate} 中 this.limit = Operators.unboundedOrLimit(prefetch); 优化至每次消耗至75% 则向上游继续取75%
 *
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_on_backpressure_and_ways_to_reshape_requests">
 *
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/15 16:25
 * @Version V1.0
 */
@Slf4j
public class Demo10 {


    public static void main(String[] args) throws InterruptedException, IOException {
        //背压
//        backpressure();
        //重塑
//        reshapeRequests();
        //补充优化
//        limitRate();
        //限制消费
//        limitRequest();
    }


    /**
     * @Description: 背压
     * 背压流程
     * {@link BaseSubscriber#request(long)} ->
     * {@link reactor.core.publisher.FluxPeek.PeekSubscriber#request(long)} ->
     * {@link reactor.core.publisher.FluxRange.RangeSubscription#request(long)}
     * @author Levi.Ding
     * @date 2023/2/15 16:44
     * @return : void
     */
    public static void backpressure(){
        Flux.range(1,10)
                .doOnRequest((i) -> log.info("request of {}",i))
                .subscribe(new BaseSubscriber<Integer>() {
                    int prefetch = 3;
                    int limit ;
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(prefetch);
                    }

                    @SneakyThrows
                    @Override
                    protected void hookOnNext(Integer value) {
                        log.info("process request result : {}",value);
                        TimeUnit.SECONDS.sleep(1);
                        limit ++ ;
                        if (limit == prefetch){
                            limit = 0;
                            request(prefetch);
                        }
                        if (value.intValue() == 6){
                            cancel();
                        }
                    }
                });
    }

    /**
     * @Description: 重塑
     * 重塑流程
     * {@link BaseSubscriber#request(long n)} ->
     * {@link reactor.core.publisher.FluxPeek.PeekSubscriber#request(long n)} ->
     * {@link reactor.core.publisher.FluxBuffer.BufferExactSubscriber#request(long n)} ->
     * {@link reactor.core.publisher.FluxPeek.PeekSubscriber#request(long n * reactor.core.publisher.FluxBuffer.BufferExactSubscriber.size )} ->
     * {@link reactor.core.publisher.FluxRange.RangeSubscription#request(long n * reactor.core.publisher.FluxBuffer.BufferExactSubscriber.size )}
     * @author Levi.Ding
     * @date 2023/2/15 17:37
     * @return : void
     */
    public static void reshapeRequests(){
        Flux.range(1,10).doOnRequest((i) -> log.info("request 1 of {}",i)).buffer(3).doOnRequest((i) -> log.info("request 2 of {}",i))
                .subscribe(new BaseSubscriber<List<Integer>>() {
                    int prefetch = 1;
                    int limit ;
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(prefetch);
                    }

                    @SneakyThrows
                    @Override
                    protected void hookOnNext(List<Integer> value) {
                        value.forEach((i) -> log.info("batch process request result : {}",i));
                        TimeUnit.SECONDS.sleep(1);
                        limit ++ ;
                        if (limit == prefetch){
                            limit = 0;
                            request(prefetch);
                        }
                    }
                });
    }


    /**
     * @Description: 手动设置补充优化  50%
     * @author Levi.Ding
     * @date 2023/2/16 16:52
     * @return : void
     */
    public static void limitRate(){
        Flux.range(1,10)
                .doOnRequest(i -> log.info("request of {}",i))
                .limitRate(4,4 -(4>>1)).subscribe(i -> {
                   log.info("flux subscribe i :{}",i);
                });
    }

    /**
     * @Description: 限制下游（消费者）最大总需求，达到最大队列后，会发送 onComplete
     * @author Levi.Ding
     * @date 2023/2/16 17:09
     * @return : void
     */
    public static void limitRequest(){
        Flux.range(1,10)
                .doOnRequest(i -> log.info("request of {}",i))
                .take(5)
                .subscribe(i -> {
                    log.info("flux subscribe i :{}",i);
                });
    }
}
