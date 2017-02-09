package com.turbine.data.nginx;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixOutMetrics;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhengyouwei on 2017/2/6.
 */
public class OutMetricsContainer {

    private static ConcurrentHashMap<String, HystrixOutMetrics> metricsConcurrentHashMap = new ConcurrentHashMap<>();

    public static void success(String method,long duration,String host){
        if (!metricsConcurrentHashMap.containsKey(method)){
            create(method,host);
        }
        metricsConcurrentHashMap.get(method).markSuccess(duration);
    }

    public static void fail(String method,String host){
        if (!metricsConcurrentHashMap.containsKey(method)){
            create(method,host);
        }
        metricsConcurrentHashMap.get(method).markFailure();
    }

    private synchronized static void create(String method,String host){
        if (!metricsConcurrentHashMap.containsKey(method)){
            HystrixCommandProperties hystrixCommandProperties = HystrixPropertiesFactory.getCommandProperties(HystrixCommandKey.Factory.asKey(method), null);
            HystrixOutMetrics hystrixOutMetrics = new HystrixOutMetrics(HystrixCommandKey.Factory.asKey(method), HystrixCommandGroupKey.Factory.asKey(host), hystrixCommandProperties);
            metricsConcurrentHashMap.put(method, hystrixOutMetrics);
        }
    }

}
