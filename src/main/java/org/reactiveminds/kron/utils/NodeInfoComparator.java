package org.reactiveminds.kron.utils;

import java.util.Comparator;

import org.reactiveminds.kron.model.NodeInfo;

public class NodeInfoComparator implements Comparator<NodeInfo> {

	@Override
	public int compare(NodeInfo o1, NodeInfo o2) {
		int c = Integer.compare(o1.getJobsRunning(), o2.getJobsRunning());
		if(c != 0)
			return c;
		c = Double.compare(o2.getSysInfo().getProcessCpuLoad(), o1.getSysInfo().getProcessCpuLoad());
		if(c != 0)
			return c;
		c = Double.compare(o2.getSysInfo().getPctFreeMemory(), o1.getSysInfo().getPctFreeMemory());
		if(c != 0)
			return c;
		return 0;
	}

}
