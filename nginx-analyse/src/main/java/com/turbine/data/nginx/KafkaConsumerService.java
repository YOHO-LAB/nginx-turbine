package com.turbine.data.nginx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhengyouwei on 2017/2/6.
 */
public class KafkaConsumerService {

    private static Pattern p = Pattern.compile("method=[a-zA-Z[\\\\.]]+"); // 匹配方法

    static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    static final ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();

    public void processMessage(Map<String, Map<Integer, List<String>>> msgs) {
        for (Map.Entry<String, Map<Integer, List<String>>> entry :
                msgs.entrySet()) {
            for (List<String> msgList : entry.getValue().values()) {
                for (String msg : msgList) {
                    try {
                        JSONObject jsonObject = JSON.parseObject(msg);
                        String message = String.valueOf(jsonObject.get("message"));
                        String[] arrays = message.split("\\|");
                        String remote_addr = arrays[0];
                        String http_x_forwarded_for = arrays[1];
                        String time_local = arrays[2];
                        String http_host = arrays[3];
                        String request = arrays[4];
                        String status = arrays[5];
                        String body_bytes_sent = arrays[6];
                        String request_time = arrays[7];
                        String upstream_response_time = arrays[8];
                        String upstream_cache_status = arrays[9];
                        String http_referer = arrays[10];
                        String http_user_agent = arrays[11];
                        String upstream_addr = arrays[12];

                        String[] requestArray = request.split(" ");
                        if (requestArray.length != 3) {
                            logger.info("processMessage null:{}", message);
                            continue;
                        }
                        if ("POST".equals(requestArray[0])){
                            continue;
                        }
                        String method = methodMatch(requestArray[1]);
                        if (method == null) {
                            if (requestArray[1].indexOf("?") != -1) {
                                method = requestArray[1].substring(0, requestArray[1].indexOf("?"));
                            }
                        } else {
                            method = method.substring(7);
                        }
                        if (method == null) {
                            logger.info("processMessage null:{}", message);
                            continue;
                        }
                        long duration = (long) (Float.valueOf(request_time) * 1000);
                        if (Integer.valueOf(status) == 200) {
                            OutMetricsContainer.success(method, duration, http_host);
                        } else OutMetricsContainer.fail(method, http_host);
                    } catch (Exception e) {
                        logger.error("processMessage failed! msg:{},exception:{}", msg, e.toString());
                    }
                }
            }
        }
    }

    /**
     * 匹配method
     *
     * @param bodyvalue
     * @return
     */

    private String methodMatch(String bodyvalue) {
        Matcher matcher = p.matcher(bodyvalue); // 操作的字符串
        boolean find = matcher.find();
        if (find) {
            return matcher.group();
        }
        return null;
    }

}
