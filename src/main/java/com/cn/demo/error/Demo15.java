package com.cn.demo.error;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @Description
 * Flux error  VS   try-catch
 *
 * @Author: Levi.Ding
 * @Date: 2023/3/17 16:00
 * @Version V1.0
 */
@Slf4j
public class Demo15 {

    public static void main(String[] args) {
//        onErrorVsTryCatch();
//        onErrorReturnVsTryCatch();
//        onErrorCompleteVsTryCatch();
//        onErrorResumeVsOnErrorReturn();
//        onErrorResumeVsTryCatch();
        onErrorMapVsTryCatch();
    }


    /**
     * @Description: onErrorVsTryCatch
     * @author Levi.Ding
     * @date 2023/3/22 15:03
     * @return : void
     */
    public static void onErrorVsTryCatch(){
        log.info("-----------------{}---------------","onError");
        onError();

        log.info("-----------------{}---------------","tryCatchOnError");
        tryCatchOnError();

    }

    /**
     * @Description: Flux onError
     * @author Levi.Ding
     * @date 2023/3/22 15:01
     * @return : void
     */
    public static void onError(){
        Flux.range(1,10).map(i->{
            i = i * 2;
            if (i == 10){
                throw new RuntimeException("Test onError");
            }
            return i;
        }).subscribe(i->log.info("i:{}",i),e->log.info("e:{}",e));
    }

    /**
     * @Description: Flux onError slab : try-catch
     * @author Levi.Ding
     * @date 2023/3/22 15:01
     * @return : void
     */
    public static void tryCatchOnError(){
        try {
            for (int i = 1; i < 10; i++) {
                if (i*2 == 10){
                    throw new RuntimeException("Test tryCatchOnError");
                }
                log.info("i:{}",i*2);
            }
        }catch (Exception e){
            log.info("e:{}",e);
        }
    }

    /**
     * @Description: onErrorReturnVsTryCatch
     * @author Levi.Ding
     * @date 2023/3/22 15:03
     * @return : void
     */
    public static void onErrorReturnVsTryCatch(){
        log.info("-----------------{}---------------","onErrorReturn");
        onErrorReturn();

        log.info("-----------------{}---------------","tryCatchOnErrorReturn");
        tryCatchOnErrorReturn();
    }

    /**
     * @Description: Flux onErrorReturn
     * @author Levi.Ding
     * @date 2023/3/22 15:07
     * @return : void
     */
    public static void onErrorReturn(){
        Flux.range(1,10).map(i->{
           i = i*2;
           if (i == 10){
               throw new RuntimeException("Test onError");
           }
           return i;
        }).onErrorReturn(e -> e.getMessage().equals("Test onError"),10).subscribe(i->log.info("i:{}",i),e->log.info("e:{}",e));
    }

    /**
     * @Description: Flux onErrorReturn slab : try-catch
     * @author Levi.Ding
     * @date 2023/3/22 15:01
     * @return : void
     */
    public static void tryCatchOnErrorReturn(){
        try {
            for (int i = 1; i < 10; i++) {
                if (i*2 == 10){
                    throw new RuntimeException("Test tryCatchOnError");
                }
                log.info("i:{}",i*2);
            }
        }catch (Exception e){
            if (e.getMessage().equals("Test tryCatchOnError")){
                log.info("i:{}",10);
            }else{
                log.info("e:{}",e);
            }
        }
    }

    /**
     * @Description: onErrorCompleteVsTryCatch
     * @author Levi.Ding
     * @date 2023/3/22 15:03
     * @return : void
     */
    public static void onErrorCompleteVsTryCatch(){
        log.info("-----------------{}---------------","onErrorComplete");
        onErrorComplete();

        log.info("-----------------{}---------------","tryCatchOnErrorComplete");
        tryCatchOnErrorComplete();
    }

    /**
     * @Description: Flux onErrorComplete
     * @author Levi.Ding
     * @date 2023/3/22 15:07
     * @return : void
     */
    public static void onErrorComplete(){
        Flux.range(1,10).map(i->{
            i = i*2;
            if (i == 10){
                throw new RuntimeException("Test onError");
            }
            return i;
        }).onErrorComplete().subscribe(i->log.info("i:{}",i),e->log.info("e:{}",e),()->log.info("complete!"));
    }

