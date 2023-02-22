package com.cn.demo.simple;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @Description Demo 7 Disposable 使用

 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_cancelling_a_subscribe_with_its_disposable">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/2 14:24
 * @Version V1.0
 */
@Slf4j
public class Demo8 {


    public static void main(String[] args) throws InterruptedException, IOException {
        //取消
//        cancel();
        //替换
//        swap();
        //Disposable 收集
        composite();
        System.in.read();
    }


    /**
     * @Description: Disposable cancel
     * @author Levi.Ding
     * @date 2023/2/13 15:17
     * @return : void
     */
    public static void cancel() throws InterruptedException {
        Object o = new Object();
        Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofMillis(1000)).log();

        Disposable disposable = flux.subscribe(s ->
                {
                    log.info("flux subscribe success" + s);
                    if (s.intValue() ==  5){
                        synchronized (o){
                            o.notifyAll();
                        }
                    }
                }
                , e -> log.info("flux subscribe error {}", e),() -> log.info("flux complete !"));

        synchronized (o){
            o.wait(11*1000);
        }
        //取消订阅
        disposable.dispose();
        log.info("取消订阅成功");
    }

    /**
     * @Description: Disposable swap
     * 可用于 替换 Disposable
     * 当前swap 是一个容器，可以存指定的Disposable ，
     * 当前选择 {@link Disposable.Swap#update(Disposable)} 可以替换当前容器中的 Disposable 并自动停止订阅替换前的Disposable
     * 当前选择 {@link Disposable.Swap#replace(Disposable)} 可以替换当前容器中的 Disposable 不会自动停止替换前的Disposable
     * @author Levi.Ding
     * @date 2023/2/13 15:25
     * @return : void
     */
    public static void swap() throws InterruptedException {
        Object o = new Object();
        Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofMillis(1000)).log();

        Disposable disposable = flux.subscribe(s ->
                {
                    log.info("flux subscribe success" + s);
                    if (s.intValue() ==  5){
                        synchronized (o){
                            o.notifyAll();
                        }
                    }
                }
                , e -> log.info("flux subscribe error {}", e),() -> log.info("flux complete !"));



        synchronized (o){
            o.wait(11*1000);
        }
        Disposable subscribe = Flux.just(1, 2).log().delayElements(Duration.ofMillis(1000)).subscribe(i -> {
            log.info("flux 2 subscribe : {}",i);
            if (i.intValue() == 1){
                synchronized (o){
                    o.notifyAll();
                }
            }
        });
        Disposable.Swap swap = Disposables.swap();
        swap.update(disposable);
        //替换
        log.info("替换结果:{}",swap.update(subscribe));
        synchronized (o){
            o.wait();
        }
        swap.get().dispose();
    }

    /**
     * @Description: 多个Disposable进行收集，实现 统一dispose()
     * @author Levi.Ding
     * @date 2023/2/13 16:30
     * @return : void
     */
    public static void composite() throws InterruptedException {
        Object o = new Object();
        Flux<Integer> flux = Flux.range(1, 10).delayElements(Duration.ofMillis(1000)).log();

        Disposable disposable = flux.subscribe(s ->
                {
                    log.info("flux subscribe success" + s);
                    if (s.intValue() ==  5){
                        synchronized (o){
                            o.notifyAll();
                        }
                    }
                }
                , e -> log.info("flux subscribe error {}", e),() -> log.info("flux complete !"));



        synchronized (o){
            o.wait(11*1000);
        }
        Disposable subscribe = Flux.just(1, 2, 3).log().delayElements(Duration.ofMillis(1000)).subscribe(i -> {
            log.info("flux 2 subscribe : {}",i);
            if (i.intValue() == 1){
                synchronized (o){
                    o.notifyAll();
                }
            }
        });
        synchronized (o){
            o.wait();
        }

        Disposable.Composite composite = Disposables.composite(disposable, subscribe);
        composite.dispose();
    }
}
