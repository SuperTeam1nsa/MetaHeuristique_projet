package jobshop.solvers;

import java.util.Comparator;

import jobshop.encodings.Task;

public class ComparateurGloutonTime implements Comparator<Task> {
	
	private final GloutonTime glou;
	public ComparateurGloutonTime(GloutonTime glouglou) {
		glou=glouglou;
	}

	@Override
	public int compare(Task t1, Task t2) {
		return glou.startTimes[t1.job][ t1.task]-glou.startTimes[t2.job][ t2.task];
	}

}