    /**
     * @Description: Flux onErrorComplete slab : try-catch
     * @author Levi.Ding
     * @date 2023/3/22 15:01
     * @return : void
     */
    public static void tryCatchOnErrorComplete(){
        try {
            for (int i = 1; i < 10; i++) {
                if (i*2 == 10){
                    throw new RuntimeException("Test tryCatchOnError");
                }
                log.info("i:{}",i*2);
            }
        }catch (Exception e){

        }
        log.info("complete!");
    }


    /**
     * @Description:
     * 动态流,静态流
     *
     * 动态流
     * {@link reactor.core.publisher.FluxOnErrorResume.ResumeSubscriber#onError(Throwable)} 中 基于失败流重新发布消息
     * p = Objects.requireNonNull(nextFactory.apply(t),
     * 					"The nextFactory returned a null Publisher");
     * p.subscribe(this);
     *
     * 静态流
     * {@link reactor.core.publisher.FluxOnErrorReturn.ReturnSubscriber#onError(Throwable)} 中 actual.onNext(this.fallbackValue);
     *
     * @author Levi.Ding
     * @date 2023/3/23 14:19
     * @return : void
     */
    public static void onErrorResumeVsOnErrorReturn(){
        log.info("-------------------{}---------------------","动态流");
        Flux.range(1,10).map(i -> {
            log.info("i : {}",i);
            i = i*2;
            throw new RuntimeException();
//            return i;
        }).onErrorResume(e -> Mono.fromSupplier(()->{
            return fallbackValue();
        })).subscribe(i->log.info("i:{}",i),e->log.info("e:{}",e),()->log.info("complete!"));

        log.info("-------------------{}---------------------","静态流");
        Flux.range(1,10).map(i -> {
            log.info("i : {}",i);
            i = i*2;
            throw new RuntimeException();
//            return i;
        }).onErrorReturn(fallbackValue()).subscribe(i->log.info("i:{}",i),e->log.info("e:{}",e),()->log.info("complete!"));
    }


    public static Integer fallbackValue(){
        log.info("error fallbackValue");
        return 100;
    }


    /**
     * @Description: onErrorResume VS TryCatch
     * @author Levi.Ding
     * @date 2023/3/23 16:28
     * @return : void
     */
    public static void onErrorResumeVsTryCatch(){
        log.info("-------------------{}---------------------","onErrorResume");
        Flux.range(1,10).flatMap(i -> {
            return Mono.fromSupplier(()->{
                int j = i*2;
                if (j == 10){
                    throw new RuntimeException();
                }
                return j;
            }).onErrorResume(e -> Mono.fromSupplier(() -> fallbackValue()));
        }).subscribe(i->log.info("i:{}",i),e->log.info("e:{}",e),()->log.info("complete!"));


        log.info("-------------------{}---------------------","try-catch");


        for (int i = 1; i < 10; i++) {
            try {
                if (i*2 == 10){
                    throw new RuntimeException("Test tryCatchOnError");
                }
                log.info("i:{}",i*2);
            }catch (Exception e){
                log.info("i:{}",fallbackValue());
            }
        }
        log.info("complete!");
    }


    /**
     * @Description: onErrorMap Vs TryCatch
     * @author Levi.Ding
     * @date 2023/3/23 17:34
     * @return : void
     */
    public static void onErrorMapVsTryCatch() {
        log.info("-------------------{}---------------------", "onErrorMap");
        Flux.range(1, 10).flatMap(i -> {
                    int j = i * 2;
                    if (j == 10) {
                        throw new RuntimeException("FlatMap Exception");
                    }
                    return Flux.just(j);
                })
                .onErrorMap(o -> new RuntimeException("ErrorMap Exception"))
                .subscribe(i -> log.info("i:{}", i), e -> log.info("e:{}", e), () -> log.info("complete!"));


        log.info("-------------------{}---------------------", "try-catch");


        try {
            for (int i = 1; i < 10; i++) {
                if (i * 2 == 10) {
                    throw new RuntimeException("Test tryCatchOnError");
                }
                log.info("i:{}", i * 2);

            }
        } catch (Exception e) {
            throw new RuntimeException("ErrorMap Exception");
        }
    }
}
