package org.reactiveminds.kron.core.grid;

import java.io.IOException;

import org.reactiveminds.kron.core.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@ConditionalOnProperty(name = "kron.worker")
class HazelcastClientConfiguration {

	@Value("${spring.hazelcast.config:}")
	String configXml;
	@Autowired
	ApplicationContext context;
	
	@Bean
	public HazelcastInstance hazelcastInstance()
			throws IOException {
		DistributionService.LOG.info("Starting Hazelcast client service ..");
		Resource config = null;
		if(StringUtils.hasText(configXml)) {
			config = context.getResource(configXml);
		}
		ClientConfig c = config != null ? new XmlClientConfigBuilder(config.getURL()).build() : new XmlClientConfigBuilder().build();
		c.getNetworkConfig().setSmartRouting(false);
		c.getNetworkConfig().setConnectionAttemptLimit(15);
		c.getNetworkConfig().setConnectionAttemptPeriod(5000);
		return getHazelcastInstance(c);
	}
	
	private static HazelcastInstance getHazelcastInstance(ClientConfig clientConfig) {
		if (StringUtils.hasText(clientConfig.getInstanceName())) {
			return HazelcastClient
					.getHazelcastClientByName(clientConfig.getInstanceName());
		}
		return HazelcastClient.newHazelcastClient(clientConfig);
	}
}
