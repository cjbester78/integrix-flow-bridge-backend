package com.integrixs.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for event-driven architecture.
 * 
 * <p>Configures async event processing and thread pools.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
public class EventDrivenConfig implements AsyncConfigurer {
    
    /**
     * Creates the event executor for async event processing.
     * 
     * @return configured thread pool executor
     */
    @Bean(name = "eventExecutor")
    public TaskExecutor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-handler-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * Configures the application event multicaster for async event processing.
     * 
     * @param eventExecutor the event executor
     * @return configured event multicaster
     */
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster(@Qualifier("eventExecutor") TaskExecutor eventExecutor) {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(eventExecutor);
        eventMulticaster.setErrorHandler(throwable -> 
            log.error("Error in event listener", throwable)
        );
        return eventMulticaster;
    }
    
    @Override
    public Executor getAsyncExecutor() {
        return eventExecutor();
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("Uncaught async exception in method: {} with params: {}", 
                     method.getName(), objects, throwable);
        };
    }
}