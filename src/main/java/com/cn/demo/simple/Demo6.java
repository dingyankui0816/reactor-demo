package com.cn.demo.simple;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Description Demo 6 Reactor 基础应用
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_asynchronicity_to_the_rescue">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/2 14:24
 * @Version V1.0
 */
@Slf4j
public class Demo6 {

    /**
     * @Description: 模拟数据库操作
     * @author Levi.Ding
     * @date 2023/2/2 14:29
     * @param userId :
     * @return : reactor.core.publisher.Mono<java.util.List<java.math.BigInteger>>
     */
    public Flux<BigInteger> getFavorites(BigInteger userId) throws InterruptedException {

        log.info("getFavorites  : " + Thread.currentThread().getName());
        List<BigInteger> list = new ArrayList<>();
        Random random = new Random();
//        TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
        if (1==1){
            list.add(BigInteger.valueOf(1));
            list.add(BigInteger.valueOf(2));
            list.add(BigInteger.valueOf(3));
            list.add(BigInteger.valueOf(4));
            list.add(BigInteger.valueOf(5));
            list.add(BigInteger.valueOf(6));
        }
        return Flux.fromIterable(list);
    }

    /**
     * @Description: 模拟数据库操作
     * @author Levi.Ding
     * @date 2023/2/2 14:35
     * @param favoriteId :
     * @return : reactor.core.publisher.Mono<java.util.List<java.lang.String>>
     */
    public Flux<List<String>> getDetails(BigInteger favoriteId) {
        log.info("getDetails  : " + Thread.currentThread().getName());
        List<String> list = new ArrayList<>();
        Random random = new Random();
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (random.nextInt(2)==1){
            list.add((favoriteId + ": 爱好1"));
            list.add((favoriteId + ": 爱好2"));
            list.add((favoriteId + ": 爱好3"));
            list.add((favoriteId + ": 爱好4"));
            list.add((favoriteId + ": 爱好5"));
            list.add((favoriteId + ": 爱好6"));
        }
        return Flux.just(list);
    }

    /**
     * @Description: 模拟数据库操作
     * @author Levi.Ding
     * @date 2023/2/2 14:35
     * @return : reactor.core.publisher.Mono<java.util.List<java.lang.String>>
     */
    public List<String> getSuggestions() {
        log.info("getSuggestions  : " + Thread.currentThread().getName());
        List<String> list = new ArrayList<>();
        Random random = new Random();
//        try {
//            TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if (1==1){
            list.add(("推荐1"));
            list.add(("推荐2"));
            list.add(("推荐3"));
            list.add(("推荐4"));
            list.add(("推荐5"));
            list.add(("推荐6"));
        }
        return list;
    }


    public static void main(String[] args) throws InterruptedException, IOException {

        Demo6 demo6 = new Demo6();
        List<List<String>> block = demo6.getFavorites(BigInteger.valueOf(1))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(a -> demo6.getDetails(a))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.fromSupplier(()->demo6.getSuggestions()))
                .subscribeOn(Schedulers.parallel())
                .flatMap(s->{

                    log.info("subscribe: " + Thread.currentThread().getName());
                    return Flux.fromIterable(s).subscribeOn(Schedulers.parallel()).flatMap(si -> {
//                        log.info("subscribe  flatMap: " + Thread.currentThread().getName());
                        return Flux.just(demo6.getSuggestions());
                    });

                })
                .collectList()
                .block();
        block
                .forEach(item -> item.forEach(i -> log.info(i)));
//                .take(5)
//                .publishOn(Schedulers.boundedElastic())
//                .subscribe(i->{
//                    if (CollectionUtils.isEmpty(i)){
//                        return;
//                    }
//                    log.info("=====================================");
//                    i.stream().forEach(item -> log.info(item));
//                    log.info("=====================================");
//                },i->log.info("error"),()->{log.info("success");});



        System.in.read();
    }
}
