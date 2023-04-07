package com.cn.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @Description reusing code （重用流） ——> 方法封装
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#advanced-mutualizing-operator-usage">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/6 17:45
 * @Version V1.0
 */
@Slf4j
public class Demo1 {


    public static void main(String[] args) {

        transform();
//        transformDeferred();

    }



    /**
     * @Description: transform 流处理逻辑重用
     *
     * {@link reactor.core.publisher.Flux#transform(java.util.function.Function)} 中 from(transformer.apply(this))
     *
     * @author Levi.Ding
     * @date 2023/4/7 13:50
     * @return : void
     */
    public static void transform(){
        AtomicInteger atomicInteger = new AtomicInteger();

        Function<Flux<Integer>,Flux<Integer>> code = i ->{
                log.info("stateless code");
                if(atomicInteger.incrementAndGet() == 1){

                  return  i.map(c -> c*2);

                } else {
                    return i.map(c -> c*10);
                }

        };
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .doOnNext(i -> log.info("onNext1 i : {}", i))
                .transform(code);

        integerFlux.subscribe(i -> log.info("subscribe1 i : {}",i));
        integerFlux.subscribe(i -> log.info("subscribe2 i : {}",i));


    }

    /**
     * @Description: 延迟订阅流 (流处理逻辑重用)
     *
     * {@link reactor.core.publisher.Flux#transformDeferred(java.util.function.Function)}
     * defer(() -> {})
     *
     * @author Levi.Ding
     * @date 2023/4/7 14:14
     * @return : void
     */
    public static void transformDeferred(){
        AtomicInteger atomicInteger = new AtomicInteger();

        Function<Flux<Integer>,Flux<Integer>> code = i ->{
            log.info("stateful code");
            if(atomicInteger.incrementAndGet() == 1){

                return  i.map(c -> c*2);

            } else {
                return i.map(c -> c*10);
            }

        };
        Flux<Integer> integerFlux = Flux.range(1, 10)
                .doOnNext(i -> log.info("onNext1 i : {}", i))
                .transformDeferred(code);

        integerFlux.subscribe(i -> log.info("subscribe1 i : {}",i));
        integerFlux.subscribe(i -> log.info("subscribe2 i : {}",i));



    }



}
