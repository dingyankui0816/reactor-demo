package com.cn.demo.advanced;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

/**
 * @Description Flux batch
 * @Author: Levi.Ding
 * @Date: 2023/4/10 13:21
 * @Version V1.0
 */
@Slf4j
public class Demo4 {

    public static void main(String[] args) {
//        groupedFlux();
//        windowFlux();
//        windowUntil();
        windowWhile();
    }


    /**
     * @Description: 分批次Flux
     * {@link Flux#groupBy(Function)} 会将当前Flux，通过Function 就行分批次。
     *
     * 原理 {@link reactor.core.publisher.FluxGroupBy.GroupByMain#onNext(java.lang.Object)}
     *
     * @author Levi.Ding
     * @date 2023/4/10 14:50
     * @return : void
     */
    public static void groupedFlux(){
        Flux.range(1, 10)
                .groupBy(i -> i % 2 == 0 ? "偶数" : "奇数")
                .flatMap(g -> g.doOnComplete(() -> log.info("onComplete!")).collectMultimap(j -> g.key()))
                .subscribe(i -> log.info("i : {}",JSON.toJSONString(i)));
    }

    /**
     * @Description: window 窗口化分批
     *
     * size == skip : {@link reactor.core.publisher.FluxWindow.WindowExactSubscriber#onNext(java.lang.Object)}
     * size > skip : {@link reactor.core.publisher.FluxWindow.WindowOverlapSubscriber#onNext(java.lang.Object)}
     * size < skip : {@link reactor.core.publisher.FluxWindow.WindowSkipSubscriber#onNext(java.lang.Object)}
     *
     *
     * @author Levi.Ding
     * @date 2023/4/10 14:58

     * @return : void
     */
    public static void windowFlux(){
        // size == skip
        log.info("size == skip");
        Flux.range(1,10).window(2).flatMap(f -> f.doOnComplete(() -> log.info("onComplete!")).collectList()).subscribe(i -> log.info("i : {}",JSON.toJSONString(i)));
        // size > skip
        log.info("size > skip");
        Flux.range(1,10).window(2,1).flatMap(f -> f.doOnComplete(() -> log.info("onComplete!")).collectList()).subscribe(i -> log.info("i : {}",JSON.toJSONString(i)));
        // size < skip
        log.info("size < skip");
        Flux.range(1,10).window(2,4).flatMap(f -> f.doOnComplete(() -> log.info("onComplete!")).collectList()).subscribe(i -> log.info("i : {}",JSON.toJSONString(i)));

    }

    /**
     * @Description: 基于当前匹配的元素进行窗口拆分
     *
     * {@link  reactor.core.publisher.FluxWindowPredicate.WindowPredicateMain#onNext(java.lang.Object)}
     * mode == Mode.UNTIL && match
     *
     * @author Levi.Ding
     * @date 2023/4/10 15:54
     * @return : void
     */
    public static void windowUntil(){
        Flux.range(1,10).windowUntil(i -> i.equals(3)).flatMap(f -> f.doOnComplete(() -> log.info("onComplete!")).collectList()).subscribe(i -> log.info("windowUntil i : {}",JSON.toJSONString(i)));
        log.info("=================================");
        Flux.just(1,2,3,3,4,3,5,6,7).windowUntil(i -> i.equals(3)).flatMap(f -> f.doOnComplete(() -> log.info("onComplete!")).collectList()).subscribe(i -> log.info("windowUntil i : {}",JSON.toJSONString(i)));
    }

    /**
     * @Description: windowWhile 当匹配失败时 会完成当前WindowFlux 的 complete (不会往下传递这条元素)
     * {@link  reactor.core.publisher.FluxWindowPredicate.WindowPredicateMain#onNext(java.lang.Object)}
     * mode == Mode.WHILE && !match
     *
     * @author Levi.Ding
     * @date 2023/4/10 16:04
     * @return : void
     */
    public static void windowWhile(){
        Flux.range(1,10).windowWhile(i -> i.equals(5)).flatMap(f -> f.doOnComplete(() -> log.info("onComplete!")).collectList()).subscribe(i -> log.info("windowUntil i : {}",JSON.toJSONString(i)));
    }

}
