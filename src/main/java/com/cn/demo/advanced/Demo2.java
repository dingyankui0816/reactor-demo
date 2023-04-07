package com.cn.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * @Description Flux 静态/动态流
 * @Author: Levi.Ding
 * @Date: 2023/4/7 14:42
 * @Version V1.0
 */
@Slf4j
public class Demo2 {


    public static void main(String[] args) {
//        cold();
        hot();
    }

    /**
     * @Description: 静态流，订阅即消费
     * @author Levi.Ding
     * @date 2023/4/7 14:52
     * @return : void
     */
    public static void cold(){
        Flux<Integer> range = Flux.range(1, 10).share();

        range.subscribe(i -> log.info("subscribe1 i : {}",i));
        range.subscribe(i -> log.info("subscribe2 i : {}",i));

    }

    /**
     * @Description: 动态流，基于生产者动态生成的数据进行消费
     * @author Levi.Ding
     * @date 2023/4/7 14:53
     * @return : void
     */
    public static void hot(){
        Sinks.Many<Object> range = Sinks.many().multicast().directBestEffort();

        range.asFlux().subscribe(i -> log.info("subscribe1 i : {}",i));

        for (int i = 1; i < 11; i++) {

            if (i == 5){

                range.asFlux().subscribe(j -> log.info("subscribe2 i : {}",j));
            }
            range.tryEmitNext(i);

        }
    }
}
