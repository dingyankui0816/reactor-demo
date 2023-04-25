package com.cn.demo.propagation.accessor;

import com.cn.demo.propagation.Demo1;
import io.micrometer.context.ThreadLocalAccessor;

/**
 * @Description  DefaultThreadLocalAccessor
 * @Author: Levi.Ding
 * @Date: 2023/4/24 17:34
 * @Version V1.0
 */
public class DefaultThreadLocalAccessor implements ThreadLocalAccessor<String> {
    @Override
    public String key() {
        return "Demo1.threadLocal";
    }

    @Override
    public String getValue() {
        return Demo1.threadLocal.get();
    }

    @Override
    public void setValue(String value) {
        Demo1.threadLocal.set(value);
    }

    @Override
    public void reset() {
        Demo1.threadLocal.remove();
    }
}
