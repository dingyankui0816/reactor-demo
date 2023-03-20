package com.cn.demo.error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @Description Flux error.handling
 *
 *  处理错误
 *
 *  在reactor中 所有的异常错误，都是终止信号，即无论是否处理当前多个流中的某个流异常，后续流都不会继续执行，
 *  当出现异常时，后续异常流处理逻辑会继续执行
 *
 * {@link reactor.core.publisher.FluxOnErrorReturn.ReturnSubscriber#onError(Throwable)} 中 actual.onNext(this.fallbackValue);
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#error.handling">
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/16 15:55
 * @Version V1.0
 */
@Slf4j
public class Demo14 {

    public static void main(String[] args) {

        try {
            List<String> block = Flux.just(1, 2, 0).map(i -> "100 / " + i + " = " + (100 / i)).collectList().block();

            block.stream().forEach(System.out::println);
        }catch (Exception e){
            log.info("{}",e);
        }

        try {
            Flux.just(1, 2, 0).map(i -> "100 / " + i + " = " + (100 / i)).subscribe(i -> log.info(i));
        }catch (Exception e){
            log.info("{}",e);
        }


        Flux.just(1, 0, 2).map(i -> "100 / " + i + " = " + (100 / i)).onErrorReturn("Divided by zero :(").subscribe(i -> log.info(i));
    }
}
