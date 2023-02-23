package com.cn.demo.producing;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * @Description Flux create/push : init push or pull
 *  Flux.create()/Flux.push 可以提供推拉混合模型 ，初始化时，消费端可以先拉数据 -> 后续进行推
 *  拉数据关键代码
 * {@link reactor.core.publisher.FluxCreate.BaseSink#request(long)} 中
 *              LongConsumer consumer = requestConsumer;
 * 				if (n > 0 && consumer != null && !isCancelled()) {
 * 					consumer.accept(n);
 *              }
 * @Author: Levi.Ding
 * @Date: 2023/2/23 15:09
 * @Version V1.0
 */
@Slf4j
public class Demo4 {


    public static void main(String[] args) throws InterruptedException {

        LinkedList<String> messages = new LinkedList<>();
        messages.add("Pull First Message");
        messages.add("Pull Second Message");

        Flux.create(
                        s -> {
                            //推模型
                            push(s);


                            //拉模型
                            s.onRequest(l -> {
                                log.info("Flux subscribe pull l : {}", l);
                                for (long i = 0; i < l; i++) {
                                    String message = messages.pollFirst();
                                    if (message == null) {
                                        break;
                                    }
                                    s.next(message);
                                }
                            });
                        }
                )
                .limitRate(1)
                .subscribe(i -> log.info("{} Flux subscribe i : {}", Thread.currentThread().getName(), i));

        synchronized (Demo4.class) {
            Demo4.class.wait();
        }
    }

    /**
     * @Description: 推模型
     * @author Levi.Ding
     * @date 2023/2/23 16:28
     * @param sink :
     * @return : void
     */
    public static void push(FluxSink sink) {
        new Thread(() -> {

            String read;
            do {

                Scanner scan = new Scanner(System.in);
                read = scan.nextLine();
                sink.next(read);
            } while (!read.equals("stop"));

            synchronized (Demo4.class) {
                Demo4.class.notifyAll();
            }
        }).start();
    }

}
