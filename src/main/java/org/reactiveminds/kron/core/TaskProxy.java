package org.reactiveminds.kron.core;

public interface TaskProxy {
	/**
	 * 
	 * @param workDir
	 */
	void setDefaultWorkDir(String workDir);
	/**
	 * 
	 * @return
	 */
	boolean isTaskActive();

}