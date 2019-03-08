package org.reactiveminds.kron.spi;

import org.reactiveminds.kron.core.TaskProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Basic interface to extend for creating embedded Java tasks. In effect, the Kron slave node
 * JVM will host the job execution. The implementation class has to be registered as a 
 * prototype Spring bean, with necessary configurations. The {@linkplain TaskProxy} will
 * simply {@link #run()} the bean instance retrieved.
 * 
 * @author Sutanu_Dalui
 *
 */
public interface EmbeddedTask extends Runnable {
	
	@Component("dummyTask")
	public static class DummyEmbeddedTask implements EmbeddedTask{
		private static final Logger log = LoggerFactory.getLogger("DummyEmbeddedTask");
		@Override
		public void run() {
			log.info("I am a java task. Will start working heavily ..");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("Phew! just completed in time");
		}
		
	}

}
