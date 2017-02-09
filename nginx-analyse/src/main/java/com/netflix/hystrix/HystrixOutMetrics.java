package com.netflix.hystrix;

/**
 * Created by zhengyouwei on 2017/1/24.
 */
public class HystrixOutMetrics {

    private HystrixCommandMetrics hystrixCommandMetrics;

    public HystrixOutMetrics(HystrixCommandKey key, HystrixCommandGroupKey commandGroup, HystrixCommandProperties properties) {
        this.hystrixCommandMetrics = HystrixCommandMetrics.getInstance(key,commandGroup,properties);
    }

    public void markSuccess(long duration) {
        hystrixCommandMetrics.markSuccess(0);
        hystrixCommandMetrics.addCommandExecutionTime(duration);
    }

    public void markFailure() {
        hystrixCommandMetrics.markFailure(0);
    }

}
