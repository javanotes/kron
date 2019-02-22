package org.reactiveminds.kron;

import javax.annotation.PostConstruct;

import org.reactiveminds.kron.core.DistributionService;
import org.reactiveminds.kron.err.KronConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastJpaDependencyAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootApplication(exclude = {HazelcastAutoConfiguration.class, HazelcastJpaDependencyAutoConfiguration.class})
public class KronDaemon implements ApplicationContextAware{
	
	private static final Logger log = LoggerFactory.getLogger("KronDaemon");
	public static void main(String[] args) {
		SpringApplication.run(KronDaemon.class);
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		KronDaemon.applicationContext = applicationContext;
	}
	@PostConstruct
	private void isRuntimeReady() {
		if(applicationContext == null) {
			throw new KronConfigurationException("Spring applicationContext not initialized!");
		}
		log.info("Node running mode: " + (isWorker() ? "WORKER" : "MASTER"));
	}
	/**
	 * 
	 * @return
	 */
	private static boolean isWorker() {
		return applicationContext.getBean(DistributionService.class).isWorkerNode();
	}
	private static volatile ApplicationContext applicationContext;
	
	public static <T> T getBean(Class<T> requiredType) {
		if(applicationContext == null) {
			throw new KronConfigurationException("Spring applicationContext not initialized!");
		}
		return applicationContext.getBean(requiredType);
	}
}
