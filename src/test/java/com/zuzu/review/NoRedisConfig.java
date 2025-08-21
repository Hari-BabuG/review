package com.zuzu.review;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ImportAutoConfiguration(exclude = {
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,
    RedisReactiveAutoConfiguration.class
})
public class NoRedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> mock = Mockito.mock(RedisTemplate.class);
        when(mock.hasKey(anyString())).thenReturn(false);
        return mock;
    }
}

