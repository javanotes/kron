package org.reactiveminds.kron.spi;

import org.reactiveminds.kron.core.TaskProxy;

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

}
