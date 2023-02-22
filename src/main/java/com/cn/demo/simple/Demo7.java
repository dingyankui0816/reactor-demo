package com.cn.demo.simple;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

/**
 * @Description Demo 7 Reactor Mono Flux 区别
 * Flux {@link Flux}   1 -> n
 *
 * Mono {@link Mono}   1 -> 1
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_simple_ways_to_create_a_flux_or_mono_and_subscribe_to_it">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/2 14:24
 * @Version V1.0
 */
@Slf4j
public class Demo7 {


    public static void main(String[] args) throws InterruptedException, IOException {

        // 1 -> n
        Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofMillis(100)).log();

        flux.concatMap(i -> i == 5?Flux.error(new RuntimeException(""+i)):Flux.just(i))
                .subscribe(s->log.info("flux 1 subscribe success")
                        ,e -> log.info("flux 1 subscribe error {}",e));

        flux.subscribe(s->log.info("flux 2 subscribe success")
                ,e -> log.info("flux 2 subscribe error {}",e));


        //1 -> 1
        Mono<Integer> mono = Mono.from(Flux.range(1, 10).delayElements(Duration.ofMillis(100))).log();

        mono.flatMap(i -> i == 5?Mono.error(new RuntimeException(""+i)):Mono.just(i))
                .subscribe(s->log.info("mono 1 subscribe success")
                        ,e -> log.info("mono 1 subscribe error {}",e));

        mono.subscribe(s->log.info("mono 2 subscribe success")
                ,e -> log.info("mono 2 subscribe error {}",e));

        System.in.read();
    }
}
