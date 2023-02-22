package com.cn.demo.producing;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @Description producing generate
 * Flux 数据生成器
 *
 * doc <a href="https://easywheelsoft.github.io/reactor-core-zh/index.html#producing.generate">
 *
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/21 13:32
 * @Version V1.0
 */
@Slf4j
public class Demo1 {

    public static void main(String[] args) throws IOException {
        //无状态
        //notState();

        //有状态
        //hasState();
        //可回收当前状态
        clearHasState();
    }

    /**
     * @Description: 模拟数据库操作
     * @author Levi.Ding
     * @date 2023/2/2 14:29
     * @param userId :
     * @return : reactor.core.publisher.Mono<java.util.List<java.math.BigInteger>>
     */
    public static List<BigInteger> getFavorites(BigInteger userId) {

        log.info("getFavorites  : {},userId : {}" , Thread.currentThread().getName(),userId);
        List<BigInteger> list = new ArrayList<>();
        Random random = new Random();
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (1==1){
            list.add(BigInteger.valueOf(1));
            list.add(BigInteger.valueOf(2));
            list.add(BigInteger.valueOf(3));
            list.add(BigInteger.valueOf(4));
            list.add(BigInteger.valueOf(5));
            list.add(BigInteger.valueOf(6));
        }
        return list;
    }


    /**
     * @Description: 无状态数据生成器
     * @author Levi.Ding
     * @date 2023/2/21 14:48
     * @return : void
     */
    public static void notState(){
        Consumer<SynchronousSink<List<BigInteger>>> consumer = (i) -> i.next(getFavorites(BigInteger.valueOf(1)));
        Flux.generate(consumer)
                .flatMap(i -> Flux.fromIterable(i))
                .take(5)
                .doOnRequest(i-> log.info("request of {}",i))
                .limitRate(3,3)
                .subscribe(i -> log.info("flux subscribe i : {}",i));
    }


    /**
     * @Description: 有状态Flux数据生成器
     * @author Levi.Ding
     * @date 2023/2/21 15:01
     * @return : void
     */
    public static void hasState() throws IOException {
        BiFunction<Integer,SynchronousSink<List<BigInteger>>,Integer> function = (s,i)->{
            s ++ ;
            i.next(getFavorites(BigInteger.valueOf(s)));
            if (s.equals(Integer.valueOf(5))){
                i.complete();
            }
            return s;
        };

        Flux.generate(() -> 0,function)
                .take(3)
                .limitRate(2,2>>1)
                .flatMap(i -> Flux.fromIterable(i))
                .doOnRequest(i-> log.info("request of {}",i))
                .limitRate(4,4>>1)
                .subscribe(i -> log.info("flux subscribe i : {}",i));

        System.in.read();
    }

    /**
     * @Description: 可清空当前状态的数据生成器
     * @author Levi.Ding
     * @date 2023/2/21 15:38
     * @return : void
     */
    public static void clearHasState() throws IOException {
        BiFunction<Integer,SynchronousSink<List<BigInteger>>,Integer> function = (s,i)->{
            s ++ ;
            i.next(getFavorites(BigInteger.valueOf(s)));
            if (s.equals(Integer.valueOf(5))){
                i.complete();
            }
            return s;
        };

        Flux.generate(() -> 0,function,s->log.info("clear state : {}",s))
                .take(3)
                .limitRate(2,2>>1)
                .flatMap(i -> Flux.fromIterable(i))
                .doOnRequest(i-> log.info("request of {}",i))
                .limitRate(4,4>>1)
                .subscribe(i -> log.info("flux subscribe i : {}",i),e->log.info("flux error ! ",e),()->log.info("flux complete ! "));

        System.in.read();
    }
}
