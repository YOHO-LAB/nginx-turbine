package com.turbine.data.nginx.test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhengyouwei on 2017/1/24.
 */
public class TestClass2 {

    static ConcurrentHashMap<String, HystrixOutMetrics> hystrixRollingNumberMap = new ConcurrentHashMap<>();

    static String[] keys = new String[]{"a", "b", "c", "d", "e", "f", "g"};

    static Random random = new Random();

    private static final JsonFactory jsonFactory = new JsonFactory();


    public static void main(String[] args) throws InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Iterator e = HystrixCommandMetrics.getInstances().iterator();

                        String jsonString;
                        while (e.hasNext()) {
                            HystrixCommandMetrics collapserMetrics = (HystrixCommandMetrics) e.next();
                            jsonString = getCommandJson(collapserMetrics);
                            System.out.println(jsonString);
                        }

                        e = HystrixThreadPoolMetrics.getInstances().iterator();

                        while (e.hasNext()) {
                            HystrixThreadPoolMetrics collapserMetrics1 = (HystrixThreadPoolMetrics) e.next();
                            if (hasExecutedCommandsOnThread(collapserMetrics1)) {
                                jsonString = getThreadPoolJson(collapserMetrics1);
                                System.out.println(jsonString);
                            }
                        }

                        e = HystrixCollapserMetrics.getInstances().iterator();

