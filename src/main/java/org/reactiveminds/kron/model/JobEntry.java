package org.reactiveminds.kron.model;

import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
@Entity
public class JobEntry implements DataSerializable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobEntry other = (JobEntry) obj;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		return true;
	}
	public JobEntry() {
	}
	public ExecutionRequest getJob() {
		return job;
	}

	public void setJob(ExecutionRequest job) {
		this.job = job;
	}
	private boolean isEnabled = true;
	private ExecutionRequest job;
	private String cronSchedule;
	@Id
	private String jobName;
	private String startFrom;
	public String getCronSchedule() {
		return cronSchedule;
	}
	public void setCronSchedule(String cronSchedule) {
		this.cronSchedule = cronSchedule;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getStartFrom() {
		return startFrom;
	}
	public void setStartFrom(String startFrom) {
		this.startFrom = startFrom;
	}
	
	private String dateFormat;
	//..other properties
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		job.writeData(out);
		out.writeBoolean(isEnabled);
		out.writeUTF(cronSchedule);
		out.writeUTF(jobName);
		out.writeUTF(startFrom);
		out.writeUTF(dateFormat);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		job = new ExecutionRequest();
		job.readData(in);
		setEnabled(in.readBoolean());
		setCronSchedule(in.readUTF());
		setJobName(in.readUTF());
		setStartFrom(in.readUTF());
		setDateFormat(in.readUTF());
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
}
