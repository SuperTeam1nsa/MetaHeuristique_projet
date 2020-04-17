package jobshop.solvers;

import java.util.ArrayList;
import java.util.Arrays;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class Glouton  implements Solver  {
	private String methode;
	public Glouton(String methode) {
		this.methode=methode;
	}
	@Override
	public Result solve(Instance instance, long deadline) {
		   ResourceOrder sol = new ResourceOrder(instance);
		   //tâches déjà planifiées
		   ArrayList<Task> scheduled=new ArrayList<Task>();
		   ArrayList<Task> doable=new ArrayList<Task>();
		   int total= instance.numJobs*instance.numTasks;
	       while(scheduled.size() < total) {
			 //On parcours toutes les tâches et les jobs
			for(int t=0;t<instance.numTasks;t++) {
				for(int j=0;j<instance.numJobs;j++) {
					Task task=new Task(j,t);
					//si on n'a pas déjà traité ni ajouté la tâche, et que c'est la première 
					//ou qu'on a fais la/les tâches précédentes (par recursivité sur la contrainte checker la précédente revient à checker toutes les précédentes)
					//alors on ajoute la tâche courante dans les tâches réalisables
					if(!scheduled.contains(task) && !doable.contains(task) && (t==0 || scheduled.contains(new Task(j,t-1)))) {
							doable.add(task);
				    }
			}
	       }
		   if(doable.size()==0) {
			   System.err.print(" Pas de tâches réalisables ... => broken instance");
			   return null;
		   }
		   Task t_opti=null;
		   
		   if(methode.equals("SPT")){
			   long shortest= (long)Integer.MAX_VALUE+1;
			   for(Task t:doable) {
				   int temps=instance.duration(t.job, t.task);
				   if(temps<shortest){
					   shortest=temps;
					   t_opti=t;
				   }
			   }
	       }else if(methode.equals("LPT")){
			   long longhest= (long)Integer.MIN_VALUE-1;
			   for(Task t:doable) {
				   int temps=instance.duration(t.job, t.task);
				   if(temps>longhest){
					   longhest=temps;
					   t_opti=t;
				   }
			   }
	       }else if(methode.equals("SRPT")){ 
	    	   long shortest= (long)Integer.MAX_VALUE+1;
			   for(Task t:doable) {
				   int temps=remaning_time(t.job,t.task,instance);
				   if(temps<shortest){
					   shortest=temps;
					   t_opti=t;
				   }
			   }
	       }else if(methode.equals("LRPT")){
			   long longhest= (long)Integer.MIN_VALUE-1;
			   for(Task t:doable) {
				   int temps=remaning_time(t.job,t.task,instance);
				   if(temps>longhest){
					   longhest=temps;
					   t_opti=t;
				   }
			   }
	       }
		    //Pour chaque tache d'un job on détermine quelle machine est utilisée
			int m =instance.machine(t_opti.job, t_opti.task);					
			//on lui affecte la tâche
			sol.manualFill(m, t_opti.job, t_opti.task);
			doable.remove(t_opti);
			scheduled.add(t_opti);
	       }//while
	       return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
	    }
	private int remaning_time(int job, int task, Instance instance) {
		int time=0;
		for(int i=task;i<instance.numTasks;i++) {
			time+=instance.duration(job, i);
		}
		return time;
	}
	

}
