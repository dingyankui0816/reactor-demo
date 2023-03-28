package com.cn.demo.operators;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.Scanner;

/**
 * @Description 自定义延迟订阅
 *
 * 对比: {@link reactor.core.publisher.Operators.DeferredSubscription}
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/28 15:20
 * @Version V1.0
 */
@Slf4j
public class CustomerDeferred {


    public static void main(String[] args) {
        Deferred deferred = new Deferred();
        Flux.range(1,10).subscribe(deferred);


        Scanner scanner = new Scanner(System.in);
        String s ;
        do {
            s = scanner.nextLine();
            if (s.equals("start")){
                deferred.request(1);
            }
            if (s.equals("cancel")){
                deferred.cancel();
            }

        }while (!s.equals("end"));

    }


    public static class Deferred extends BaseSubscriber<Integer>{

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            log.info("暂时不向上层发起订阅请求信息");
        }

        @Override
        protected void hookOnNext(Integer value) {
            log.info("处理上层返回的数据 value : {}",value);
        }


    }

}
