package com.stocks.myportfolio.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("ticketAgentExecutor")
    public Executor ticketAgentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("ticket-agent-");
        executor.initialize();
        return executor;
    }

    @Bean("testRunnerExecutor")
    public Executor testRunnerExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "playwright-runner");
            t.setDaemon(true);
            return t;
        });
    }
}
