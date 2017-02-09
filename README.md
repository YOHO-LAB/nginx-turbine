# nginx-turbine
基于logstash,kafka,hystrix,turbine的nginx日志展示流系统

## logstash 设置
``` file
input {
    file {
        type => "aws_nginx_api_access"
        path => [
                "/Data/logs/nginx/api.*.log"
        ]
        exclude => ["*.gz","catalina*","localhost*","manage*"]
        add_field => {
            "log_ip" => "1.1.1.1"
        }
        start_position => "beginning"
        sincedb_path => "/opt/logstash/sincedb-api-access"
        codec => multiline {
                        pattern => "^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"
                        negate => "true"
                        what => "previous"
                           }
    }
}

filter {
    ruby {
        code => "
                 event['log_filename']=event['path'].gsub(/\/.*\//,'').downcase
                 log_date=Time.now.strftime('%Y-%m-%d')
                 log_htime=Time.now.strftime('%H:%M:%S')
                 event['log_date']=log_date
                 event['log_year']=log_date.split('-')[0]
                 event['log_month']=log_date.split('-')[1]
                 event['log_hour']=log_htime.split(':')[0]
        "
    }

}

output {
    kafka {
        bootstrap_servers => "1.1.1.2:9092"
        topic_id => "aws_nginx_api_access"
        workers => 5
            }
}
```


## 消费kafka消息

    程序消费kafka消息后，组装成类似hystrix调用请求，形成hystrix的请求流：
``` java
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
```

## 使用turbine展示
<center><img src="https://github.com/Netflix/Hystrix/wiki/images/hystrix-dashboard-netflix-api-example-iPad.png"></center>
