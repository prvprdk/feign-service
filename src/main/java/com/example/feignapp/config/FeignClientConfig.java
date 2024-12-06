package com.example.feignapp.config;


import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${spring.feign.client.config.default.retry.max-attempts}")
    private int maxAttempts;
    @Value("${spring.feign.client.config.default.retry.max-period}")
    private int maxPeriod;
    @Value("${spring.feign.client.config.default.retry.period}")
    private int period;

    @Bean
    public Retryer feingRetryer() {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }
}
