package com.cn.demo.operators;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;


/**
 * @Description  线程安全的订阅切换模式(订阅切换会继承上次请求次数[requested] )
 *
 * {@link reactor.core.publisher.Operators.MultiSubscriptionSubscriber#set(Subscription)} 中 s.request(requested); 继承上次请求次数 继续请求剩余数据
 *
 * 如果想不继承上次请求次数需要在每个元素交付任务时 调用 {@link reactor.core.publisher.Operators.MultiSubscriptionSubscriber#produced(long)}/{@link Operators.MultiSubscriptionSubscriber#producedOne()} 减少 requested 数量
 *
 * {@link reactor.core.publisher.Operators.MultiSubscriptionSubscriber}
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/30 11:17
 * @Version V1.0
 */
@Slf4j
public class CustomerMultiSubscription {


    public static void main(String[] args) {
        egMultiSubscription();
    }


    public static void egMultiSubscription() {
        Flux.range(1, 10).filter(i -> {
                    if (i >= 11) {
                        return true;
                    }
                    return false;
                })
                .switchIfEmpty(Mono.fromSupplier(() -> 1))
                .limitRate(1)
                .subscribe(i -> log.info("i : {}", i));
    }



}
