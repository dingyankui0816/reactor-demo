package com.cn.demo.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.UUID;

/**
 * @Description Flux.context
 *
 *   doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#context">
 *
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/23 16:20
 * @Version V1.0
 */
@Slf4j
public class Demo6 {

    public static void main(String[] args) {
//        testContext1();
//        testContext2();
//        testContext3();
//        testContext4();
        testContext5();
    }


    /**
     * @Description: First using Context
     *
     * {@link reactor.core.publisher.MonoContextWrite#subscribeOrReturn(reactor.core.CoreSubscriber)}
     * 中 [doOnContext.apply(actual.currentContext());] 将context 传入 ->
     * 保存基于业务处理后的 context [new FluxContextWrite.ContextWriteSubscriber<>(actual, c)]
     *
     * @author Levi.Ding
     * @date 2023/4/24 13:43
     * @return : void
     */
    public static void testContext1(){
        String key  = "message";
        String mono = Mono.just("Hello").publishOn(Schedulers.boundedElastic())
                .flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.get(key))).subscribeOn(Schedulers.boundedElastic()))
                .contextWrite(ctx -> ctx.put(key, "World!")).log().block();

        log.info("value: {}",mono);
    }

    /**
     * @Description: First using Context
     *
     * {@link reactor.core.publisher.MonoContextWrite#subscribeOrReturn(reactor.core.CoreSubscriber)}
     * 中 doOnContext.apply(actual.currentContext()); 将context 传入 同时保存基于业务处理后的 context
     * 但由于 {@link reactor.core.publisher.MonoDeferContextual#subscribe(reactor.core.CoreSubscriber)} 中是使用下层 context，
     * 所以拿不到上层中 MonoContextWrite 保存的context 所以会重新创建一个context 导致取不到业务设置的message
     *
     * @author Levi.Ding
     * @date 2023/4/24 13:43
     * @return : void
     */
    public static void testContext2(){
        String key  = "message";
        String mono = Mono.just("Hello")
                .contextWrite(ctx -> ctx.put(key, "World!")).log()
                .flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.getOrDefault(key,"Levi"))))
                .block();

        log.info("value: {}",mono);
    }

    /**
     * @Description: 多次使用 {@link Mono#contextWrite(ContextView)} 仅最近的message 会生效
     * @author Levi.Ding
     * @date 2023/4/24 14:37
     * @return : void
     */
    public static void testContext3(){
        String key  = "message";
        String mono = Mono.just("Hello")
                .flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.getOrDefault(key,"Levi"))))
                .contextWrite(ctx -> ctx.put(key, "World——1!"))
                .contextWrite(ctx -> ctx.put(key, "World——2!"))
                .block();

        log.info("value: {}",mono);
    }

    /**
     * @Description: context 作用域，多次contextWrite仅会影响 每次context的上层
     *
     * 原理 每次使用 ctx.put方法其实是重新创建一个Context实例，所以多次put不会相互影响
     * {@link reactor.util.context.CoreContext#put(java.lang.Object, java.lang.Object)}
     *
     * @author Levi.Ding
     * @date 2023/4/24 14:51
     * @return : void
     */
    public static void testContext4(){
        String key  = "message";
        String mono = Mono.deferContextual(ctx -> Mono.just("Hello "+ctx.get(key)))
                .contextWrite(ctx -> ctx.put(key, "World——1!"))
                .flatMap(s -> Mono.deferContextual(ctx -> Mono.just(s + " " + ctx.getOrDefault(key,"Levi"))))
                .contextWrite(ctx -> ctx.put(key, "World——2!"))
                .block();

        log.info("value: {}",mono);
    }



    static final String RANDOM_ID = "RANDOM_ID";

    /**
     * @Description: 实际场景
     *
     * 模拟一个HTTP请求，客户端选择请求方式，请求时携带唯一标识ID，服务端基于当前唯一标识做处理
     *
     * @author Levi.Ding
     * @date 2023/4/24 15:56
     * @return : void
     */
    public static void testContext5(){

        Mono<Tuple2<Integer, String>> tuple2Mono = Mono.just("PUT")
                .zipWith(Mono.deferContextual(ctx -> Mono.just(ctx.getOrEmpty(RANDOM_ID))))
                .<String>handle((i, s) -> {
                    if (i.getT2().isPresent()) {
                        if (i.getT2().get().equals("Levi")) {
                            s.error(new RuntimeException("参数错误"));
                            return;
                        }
                        s.next("PUT <" + i.getT1() + "> sent to www.baidu.com" +
                                " with header RANDOM_ID = " + i.getT2().get());
                    } else {
                        s.next("PUT <" + i.getT1() + "> sent to www.baidu.com");
                    }
                    s.complete();
                }).map(s -> Tuples.of(200, s))
                .onErrorResume(e -> Mono.just(Tuples.of(500, e.getMessage())));


        tuple2Mono
                .contextWrite(ctx -> ctx.put(RANDOM_ID, "Levi"))
                .contextWrite(ctx -> ctx.put(RANDOM_ID, UUID.randomUUID().toString()))
                .subscribe(i -> log.info("code : {} , msg : {}",i.getT1(),i.getT2() ));

    }




}
