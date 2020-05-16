package jobshop.solvers;
import java.util.Comparator;

import jobshop.encodings.JobNumbers;

public class PopulationComparateur implements Comparator<JobNumbers> {

	@Override
	public int compare(JobNumbers j1, JobNumbers j2) {
			return j1.toSchedule().makespan()-j2.toSchedule().makespan();
	}

}