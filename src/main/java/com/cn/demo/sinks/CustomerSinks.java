package com.cn.demo.sinks;

import com.cn.demo.error.Demo16;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.Scanner;

/**
 * @Description Sinks
 * @Author: Levi.Ding
 * @Date: 2023/3/31 14:45
 * @Version V1.0
 */
@Slf4j
public class CustomerSinks {


    public static void main(String[] args) throws IOException, InterruptedException {
        testSinks();
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
}
