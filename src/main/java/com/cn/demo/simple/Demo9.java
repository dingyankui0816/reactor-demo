package com.cn.demo.simple;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * @Description Subscribe  子类使用
 *
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_an_alternative_to_lambdas_basesubscriber">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/14 16:10
 * @Version V1.0
 */
@Slf4j
public class Demo9 {

    public static void main(String[] args) throws InterruptedException, IOException {
        //LambdaSubscriber
//        lambdaSubscriber();
        //SimpleSubscriber
        baseSubscriber();
        System.in.read();
    }


    /**
     * @Description: {@link Flux#subscribe()} 默认使用 {@link reactor.core.publisher.LambdaSubscriber}
     * @author Levi.Ding
     * @date 2023/2/14 16:19
     * @return : void
     */
    public static void lambdaSubscriber(){
        Flux.range(1,10).subscribe(i -> log.info("LambdaSubscriber Flux i : {}",i));

    }

    /**
     * @Description: Base 多样性订阅
     * @author Levi.Ding
     * @date 2023/2/14 17:18
     * @return : void
     */
    public static void baseSubscriber(){
        SampleSubscriber sampleSubscriber = new SampleSubscriber(2);
        Flux.range(1,10).log().subscribe(sampleSubscriber);
        // !!!!!!! 同一个 订阅不能用于多流模式 , 当前流会取消
        Flux<Integer> share = Flux.range(10, 20);
        share.subscribe(sampleSubscriber);
    }

    /**
     * Base 多样性订阅
     * @param <T>
     */
    public static class SampleSubscriber<T> extends BaseSubscriber<T>{

        public int n;

        public int limit;

        public SampleSubscriber(Integer n) {
            this.n = n;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            log.info("开始执行订阅信息 previous subscription : {} ",subscription);

            //执行当前 订阅信息的实现
            // 批次消费，无边界，即一次性消费全部  -- 背压 {@link Demo10}
//            requestUnbounded();
            // 批次消费，每批消费n条 -- 背压 {@link Demo10}
            request(n);
        }

        @Override
        protected void hookOnNext(T value) {
            log.info("SampleSubscriber Flux i : {}",value);
            limit ++ ;
            if (limit == n) {
                limit = 0 ;
                request(n);
            }
        }

        @Override
        protected void hookOnCancel() {
            log.info("SampleSubscriber Flux Cancel");
        }
    }

}
