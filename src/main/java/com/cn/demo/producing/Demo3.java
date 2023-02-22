package com.cn.demo.producing;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @Description Flux push
 *  Flux 高级数据生成器
 *  异步（sink 可以抽出来作为外部变量，对Flux 进行 next(Object)操作 ），
 *  单线程（由于使用了 SpscLinkedArrayQueue 作为内部数据存储，只支持一个生产者一个消费者）
 * 主要代码 {@link reactor.core.publisher.FluxCreate#subscribe(Subscriber)}
 *         source.accept(
 * 					createMode == CreateMode.PUSH_PULL ? new SerializedFluxSink<>(sink) :
 * 							sink);
 *
 * 	doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_asynchronous_but_single_threaded_push">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/22 17:00
 * @Version V1.0
 */
@Slf4j
public class Demo3 {


    /**
     * @Description: 模拟控制台生产数据，基于 reactor 进行数据消费
     * @author Levi.Ding
     * @date 2023/2/22 16:06
     * @param args :
     * @return : void
     */
    public static void main(String[] args) throws InterruptedException {
        Flux.create(i -> {
                    log.info(Thread.currentThread().getName());
                    new Thread(() -> {
                        try {
                            synchronized (Demo3.class) {
                                client(new Demo3.AsyncOp() {
                                    @Override
                                    public void input(String input) {
                                        i.next(input);
                                    }

                                    @Override
                                    public void complete() {
                                        i.complete();
                                    }
                                });
                                Demo3.class.notify();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    synchronized (Demo3.class) {
                        try {
                            Demo3.class.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .switchIfEmpty(Flux.generate(g -> {
                    g.next("empty");
                    g.complete();
                }))
                .subscribe(new BaseSubscriber<Object>() {
                    StringBuilder sb = new StringBuilder();

                    @Override
                    protected void hookOnNext(Object value) {
                        sb.append(value).append(" ");
                        log.info(Thread.currentThread().getName());
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        log.info(sb.toString());
                    }
                });

        TimeUnit.MINUTES.sleep(1);

    }


    /**
     * 生产者模型
     */
    public abstract static class AsyncOp {
        public abstract void input(String input);

        public abstract void complete();
    }

    /**
     * @Description: 生产过程
     * @author Levi.Ding
     * @date 2023/2/22 16:07
     * @param asyncOp :
     * @return : void
     */
    public static void client(Demo3.AsyncOp asyncOp) throws IOException {
        Scanner scan = new Scanner(System.in);
        for (int i = 0; i < 10; i++) {
            String read = scan.nextLine();
            asyncOp.input(read);
        }
        asyncOp.complete();
    }
}
