package com.cn.demo.producing;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @Description Flux create
 *  Flux 高级数据生成器
 *  异步（sink 可以抽出来作为外部变量，对Flux 进行 next(Object)操作 ），
 *  多线程（由于使用了 MpscLinkedQueue 作为内部数据存储，可以保证多生产者时，存储数据不丢失）
 * 主要代码 {@link reactor.core.publisher.FluxCreate#subscribe(Subscriber)}
 *         source.accept(
 * 					createMode == CreateMode.PUSH_PULL ? new SerializedFluxSink<>(sink) :
 * 							sink);
 *
 * 	doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#producing.create">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/21 15:54
 * @Version V1.0
 */
@Slf4j
public class Demo2 {

    /**
     * @Description: 模拟控制台生产数据，基于 reactor 进行数据消费
     * @author Levi.Ding
     * @date 2023/2/22 16:06
     * @param args :
     * @return : void
     */
    public static void main(String[] args) throws InterruptedException {
        Flux.create(i -> {

                    try {
                        produce(new AsyncOp(i));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
    public static  class  AsyncOp<T>  {

        private FluxSink<T> sink;

        public AsyncOp(FluxSink<T> sink) {
            this.sink = sink;
        }

        public void next(T t){
            sink.next(t);
        }

        public void auNext(Consumer<AsyncOp<T>> cs){
            cs.accept(this);
            complete();
        }

        private void complete(){
            sink.complete();
        }
    }

    /**
     * @Description: 生产过程
     * @author Levi.Ding
     * @date 2023/2/22 16:07
     * @param asyncOp :
     * @return : void
     */
    public static void produce(AsyncOp<String> asyncOp) throws IOException, InterruptedException {

        Scanner scan = new Scanner(System.in);
        asyncOp.auNext(cs -> {
            for (int i = 0; i < 10; i++) {
                String read = scan.nextLine();
                produceMessage(asyncOp,read);
                produceMessage(asyncOp,read);
            }
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void produceMessage(AsyncOp<String> asyncOp,String message){
        executorService.submit(()->{
            asyncOp.next(message);

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
