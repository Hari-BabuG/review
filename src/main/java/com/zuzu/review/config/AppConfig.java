package com.zuzu.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfig {

  // Use the concrete type so there’s no ambiguity with Boot’s TaskScheduler
  @Bean(name = "ingestionExecutor")
  public ThreadPoolTaskExecutor ingestionExecutor() {
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setCorePoolSize(4);
    exec.setMaxPoolSize(8);
    exec.setQueueCapacity(50);
    exec.setThreadNamePrefix("ingest-");
    exec.initialize();
    return exec;
  }
}