                        while (e.hasNext()) {
                            HystrixCollapserMetrics collapserMetrics2 = (HystrixCollapserMetrics) e.next();
                            jsonString = getCollapserJson(collapserMetrics2);
                            System.out.println(jsonString);
                        }

                    } catch (Exception var4) {
                        System.out.println(var4);
                    }
                }
            }
        }).start();

        while (true) {
            int kyeIndex = random.nextInt(7);
            if (!hystrixRollingNumberMap.containsKey(keys[kyeIndex])) {
                HystrixCommandProperties hystrixCommandProperties = HystrixPropertiesFactory.getCommandProperties(HystrixCommandKey.Factory.asKey(keys[kyeIndex]), null);
                HystrixOutMetrics hystrixOutMetrics = new HystrixOutMetrics(HystrixCommandKey.Factory.asKey(keys[kyeIndex]), HystrixCommandGroupKey.Factory.asKey(keys[kyeIndex]), hystrixCommandProperties);
                hystrixRollingNumberMap.put(keys[kyeIndex], hystrixOutMetrics);
            }
            int successOrFaileRandom = random.nextInt(10);
            boolean success = true;
            if (successOrFaileRandom > 8) {
                success = false;
            }
            long time = 1;
            time = time + random.nextInt(100);
            if (success) {
                hystrixRollingNumberMap.get(keys[kyeIndex]).markSuccess(time);
            } else {
                hystrixRollingNumberMap.get(keys[kyeIndex]).markFailure();
            }

            Thread.sleep(10);
        }
    }

    private static String getCommandJson(HystrixCommandMetrics commandMetrics) throws IOException {
        HystrixCommandKey key = commandMetrics.getCommandKey();
        HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(key);
        StringWriter jsonString = new StringWriter();
        JsonGenerator json = jsonFactory.createGenerator(jsonString);
        json.writeStartObject();
        json.writeStringField("type", "HystrixCommand");
        json.writeStringField("name", key.name());
        json.writeStringField("group", commandMetrics.getCommandGroup().name());
        json.writeNumberField("currentTime", System.currentTimeMillis());
        if (circuitBreaker == null) {
            json.writeBooleanField("isCircuitBreakerOpen", false);
        } else {
            json.writeBooleanField("isCircuitBreakerOpen", circuitBreaker.isOpen());
        }

        HystrixCommandMetrics.HealthCounts healthCounts = commandMetrics.getHealthCounts();
        json.writeNumberField("errorPercentage", healthCounts.getErrorPercentage());
        json.writeNumberField("errorCount", healthCounts.getErrorCount());
        json.writeNumberField("requestCount", healthCounts.getTotalRequests());
        json.writeNumberField("rollingCountBadRequests", commandMetrics.getRollingCount(HystrixRollingNumberEvent.BAD_REQUEST));
        json.writeNumberField("rollingCountCollapsedRequests", commandMetrics.getRollingCount(HystrixRollingNumberEvent.COLLAPSED));
        json.writeNumberField("rollingCountEmit", commandMetrics.getRollingCount(HystrixRollingNumberEvent.EMIT));
        json.writeNumberField("rollingCountExceptionsThrown", commandMetrics.getRollingCount(HystrixRollingNumberEvent.EXCEPTION_THROWN));
        json.writeNumberField("rollingCountFailure", commandMetrics.getRollingCount(HystrixRollingNumberEvent.FAILURE));
        json.writeNumberField("rollingCountFallbackEmit", commandMetrics.getRollingCount(HystrixRollingNumberEvent.FALLBACK_EMIT));
        json.writeNumberField("rollingCountFallbackFailure", commandMetrics.getRollingCount(HystrixRollingNumberEvent.FALLBACK_FAILURE));
        json.writeNumberField("rollingCountFallbackMissing", commandMetrics.getRollingCount(HystrixRollingNumberEvent.FALLBACK_MISSING));
        json.writeNumberField("rollingCountFallbackRejection", commandMetrics.getRollingCount(HystrixRollingNumberEvent.FALLBACK_REJECTION));
        json.writeNumberField("rollingCountFallbackSuccess", commandMetrics.getRollingCount(HystrixRollingNumberEvent.FALLBACK_SUCCESS));
        json.writeNumberField("rollingCountResponsesFromCache", commandMetrics.getRollingCount(HystrixRollingNumberEvent.RESPONSE_FROM_CACHE));
        json.writeNumberField("rollingCountSemaphoreRejected", commandMetrics.getRollingCount(HystrixRollingNumberEvent.SEMAPHORE_REJECTED));
        json.writeNumberField("rollingCountShortCircuited", commandMetrics.getRollingCount(HystrixRollingNumberEvent.SHORT_CIRCUITED));
        json.writeNumberField("rollingCountSuccess", commandMetrics.getRollingCount(HystrixRollingNumberEvent.SUCCESS));
        json.writeNumberField("rollingCountThreadPoolRejected", commandMetrics.getRollingCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED));
        json.writeNumberField("rollingCountTimeout", commandMetrics.getRollingCount(HystrixRollingNumberEvent.TIMEOUT));
        json.writeNumberField("currentConcurrentExecutionCount", commandMetrics.getCurrentConcurrentExecutionCount());
        json.writeNumberField("rollingMaxConcurrentExecutionCount", commandMetrics.getRollingMaxConcurrentExecutions());
        json.writeNumberField("latencyExecute_mean", commandMetrics.getExecutionTimeMean());
        json.writeObjectFieldStart("latencyExecute");
        json.writeNumberField("0", commandMetrics.getExecutionTimePercentile(0.0D));
        json.writeNumberField("25", commandMetrics.getExecutionTimePercentile(25.0D));
        json.writeNumberField("50", commandMetrics.getExecutionTimePercentile(50.0D));
        json.writeNumberField("75", commandMetrics.getExecutionTimePercentile(75.0D));
        json.writeNumberField("90", commandMetrics.getExecutionTimePercentile(90.0D));
        json.writeNumberField("95", commandMetrics.getExecutionTimePercentile(95.0D));
        json.writeNumberField("99", commandMetrics.getExecutionTimePercentile(99.0D));
        json.writeNumberField("99.5", commandMetrics.getExecutionTimePercentile(99.5D));
        json.writeNumberField("100", commandMetrics.getExecutionTimePercentile(100.0D));
        json.writeEndObject();
        json.writeNumberField("latencyTotal_mean", commandMetrics.getTotalTimeMean());
        json.writeObjectFieldStart("latencyTotal");
        json.writeNumberField("0", commandMetrics.getTotalTimePercentile(0.0D));
        json.writeNumberField("25", commandMetrics.getTotalTimePercentile(25.0D));
        json.writeNumberField("50", commandMetrics.getTotalTimePercentile(50.0D));
        json.writeNumberField("75", commandMetrics.getTotalTimePercentile(75.0D));
        json.writeNumberField("90", commandMetrics.getTotalTimePercentile(90.0D));
        json.writeNumberField("95", commandMetrics.getTotalTimePercentile(95.0D));
        json.writeNumberField("99", commandMetrics.getTotalTimePercentile(99.0D));
        json.writeNumberField("99.5", commandMetrics.getTotalTimePercentile(99.5D));
        json.writeNumberField("100", commandMetrics.getTotalTimePercentile(100.0D));
        json.writeEndObject();
        HystrixCommandProperties commandProperties = commandMetrics.getProperties();
        json.writeNumberField("propertyValue_circuitBreakerRequestVolumeThreshold", ((Integer) commandProperties.circuitBreakerRequestVolumeThreshold().get()).intValue());
        json.writeNumberField("propertyValue_circuitBreakerSleepWindowInMilliseconds", ((Integer) commandProperties.circuitBreakerSleepWindowInMilliseconds().get()).intValue());
        json.writeNumberField("propertyValue_circuitBreakerErrorThresholdPercentage", ((Integer) commandProperties.circuitBreakerErrorThresholdPercentage().get()).intValue());
        json.writeBooleanField("propertyValue_circuitBreakerForceOpen", ((Boolean) commandProperties.circuitBreakerForceOpen().get()).booleanValue());
        json.writeBooleanField("propertyValue_circuitBreakerForceClosed", ((Boolean) commandProperties.circuitBreakerForceClosed().get()).booleanValue());
        json.writeBooleanField("propertyValue_circuitBreakerEnabled", ((Boolean) commandProperties.circuitBreakerEnabled().get()).booleanValue());
        json.writeStringField("propertyValue_executionIsolationStrategy", ((HystrixCommandProperties.ExecutionIsolationStrategy) commandProperties.executionIsolationStrategy().get()).name());
        json.writeNumberField("propertyValue_executionIsolationThreadTimeoutInMilliseconds", ((Integer) commandProperties.executionTimeoutInMilliseconds().get()).intValue());
        json.writeNumberField("propertyValue_executionTimeoutInMilliseconds", ((Integer) commandProperties.executionTimeoutInMilliseconds().get()).intValue());
        json.writeBooleanField("propertyValue_executionIsolationThreadInterruptOnTimeout", ((Boolean) commandProperties.executionIsolationThreadInterruptOnTimeout().get()).booleanValue());
        json.writeStringField("propertyValue_executionIsolationThreadPoolKeyOverride", (String) commandProperties.executionIsolationThreadPoolKeyOverride().get());
        json.writeNumberField("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests", ((Integer) commandProperties.executionIsolationSemaphoreMaxConcurrentRequests().get()).intValue());
        json.writeNumberField("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests", ((Integer) commandProperties.fallbackIsolationSemaphoreMaxConcurrentRequests().get()).intValue());
        json.writeNumberField("propertyValue_metricsRollingStatisticalWindowInMilliseconds", ((Integer) commandProperties.metricsRollingStatisticalWindowInMilliseconds().get()).intValue());
        json.writeBooleanField("propertyValue_requestCacheEnabled", ((Boolean) commandProperties.requestCacheEnabled().get()).booleanValue());
        json.writeBooleanField("propertyValue_requestLogEnabled", ((Boolean) commandProperties.requestLogEnabled().get()).booleanValue());
        json.writeNumberField("reportingHosts", 1);
        json.writeStringField("threadPool", commandMetrics.getThreadPoolKey().name());
        json.writeEndObject();
        json.close();
        return jsonString.getBuffer().toString();
    }

    private static boolean hasExecutedCommandsOnThread(HystrixThreadPoolMetrics threadPoolMetrics) {
        return threadPoolMetrics.getCurrentCompletedTaskCount().intValue() > 0;
    }

    private static String getThreadPoolJson(HystrixThreadPoolMetrics threadPoolMetrics) throws IOException {
        HystrixThreadPoolKey key = threadPoolMetrics.getThreadPoolKey();
        StringWriter jsonString = new StringWriter();
        JsonGenerator json = jsonFactory.createJsonGenerator(jsonString);
        json.writeStartObject();
        json.writeStringField("type", "HystrixThreadPool");
        json.writeStringField("name", key.name());
        json.writeNumberField("currentTime", System.currentTimeMillis());
        json.writeNumberField("currentActiveCount", threadPoolMetrics.getCurrentActiveCount().intValue());
        json.writeNumberField("currentCompletedTaskCount", threadPoolMetrics.getCurrentCompletedTaskCount().longValue());
        json.writeNumberField("currentCorePoolSize", threadPoolMetrics.getCurrentCorePoolSize().intValue());
        json.writeNumberField("currentLargestPoolSize", threadPoolMetrics.getCurrentLargestPoolSize().intValue());
        json.writeNumberField("currentMaximumPoolSize", threadPoolMetrics.getCurrentMaximumPoolSize().intValue());
        json.writeNumberField("currentPoolSize", threadPoolMetrics.getCurrentPoolSize().intValue());
        json.writeNumberField("currentQueueSize", threadPoolMetrics.getCurrentQueueSize().intValue());
        json.writeNumberField("currentTaskCount", threadPoolMetrics.getCurrentTaskCount().longValue());
        json.writeNumberField("rollingCountThreadsExecuted", threadPoolMetrics.getRollingCount(HystrixRollingNumberEvent.THREAD_EXECUTION));
        json.writeNumberField("rollingMaxActiveThreads", threadPoolMetrics.getRollingMaxActiveThreads());
        json.writeNumberField("rollingCountCommandRejections", threadPoolMetrics.getRollingCount(HystrixRollingNumberEvent.THREAD_POOL_REJECTED));
        json.writeNumberField("propertyValue_queueSizeRejectionThreshold", ((Integer) threadPoolMetrics.getProperties().queueSizeRejectionThreshold().get()).intValue());
        json.writeNumberField("propertyValue_metricsRollingStatisticalWindowInMilliseconds", ((Integer) threadPoolMetrics.getProperties().metricsRollingStatisticalWindowInMilliseconds().get()).intValue());
        json.writeNumberField("reportingHosts", 1);
        json.writeEndObject();
        json.close();
        return jsonString.getBuffer().toString();
    }

    private static String getCollapserJson(HystrixCollapserMetrics collapserMetrics) throws IOException {
        HystrixCollapserKey key = collapserMetrics.getCollapserKey();
        StringWriter jsonString = new StringWriter();
        JsonGenerator json = jsonFactory.createJsonGenerator(jsonString);
        json.writeStartObject();
        json.writeStringField("type", "HystrixCollapser");
        json.writeStringField("name", key.name());
        json.writeNumberField("currentTime", System.currentTimeMillis());
        json.writeNumberField("rollingCountRequestsBatched", collapserMetrics.getRollingCount(HystrixRollingNumberEvent.COLLAPSER_REQUEST_BATCHED));
        json.writeNumberField("rollingCountBatches", collapserMetrics.getRollingCount(HystrixRollingNumberEvent.COLLAPSER_BATCH));
        json.writeNumberField("rollingCountResponsesFromCache", collapserMetrics.getRollingCount(HystrixRollingNumberEvent.RESPONSE_FROM_CACHE));
        json.writeNumberField("batchSize_mean", collapserMetrics.getBatchSizeMean());
        json.writeObjectFieldStart("batchSize");
        json.writeNumberField("25", collapserMetrics.getBatchSizePercentile(25.0D));
        json.writeNumberField("50", collapserMetrics.getBatchSizePercentile(50.0D));
        json.writeNumberField("75", collapserMetrics.getBatchSizePercentile(75.0D));
        json.writeNumberField("90", collapserMetrics.getBatchSizePercentile(90.0D));
        json.writeNumberField("95", collapserMetrics.getBatchSizePercentile(95.0D));
        json.writeNumberField("99", collapserMetrics.getBatchSizePercentile(99.0D));
        json.writeNumberField("99.5", collapserMetrics.getBatchSizePercentile(99.5D));
        json.writeNumberField("100", collapserMetrics.getBatchSizePercentile(100.0D));
        json.writeEndObject();
        json.writeBooleanField("propertyValue_requestCacheEnabled", ((Boolean) collapserMetrics.getProperties().requestCacheEnabled().get()).booleanValue());
        json.writeNumberField("propertyValue_maxRequestsInBatch", ((Integer) collapserMetrics.getProperties().maxRequestsInBatch().get()).intValue());
        json.writeNumberField("propertyValue_timerDelayInMilliseconds", ((Integer) collapserMetrics.getProperties().timerDelayInMilliseconds().get()).intValue());
        json.writeNumberField("reportingHosts", 1);
        json.writeEndObject();
        json.close();
        return jsonString.getBuffer().toString();
    }
}
