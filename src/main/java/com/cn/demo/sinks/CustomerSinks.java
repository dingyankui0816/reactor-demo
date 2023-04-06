package com.cn.demo.sinks;

import com.cn.demo.error.Demo16;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @Description Sinks
 * @Author: Levi.Ding
 * @Date: 2023/3/31 14:45
 * @Version V1.0
 */
@Slf4j
public class CustomerSinks {


    public static void main(String[] args) throws IOException, InterruptedException {
//        testSinks();
//        testOneSinks();
        sinksManyType();
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
     * @Description: Sinks.one()，创建多订阅者模式的Sink,当前Sink只支持next 一条数据
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




    @SneakyThrows
    public  static void sinksManyType(){

        sinksManyMulticast();

    }


    public static void sinksManyMulticast() throws IOException {
        Sinks.MulticastSpec multicast = Sinks.many().multicast();

        //直接传输(不支持背压)
        Sinks.Many<Object> directMany = multicast.directBestEffort();
        directMany.asFlux().limitRate(100).subscribe(i -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("direct subscribe i : {}",i);
        });
        for (int i = 0; i < 10; i++) {

            createThread(i,"direct",directMany);
        }

        //支持背压
        Sinks.Many<Object> backMany = multicast.onBackpressureBuffer();
        backMany.asFlux().limitRate(100).subscribe(i -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("back subscribe i : {}",i);
        });
            for (int i = 0; i < 10; i++) {

                createThread(i,"back",backMany);
            }

        System.in.read();

    }

    /**
     * @Description: 创建线程
     * @author Levi.Ding
     * @date 2023/4/4 17:51
     * @param i :
     * @param type :
     * @param many :
     * @return : void
     */
    public static void createThread(int i,String type,Sinks.Many<Object> many){
        new Thread(() -> {
            long start = System.currentTimeMillis();
            many.tryEmitNext(i);
            log.info("{} send message sum : {} ms",type,System.currentTimeMillis() - start);
        }).start();
    }
}
