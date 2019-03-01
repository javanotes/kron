package org.reactiveminds.kron.core.grid;

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
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@ConditionalOnProperty(name = "kron.master")
@Configuration
class HazelcastServerConfiguration {
	
	@Value("${spring.hazelcast.config:}")
	private String configXml;
	@Value("${kron.store.enabled:true}")
	private boolean storeEnabled;
	@Value("${spring.hazelcast.multicastPort:50071}")
	private int multicastPort;
	@Value("${spring.hazelcast.thisPort:0}")
	private int thisPort;
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
	/**
	 * @deprecated
	 * @return
	 */
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
		DistributionService.LOG.info("Starting Hazelcast data service ..");
		Resource config = null;
		boolean hasConfigXml = false;
		if(StringUtils.hasText(configXml)) {
			config = context.getResource(configXml);
			hasConfigXml = true;
		}
		final Config conf = getConfig(config);
		conf.setProperty("hazelcast.rest.enabled", "true");
		
		if (!hasConfigXml) {
			NetworkConfig network = conf.getNetworkConfig();
			if (thisPort > 0) {
				network.setPort(thisPort);
			}
			if (multicastPort > 0) {
				JoinConfig join = network.getJoin();
				join.getTcpIpConfig().setEnabled(false);
				join.getAwsConfig().setEnabled(false);
				join.getMulticastConfig().setEnabled(true);
				join.getMulticastConfig().setMulticastPort(multicastPort);
			}
		}
		if (storeEnabled) {
			MapConfig mapc = conf.getMapConfig(DistributionService.JOB_MASTER);
			MapStoreConfig storeCfg = new MapStoreConfig();
			/*storeCfg.setEnabled(true);
			storeCfg.setImplementation(aJobMasterStore());
			mapc.setMapStoreConfig(storeCfg);*/
			
			mapc = conf.getMapConfig(DistributionService.JOB_RUN);
			storeCfg = new MapStoreConfig();
			storeCfg.setEnabled(true);
			storeCfg.setImplementation(aJobRunStore());
			mapc.setMapStoreConfig(storeCfg);
		}
		
		return Hazelcast.newHazelcastInstance(conf);
	}

}
