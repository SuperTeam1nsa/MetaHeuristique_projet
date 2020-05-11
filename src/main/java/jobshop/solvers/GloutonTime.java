package jobshop.solvers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class GloutonTime  implements Solver  {
	public  int[][] startTimes ;
	private String methode;
	public GloutonTime(String methode) {
		this.methode=methode;
	}
	@Override
	public Result solve(Instance instance, long deadline) {
		   ResourceOrder sol = new ResourceOrder(instance);
		   ArrayList<Task> candidates =new ArrayList<Task>();
		   //tas #array auto ordonné
		   //https://javachannel.org/posts/priorityqueue-and-mutable-item-behavior/
		   //=> vider et reremplir le tas à chaque fois #sinon mal classé
		   PriorityQueue<Task> doable=new PriorityQueue<Task>(instance.numMachines,new ComparateurGloutonTime(this));
		   //tâches déjà planifiées
		   ArrayList<Task> scheduled=new ArrayList<Task>();
		// for each task, its start time
	        startTimes = new int[instance.numJobs][instance.numTasks];
	        int[] machine_early=new int[instance.numMachines];
	        Arrays.fill(machine_early, 0);
		   int total= instance.numJobs*instance.numTasks;
	       while(scheduled.size() < total) {
			 //On parcours toutes les tâches et les jobs
			for(int t=0;t<instance.numTasks;t++) {
				for(int j=0;j<instance.numJobs;j++) {
					Task task=new Task(j,t);
					//si on n'a pas déjà traité ni ajouté la tâche, et que c'est la première 
					//ou qu'on a fais la/les tâches précédentes (par recursivité sur la contrainte checker la précédente revient à checker toutes les précédentes)
					//alors on ajoute la tâche courante dans les tâches réalisables
					if(!scheduled.contains(task)  && (t==0 || scheduled.contains(new Task(j,t-1)))) {
						//Pour chaque tache d'un job on détermine quelle machine est utilisée
						int m =instance.machine(task.job, task.task);	
						if(t==0) {
							startTimes[task.job][task.task]=machine_early[m];
						}else {
							startTimes[task.job][task.task]=Math.max(machine_early[m],startTimes[task.job][task.task-1]+instance.duration(task.job, task.task-1));
						}
							doable.add(task);
				    }
			}
	       }
		   if(doable.size()==0) {
			   System.err.print(" Pas de tâches réalisables ... => broken instance");
			   return null;
		   }
		   //best element
		   Task t_opti=null;
		   //PriorityQueue<Task> candidates=new PriorityQueue<Task>(instance.numJobs,new GeneralComparator( methode,instance));
		   //est-ce que le tas reste ordonné quand la valeur des éléments change ?
		   Task origin=doable.poll();
		   candidates.add(origin);
		   int best=startTimes[origin.job][origin.task];
		   //cas d'égalité entre les jobs dans les dates de début
		   while(doable.peek()!=null && startTimes[doable.peek().job][doable.peek().task]==best) {
				   candidates.add(doable.poll());
		   }
		   //rq: utiliser un tas pour SPT etc. n'améliore pas les perfs (cf GeneralCOmparator)
		   if(methode.equals("SPT")){
			   long shortest= (long)Integer.MAX_VALUE+1;
			   for(Task t:candidates) {
				   int temps=instance.duration(t.job, t.task);
				   if(temps<shortest){
					   shortest=temps;
					   t_opti=t;
				   }
			   }
	       }else if(methode.equals("LPT")){
			   long longhest= (long)Integer.MIN_VALUE-1;
			   for(Task t:candidates) {
				   int temps=instance.duration(t.job, t.task);
				   if(temps>longhest){
					   longhest=temps;
					   t_opti=t;
				   }
			   }
	       }else if(methode.equals("SRPT")){ 
	    	   long shortest= (long)Integer.MAX_VALUE+1;
			   for(Task t:candidates) {
				   int temps=remaning_time(t.job,t.task,instance);
				   if(temps<shortest){
					   shortest=temps;
					   t_opti=t;
				   }
			   }
	       }else if(methode.equals("LRPT")){
			   long longhest= (long)Integer.MIN_VALUE-1;
			   for(Task t:candidates) {
				   int temps=remaning_time(t.job,t.task,instance);
				   if(temps>longhest){
					   longhest=temps;
					   t_opti=t;
				   }
			   }
	       }
		   candidates.clear();
		   //on remet les éléments candidats non selectionne dans le tas (poll les a enleve)
		  /* for(Task t:candidates) {
			   doable.add(t);
		   }*/
		   doable.clear();
		    //Pour chaque tache d'un job on détermine quelle machine est utilisée
			int m =instance.machine(t_opti.job, t_opti.task);		
			machine_early[m]+=instance.duration(t_opti.job, t_opti.task);
			//on lui affecte la tâche
			sol.manualFill(m, t_opti.job, t_opti.task);
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
