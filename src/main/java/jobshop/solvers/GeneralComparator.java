package jobshop.solvers;

import java.util.Comparator;

import jobshop.Instance;
import jobshop.encodings.Task;

//try to see if PriorityQueu everywhere is better than max algo: answer : no
public class GeneralComparator implements Comparator<Task> {
	
	private short methode=0;
	private final Instance instance;
	public GeneralComparator(String methode, Instance instance) {
		//meiux vaut comparer des int que des String à chaque comparaison #perfs (better 1 classe par comparateur mais gain faible #if comparaison)
		if(methode.equals("SPT"))
			this.methode=0;
		else if(methode.equals("LPT"))
			this.methode=1;
		else if(methode.equals("SRPT"))
			this.methode=2;
		else if(methode.equals("LRPT"))
			this.methode=3;
		else
			System.err.print(" Unkown method of comparaison ! (will use SPT )");
		this.instance=instance;
	}

	@Override
	public int compare(Task t1, Task t2) {
		if(methode==0) {
			return instance.duration(t1.job, t1.task)-instance.duration(t2.job, t2.task);
		}else if(methode==1) {
			return instance.duration(t2.job, t2.task)-instance.duration(t1.job, t1.task);
		}else if(methode==2) {
			return remaning_time(t1.job,t1.task)-remaning_time(t2.job,t2.task);
		}else
			return remaning_time(t2.job,t2.task)-remaning_time(t1.job,t1.task);
	}
	private int remaning_time(int job, int task) {
		int time=0;
		for(int i=task;i<instance.numTasks;i++) {
			time+=instance.duration(job, i);
		}
		return time;
	}

}