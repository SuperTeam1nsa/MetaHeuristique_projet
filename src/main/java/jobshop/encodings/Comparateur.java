package jobshop.encodings;

import java.util.Comparator;

import jobshop.Schedule;

public class Comparateur implements Comparator<Task> {
	
	private final Schedule s;
	public Comparateur(Schedule sched) {
		s=sched;	
	}

	@Override
	public int compare(Task t1, Task t2) {
		return s.startTime(t1.job, t1.task)-s.startTime(t2.job, t2.task);
	}

}
