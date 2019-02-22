package org.reactiveminds.kron.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.reactiveminds.kron.model.ExecutionEntry;
import org.reactiveminds.kron.model.ExecutionRequest;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.util.StringUtils;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
/**
 * A forward only generator of cron dates.
 * @author Sutanu_Dalui
 * @deprecated Will be removed
 */
public class CronSequence implements DataSerializable{

	static final String DELIM = "_";
	static final String DEFAULT_DATE_FORMAT = "yyyy"+DELIM+"MM"+DELIM+"dd"+DELIM+"HH"+DELIM+"mm"+DELIM+"ss";
	public CronSequence(String expression) {
		super();
		if(!CronSequenceGenerator.isValidExpression(expression))
			throw new IllegalArgumentException("Invalid cron expression '" + expression + "'");
		this.expression = expression;
		cronGen = new CronSequenceGenerator(this.expression);
	}
	public CronSequence() {
	}
	/**
	 * Generates a String key for the given {@linkplain ExecutionEntry}.
	 * @param entry
	 * @return
	 */
	public String extractDateKey(ExecutionEntry entry) {
		String date = dateFmt.format(entry.getScheduledTime());
		return splitDate(date);
	}
	private static String splitDate(String date) {
		String [] dArr = Arrays.copyOfRange(date.split(DELIM, 4), 0, 3);
		return StringUtils.arrayToDelimitedString(dArr, DELIM);
	}
	private transient DateFormat dateFmt = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	private transient CronSequenceGenerator cronGen;
	private Date lastRun;
	private String expression;
	
	public String getExpression() {
		return expression;
	}
	/**
	 * Generate the next set of jobRequests.
	 * @param job
	 * @param steps
	 * @return
	 */
	private List<ExecutionEntry> generateNextRequest0(ExecutionRequest job, int steps) {
		List<Date> dates = generateNext(null, steps);
		return dates.stream().map(d -> new ExecutionEntry(d, job)).collect(Collectors.toList());
	}
	/**
	 * Generate the next set of jobRequests, grouped by the date string as keys
	 * @param job
	 * @param steps
	 * @return
	 */
	public Map<String, List<ExecutionEntry>> generateNextRequest(ExecutionRequest job, int steps) {
		List<ExecutionEntry> next = generateNextRequest0(job, steps);
		return next.stream().collect(Collectors.groupingBy(e -> dateFmt.format(e.getScheduledTime())));
	}
	/**
	 * 
	 * @param epoch
	 * @param steps
	 * @return
	 */
	private List<Date> generateNext(Date epoch, int steps) {
		List<Date> dates = new LinkedList<>();
		int n = steps;
		if(lastRun == null) {
			lastRun = cronGen.next(epoch != null ? epoch : new Date());
			dates.add(lastRun);
			n--;
		}
		for (int i = 0; i < n; i++) {
			lastRun = cronGen.next(lastRun);
			dates.add(lastRun);
		}
		
		return dates;
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(lastRun != null ? lastRun.getTime() : -1);
		out.writeUTF(expression);
	}
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		long t = in.readLong();
		if(t != -1)
			lastRun = new Date(t);
		expression = in.readUTF();
		cronGen = new CronSequenceGenerator(expression);
	}
}
