package com.cn.demo.producing;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * @Description Flux handle
 * handle 过滤器，可以用于过滤当前生成的流数据，能否往下执行，或直接结束
 * 关键代码
 * {@link reactor.core.publisher.FluxHandle.HandleSubscriber#onNext(Object)} 中
 *          handler.accept(t, this);
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#_handle">
 *
 * @Author: Levi.Ding
 * @Date: 2023/2/23 18:03
 * @Version V1.0
 */
@Slf4j
public class Demo6 {


    public static void main(String[] args) {
        Flux.range(1,10)
                .handle((i,s)->{
                    if (i > 5){
                        s.complete();
                        return;
                    }
                    s.next(i);
        }).subscribe(i -> log.info("Flux subscribe i : {}",i),e -> log.info("",e),()-> log.info("success"));
    }
}
