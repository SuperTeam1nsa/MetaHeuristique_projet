package jobshop.solvers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class GloutonAleatoire  implements Solver  {
	@Override
	public Result solve(Instance instance, long deadline) {
		   ResourceOrder sol = new ResourceOrder(instance);
		   ArrayList<Task> doable=new ArrayList<Task>();
		   //tâches déjà planifiées
		   ArrayList<Task> scheduled=new ArrayList<Task>();
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
		   // element à considérer (aléatoire) parmi ceux faisables
		   Task t_opti=doable.get((int) (Math.random() * doable.size()));
		   
		    //Pour chaque tache d'un job on détermine quelle machine est utilisée
			int m =instance.machine(t_opti.job, t_opti.task);		
			//on lui affecte la tâche
			sol.manualFill(m, t_opti.job, t_opti.task);
			doable.remove(t_opti);
			scheduled.add(t_opti);
	       }//while
	       return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
	    }

	

}
