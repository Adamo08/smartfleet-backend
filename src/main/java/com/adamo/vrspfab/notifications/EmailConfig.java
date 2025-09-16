package com.adamo.vrspfab.notifications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EmailConfig {

    /**
     * Creates a RestTemplate bean for SendPulse email service.
     * @return RestTemplate instance configured for email API calls.
     */
    @Bean("emailRestTemplate")
    public RestTemplate emailRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Primary task executor bean for @Async to avoid ambiguous executors warning.
     */
    @Bean(name = "taskExecutor")
    public org.springframework.core.task.TaskExecutor primaryTaskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
