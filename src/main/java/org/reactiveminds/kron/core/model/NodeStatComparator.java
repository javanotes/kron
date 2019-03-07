package org.reactiveminds.kron.core.model;

import java.util.Comparator;

public class NodeStatComparator implements Comparator<NodeStat> {

	@Override
	public int compare(NodeStat o1, NodeStat o2) {
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
