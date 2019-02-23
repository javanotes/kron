package org.reactiveminds.kron.core.impl;

import java.io.IOException;
import java.net.URL;

import org.reactiveminds.kron.core.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@ConditionalOnProperty(name = "kron.master", havingValue = "true")
@Configuration
class HazelcastServerConfiguration {
	
	@Value("${spring.hazelcast.config:}")
	private String configXml;
	@Value("${kron.store.enabled:false}")
	private boolean storeEnabled;
	@Autowired
	ApplicationContext context;
	private static Config getConfig(Resource configLocation) throws IOException {
		if(configLocation == null)
			return new XmlConfigBuilder().build();
		
		URL configUrl = configLocation.getURL();
		Config config = new XmlConfigBuilder(configUrl).build();
		if (ResourceUtils.isFileURL(configUrl)) {
			config.setConfigurationFile(configLocation.getFile());
		}
		else {
			config.setConfigurationUrl(configUrl);
		}
		return config;
	}
	@Lazy
	@Bean
	JobMasterStore aJobMasterStore() {
		return new JobMasterStore();
	}
	@Lazy
	@Bean
	JobRunStore aJobRunStore() {
		return new JobRunStore();
	}
	@Bean
	public HazelcastInstance hazelcastInstance()
			throws IOException {
		DistributionService.LOG.info("Starting Hazelcast server ..");
		Resource config = null;
		if(StringUtils.hasText(configXml)) {
			config = context.getResource(configXml);
		}
		Config conf = getConfig(config);
		conf.setProperty("hazelcast.rest.enabled", "true");
		
		if (storeEnabled) {
			MapConfig mapc = conf.getMapConfig(DistributionService.JOB_MASTER);
			MapStoreConfig storeCfg = new MapStoreConfig();
			storeCfg.setEnabled(true);
			storeCfg.setImplementation(aJobMasterStore());
			mapc.setMapStoreConfig(storeCfg);
			
			mapc = conf.getMapConfig(DistributionService.EXEC_STATUS);
			storeCfg = new MapStoreConfig();
			storeCfg.setEnabled(true);
			storeCfg.setImplementation(aJobRunStore());
			mapc.setMapStoreConfig(storeCfg);
		}
		
		return Hazelcast.newHazelcastInstance(conf);
	}

}
