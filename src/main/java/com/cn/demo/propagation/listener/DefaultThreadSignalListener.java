package com.cn.demo.propagation.listener;

import reactor.core.observability.DefaultSignalListener;
import reactor.core.publisher.SignalType;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Description DefaultThreadSignalListener
 * @Author: Levi.Ding
 * @Date: 2023/4/25 11:09
 * @Version V1.0
 */
public class DefaultThreadSignalListener<T> extends DefaultSignalListener<T> {


    Consumer consumer;

    public DefaultThreadSignalListener(Consumer consumer) {
        super();
        this.consumer =consumer;
    }

    @Override
    public void doFirst() throws Throwable {
        consumer.accept("doFirst");
    }

    @Override
    public void doFinally(SignalType terminationType) throws Throwable {
        consumer.accept(terminationType);
    }

    @Override
    public void doOnSubscription() throws Throwable {
        consumer.accept("doOnSubscription");
    }

    @Override
    public void doOnFusion(int negotiatedFusion) throws Throwable {
        consumer.accept(negotiatedFusion);
    }

    @Override
    protected int getFusionMode() {
        return super.getFusionMode();
    }

    @Override
    public void doOnRequest(long requested) throws Throwable {
        consumer.accept(requested);
    }

    @Override
    public void doOnCancel() throws Throwable {
        consumer.accept("doOnCancel");
    }

    @Override
    public void doOnNext(T value) throws Throwable {
        consumer.accept(value);
    }

    @Override
    public void doOnComplete() throws Throwable {
        consumer.accept("doOnComplete");
    }

    @Override
    public void doOnError(Throwable error) throws Throwable {
        consumer.accept(error);
    }

    @Override
    public void doAfterComplete() throws Throwable {
        consumer.accept("doAfterComplete");
    }

    @Override
    public void doAfterError(Throwable error) throws Throwable {
        consumer.accept(error);
    }

    @Override
    public void doOnMalformedOnNext(T value) throws Throwable {
        consumer.accept(value);
    }

    @Override
    public void doOnMalformedOnComplete() throws Throwable {
        consumer.accept("doOnMalformedOnComplete");
    }

    @Override
    public void doOnMalformedOnError(Throwable error) throws Throwable {
        consumer.accept(error);
    }

    @Override
    public void handleListenerError(Throwable listenerError) {
        consumer.accept(listenerError);
    }
}
