package org.reactiveminds.kron.core.scheduler;

import org.reactiveminds.kron.core.SchedulingSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfiguration implements SchedulingConfigurer{

	@Value("${kron.scheduler.poolSize:4}")
	private int masterPoolSize;
	@Value("${kron.executor.poolSize:10}")
	private int workerPoolSize;
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
	}
	
	@Lazy
	@Bean
	TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(masterPoolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("KronScheduler-");
        threadPoolTaskScheduler.initialize();
		return threadPoolTaskScheduler;
	}
	@Bean
	ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(workerPoolSize);
		executor.setThreadNamePrefix("KronExecutor-");
		return executor;
	}
	@Bean
	SchedulingSupport executorSupport() {
		return new SpringSchedulingSupport();
	}
}
