package com.cn.demo.propagation;

import com.alibaba.fastjson.JSONObject;
import com.cn.demo.propagation.listener.DefaultThreadSignalListener;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @Description Flux contextCapture
 *
 * 使用条件：reactor-core 版本 >= 3.5.0
 * 引入 io.micrometer:context-propagation SPI的支持
 * context-propagation使用: doc <a href="https://micrometer.io/docs/contextPropagation">
 *
 *
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#context.propagation">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/24 16:03
 * @Version V1.0
 */
@Slf4j
public class Demo1 {


    public static void main(String[] args) {
//        compatibleThreadLocal();
//        handleTapThreadLocal();
        tapThreadLocal();
    }


    public static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * @Description: 通过context-propagation 中 ContextSnapshot 快照当前线程中的 ThreadLocal
     * 将快照数据通过 {@link  reactor.core.publisher.MonoContextWrite} 进行传输
     * @author Levi.Ding
     * @date 2023/4/24 18:12
     * @return : void
     */
    public static void compatibleThreadLocal(){
        threadLocal.set("Hello");

        String mono = Mono.deferContextual(ctx ->
                        Mono.delay(Duration.ofSeconds(1))
                            .map(v -> "delayed :" + v + "ms threadLocalValue : " + threadLocal.get() + " context : " + ctx)
                )
                .contextCapture()
                .block();

        log.info("value : {}",mono);
    }


    /**
     * @Description: handle()中可以将context-propagation 中 ContextSnapshot 快照中的 ThreadLocal值进行恢复，
     * 恢复流程
     *
     * 1、通过 contextCapture() 将ThreadLocal的值进行快照
     * 2、在handle中 {@link reactor.core.publisher.MonoHandle#subscribeOrReturn(reactor.core.CoreSubscriber)}进行快照恢复
     *          ContextPropagation.contextRestoreForHandle(this.handler, actual::currentContext); 恢复逻辑
     *
     * @author Levi.Ding
     * @date 2023/4/25 10:55
     * @return : void
     */
    public static void handleTapThreadLocal(){
        threadLocal.set("handle");
        String msg = Mono.delay(Duration.ofSeconds(1))
                .doOnNext(v -> log.info("{}", threadLocal.get()))
                .<String>handle((v, s) -> s.next("handle value : " + threadLocal.get()+ "ThreadName : "+Thread.currentThread().getName()))
                .doOnNext(v -> log.info("{}", threadLocal.get()))
                .contextCapture()
                .block();
        log.info(msg);
    }

    /**
     * @Description: tap()与handle 的原理差不多，区别在于 tap 最终是一个 SignalListener，
     * 使用静态代理 将快照ThreadLocal进行还原 {@link reactor.core.publisher.ContextPropagation.ContextRestoreSignalListener}
     * @author Levi.Ding
     * @date 2023/4/25 11:26
     * @return : void
     */
    public static void tapThreadLocal(){
        threadLocal.set("tap");
        Mono.delay(Duration.ofSeconds(1))
                .doOnNext(v -> log.info("{}", threadLocal.get()))
                .tap(()->new DefaultThreadSignalListener(i -> log.info("thread value : {} ,listener value : {}",threadLocal.get(),i)))
                .doOnNext(v -> log.info("{}", threadLocal.get()))
                .contextCapture()
                .block();
    }

}
