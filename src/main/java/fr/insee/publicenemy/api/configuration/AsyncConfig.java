package fr.insee.publicenemy.api.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@ConditionalOnProperty(value="feature.async.enabled", havingValue = "true")
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor delegateExecutor = new ThreadPoolTaskExecutor();
        delegateExecutor.setCorePoolSize(10);
        delegateExecutor.setMaxPoolSize(100);
        delegateExecutor.setQueueCapacity(1000);
        delegateExecutor.setThreadNamePrefix("async-task-");
        delegateExecutor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(delegateExecutor);
    }
}
