package com.cn.demo.sinks;

import com.alibaba.fastjson.JSON;
import com.cn.demo.error.Demo16;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @Description Sinks
 *
 * doc
 * 官方： <a href="https://projectreactor.io/docs/core/release/reference/index.html#sinks">
 * 资料： <a href="https://ithelp.ithome.com.tw/articles/10272949">
 *
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/31 14:45
 * @Version V1.0
 */
@Slf4j
public class CustomerSinks {


    public static void main(String[] args) throws IOException, InterruptedException {
//        testSinks();
//        testOneSinks();
//        sinksManyUnicast();
        sinksManyReplay();
    }

    /**
     * @Description:  Sinks.many().multicast().onBackpressureBuffer()
     *
     *  创建一个多订阅者类型的Sink,配置了一个背压缓冲区（当订阅者处理不过来用于缓存）
     *
     * completionSignal.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST); 执行流程
     * {@link reactor.core.publisher.InternalManySink#emitNext(java.lang.Object, reactor.core.publisher.Sinks.EmitFailureHandler)} ->
     * {@link reactor.core.publisher.SinkManySerialized#tryEmitNext(java.lang.Object)} ->
     * {@link reactor.core.publisher.SinkManyEmitterProcessor#tryEmitNext(java.lang.Object)} ->
     * 初始化队列 q = Queues.<T>get(prefetch).get(); -> 将当前Sink#next()的数据 offer 到 queue中 ->
     * {@link reactor.core.publisher.SinkManyEmitterProcessor#drain()} ->
     * 获取所有subscribers 对比所有的 requested 属性，获取最小的请求数量 maxRequested = Math.min(maxRequested, r); ->
     * 循环将 requested 数量的生产数据 加入到 所有 subscribers中  inner.actual.onNext(v);
     *
     * @author Levi.Ding
     * @date 2023/3/28 16:41
     * @return : void
     */
    public static void testSinks() throws IOException, InterruptedException {
        Sinks.Many<String> completionSignal = Sinks.many().multicast().onBackpressureBuffer();
        Scanner scan = new Scanner(System.in);
        new Thread(() -> {

            while (true){
                String s = scan.nextLine();
                if (s.equals("end")){
                    synchronized (Demo16.class){
                        Demo16.class.notifyAll();
                    }
                    return;
                }
                completionSignal.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }).start();

        completionSignal.asFlux()
                .subscribe(i -> log.info("1 {}",i));

        completionSignal.asFlux()
                .subscribe(i -> log.info("2 {}",i));
        synchronized (Demo16.class){
            Demo16.class.wait();
        }
    }



    /**
     * @Description: Sinks.one()，创建多订阅者模式的Sink,当前Sink只支持next 一条数据 ，类似于 Mono
     * @author Levi.Ding
     * @date 2023/4/3 16:35
     * @return : void
     */
    public static void testOneSinks(){
        Sinks.One<String> completionSignal = Sinks.one();

        completionSignal.asMono().subscribe(i -> log.info("Mono Subscribe i : {}",i),e -> log.info("error e : ",e),() -> log.info("complete!"));

        completionSignal.asMono().subscribe(i -> log.info("Mono Subscribe i : {}",i),e -> log.info("error e : ",e),() -> log.info("complete!"));
        completionSignal.tryEmitValue("one");
        completionSignal.tryEmitValue("tow");
    }


    /**
     * @Description:
     *
     * multicast 可以有多个订阅者，每个订阅者并不会都拿到全部且一样的元素，而是只会取得订阅后开始最新的。
     *
     * multicast.directBestEffort() 不支持背压，当生产元素比消费 所用时间还长时 会出现丢失数据。多订阅者，只会丢失 未消费完成的订阅者链路数据
     *
     * multicast.onBackpressureBuffer() 支持背压，通过Queue 缓存未被消费当元素，需要控制Queue长度
     *
     * multicast.directAllOrNothing() 当订阅者中有一个订阅者消费还没结束，此时继续生产数据，则所有消费者都无法消费此数据
     *
     * @author Levi.Ding
     * @date 2023/4/6 16:33
     * @return : void
     */
    public static void sinksManyMulticast() throws IOException {
        Sinks.MulticastSpec multicast = Sinks.many().multicast();

        //直接传输(不支持背压)
        Sinks.Many<Object> directMany = multicast.directBestEffort();

        BaseSubscriber<Object> directS = new BaseSubscriber<Object>() {


            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                log.info("不处理数据");
            }

            @Override
            protected void hookOnNext(Object value) {
                log.info("direct subscribe i : {}", value);
            }
        };

        directMany.asFlux().subscribe(directS);

        createThread("direct", directMany,directS);



        //支持背压
        Sinks.Many<Object> backMany = multicast.onBackpressureBuffer();
        BaseSubscriber<Object> backS = new BaseSubscriber<Object>() {


            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                log.info("不处理数据");
            }

            @Override
            protected void hookOnNext(Object value) {
                log.info("back subscribe i : {}", value);
            }
        };

        backMany.asFlux().subscribe(backS);

        createThread("back", backMany,backS);

        System.in.read();

    }

    /**
     * @Description: 创建线程
     * @author Levi.Ding
     * @date 2023/4/4 17:51
     * @param type :
     * @param many :
     * @return : void
     */
    public static void createThread(String type,Sinks.Many<Object> many,BaseSubscriber<Object> b){

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {

                long start = System.currentTimeMillis();
                Sinks.EmitResult emitResult = many.tryEmitNext(i);
                log.info("{} send message sum : {} ms , result : {}", type, System.currentTimeMillis() - start, JSON.toJSONString(emitResult));
                if (i == 5){
                    b.request(Integer.MAX_VALUE);
                }
            }
        }).start();
    }


    /**
     * @Description: unicast 仅支持一个订阅者
     * @author Levi.Ding
     * @date 2023/4/6 16:44
     * @return : void
     */
    public static void sinksManyUnicast(){
        Sinks.Many<Object> back = Sinks.many().unicast().onBackpressureBuffer();
        back.tryEmitNext(1);
        back.tryEmitNext(2);
        back.asFlux().subscribe(i -> log.info("subscribe1 i : {}",i));
        back.tryEmitNext(3);
        back.tryEmitNext(4);
        back.asFlux().subscribe(i -> log.info("subscribe2 i : {}",i));
        back.tryEmitNext(5);
    }

    /**
     * @Description: 支持所有订阅者获取到订阅前到数据并消费
     * @author Levi.Ding
     * @date 2023/4/6 16:46
     * @return : void
     */
    public static void sinksManyReplay(){
        Sinks.Many<Object> back = Sinks.many().replay().all();
        back.tryEmitNext(1);
        back.tryEmitNext(2);
        back.asFlux().subscribe(i -> log.info("subscribe1 i : {}",i));
        back.tryEmitNext(3);
        back.tryEmitNext(4);
        back.asFlux().subscribe(i -> log.info("subscribe2 i : {}",i));
        back.tryEmitNext(5);
    }
}
