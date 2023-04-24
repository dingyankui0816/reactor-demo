package com.cn.demo.propagation;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @Description Flux contextCapture
 *
 * 使用条件：reactor-core 版本 >= 3.5.0
 * 引入 io.micrometer:context-propagation SPI的支持
 * context-propagation使用: doc <a href="https://micrometer.io/docs/contextPropagation">
 *
 *
 *
 * doc <a href="https://projectreactor.io/docs/core/release/reference/index.html#context.propagation">
 *
 * @Author: Levi.Ding
 * @Date: 2023/4/24 16:03
 * @Version V1.0
 */
@Slf4j
public class Demo1 {


    public static void main(String[] args) {
        compatibleThreadLocal();
    }


    public static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * @Description: 通过context-propagation 中 ContextSnapshot 快照当前线程中的 ThreadLocal
     * 将快照数据通过 {@link  reactor.core.publisher.MonoContextWrite} 进行传输
     * @author Levi.Ding
     * @date 2023/4/24 18:12
     * @return : void
     */
    public static void compatibleThreadLocal(){
        threadLocal.set("Hello");

        String mono = Mono.deferContextual(ctx ->
                        Mono.delay(Duration.ofSeconds(1))
                            .map(v -> "delayed :" + v + "ms threadLocalValue : " + threadLocal.get() + " context : " + ctx)
                )
                .contextCapture()
                .block();

        log.info("value : {}",mono);
    }
}
