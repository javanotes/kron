package org.reactiveminds.kron.core;
@FunctionalInterface
public interface LeaderElectNotifier {

	void onElect(String message);
}
