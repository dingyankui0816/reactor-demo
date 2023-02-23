package com.cn.demo.producing;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @Description Flux clear
 * onDispose 终止时清理 （出错或者取消时触发） 终止操作
 * onCancel 取消信号
 * 执行顺序 关键代码
 *
 *
 * {@link reactor.core.publisher.FluxCreate.BaseSink#disposeResource(boolean)} 中
 *                 if (d != null && d != TERMINATED && d != CANCELLED) {
 * 					if (isCancel && d instanceof SinkDisposable) {
 * 						((SinkDisposable) d).cancel();
 *                  }
 * 					d.dispose();
 * 				}
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_cleaning_up_after_push_or_create">
 *
 * 取消时  cancel ->  dispose
 * 报错时  dispose
 * 完成时  dispose
 * @Author: Levi.Ding
 * @Date: 2023/2/23 16:56
 * @Version V1.0
 */
@Slf4j
public class Demo5 {


    public static void main(String[] args) {
        Disposable first = Flux.create(s -> {
            s.next("First");
            s.onCancel(() -> log.info("Flux Cancel!"));
            s.onDispose(() -> log.info("Flux Dispose!"));
//            s.complete();
        }).delayElements(Duration.ofSeconds(10)).subscribe(i -> log.info("Flux Subscribe i : {}", i));

        first.dispose();


    }
}
