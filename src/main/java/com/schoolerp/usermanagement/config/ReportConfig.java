package com.schoolerp.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ReportConfig {

    @Bean(name = "reportTaskExecutor")
    public Executor reportExecutor() {

        ThreadPoolTaskExecutor excutor = new ThreadPoolTaskExecutor();
        excutor.setCorePoolSize(5);
        excutor.setMaxPoolSize(20);
        excutor.setQueueCapacity(100);
        excutor.setThreadNamePrefix("report-");
        excutor.initialize();
        return excutor;
    }


}
