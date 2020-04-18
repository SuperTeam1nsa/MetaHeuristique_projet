package jobshop.encodings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;
//NB: I code alone all this class, without using the given one (later)including ResourceOrder(Schedule schedule) and 
public class ResourceOrder extends Encoding {
	// for each machine m, jobs[m] is an array of tasks to be
    // executed on this machine in the same order
	// (jobs, taches)=Task par machine #matrice
	public final Task[][] jobs;
	private int[] orderNumberMachine;
	public ResourceOrder(Instance instance) {
		super(instance);//save l'instance
		//Tableau [machine][ordre sur la machine]=task
		jobs = new Task[instance.numMachines][instance.numJobs];
		orderNumberMachine=new int[instance.numMachines];
		Arrays.fill(orderNumberMachine, 0);
	}
//mine
	public ResourceOrder(Schedule sched) {
		super(sched.pb);
		//Tableau [machine][ordre sur la machine]=task
		jobs = new Task[instance.numMachines][instance.numJobs];	
		orderNumberMachine=new int[instance.numMachines];
		Arrays.fill(orderNumberMachine, 0);
		//affecte les tâches à chaque machine correspondante
		this.autoFillNotOptimalOrder();
		//affecte les tâches dans l'ordre (fourni par startTime)
		for(int m=0;m<instance.numMachines;m++) {
		Arrays.sort(jobs[m], new Comparateur(sched));
		}
	}
	  /*[given] Creates a resource order from a schedule. 
    public ResourceOrder(Schedule schedule)
    {
        super(schedule.pb);
        Instance pb = schedule.pb;
        this.jobs = new Task[pb.numMachines][];
        int nextFreeSlot[] = new int[instance.numMachines];
        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;
            // for thi machine, find all tasks that are executed on it and sort them by their start time
            jobs[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine
            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }
    }*/
	/* Creates an exact copy of this resource order. */
    public ResourceOrder copy() {
        return new ResourceOrder(this.toSchedule());
    }
	/*
	 * Rempli la matrice des jobs connaissant:
	 * la tache, le job, l'ordre de réalisation sur la machine et la machine correspondante
	 */
	public void manualFill(int machine,int ordre,int job,int task) {
		jobs[machine][ordre]=new Task(job,task);
	}
	/*
	 * Rempli la matrice des jobs connaissant:
	 * la tache, le job, et la machine correspondante (autodetermine l'ordre de la tache selon l'ordre chrono des appels) 
	 * Do not mix it with manualFill fixing order corrupting data risk ! 
	 */
	public void manualFill(int machine,int job,int task) {
		jobs[machine][orderNumberMachine[machine]]=new Task(job,task);
		orderNumberMachine[machine]++;
	}
	/*
	 * Attribue chaque tâche à une ressource (machine) sans appliquer d'ordre en dehors du respect des contraintes
	 */
public void autoFillNotOptimalOrder() {
	//Cpt est le nombre de tâches déjà affecté à chaque machine (index)
	int[] cpt = new int[instance.numMachines];
	 Arrays.fill(cpt, 0);
	 //On parcours toutes les tâches et les jobs
	for(int t=0;t<instance.numTasks;t++) {
		for(int j=0;j<instance.numJobs;j++) {
			//Pour chaque tache d'un job on détermine quelle machine est utilisée
			int m =instance.machine(j, t);
			//on lui affecte la tâche
			jobs[m][cpt[m]]=new Task(j,t);
			//on augmente de un le nombre de tâches effectuées sur cette machine 
			cpt[m]++;
		}
	}
	/* Ancienne version :
	for(int j=0;j<instance.numJobs;j++)//jobs
	{
		for(int m=0;m<instance.numMachines;m++) //m=machines
		{
		int task=instance.task_with_machine(j, m);
		jobs[m][j]=new Task(j,task);
		}
	}*/
}
	public String toString() {
		String txt="";
		for(int k=0;k<jobs.length;k++) {
			txt+=" Machine "+k+" :";
			for(int l=0;l<jobs[k].length;l++)
				txt+=jobs[k][l];
			txt+="\n";
		}
		return txt;
	}


	@Override
	public Schedule toSchedule() {
		//System.out.print(this.toString());
        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
        
        ArrayList<Task> scheduled=new ArrayList<Task>();
       int total= instance.numJobs*instance.numTasks;
       int nb_scheduled=0;
       while(scheduled.size() < total) {
        	for(int i =0; i< jobs.length;i++) {//i=machine
        		for(int j=0;j<jobs[i].length;j++) {//j=ordre sur la machine
        			Task a=jobs[i][j];//a=(job,task)
        			//realizable
        			if(!scheduled.contains(a)){
        				//tache ini d'un job et 1ère sur la machine
        				if(a.task==0 && j==0) {
        					 startTimes[a.job][a.task]=0;
        					 scheduled.add(a);
        				//tache dont on a realise les prédécesseurs et 1ère sur la machine
	        			}else if( a.task!=0 && j==0 && scheduled.contains(new Task(a.job,a.task-1))) {
	        				startTimes[a.job][a.task]=startTimes[a.job][a.task-1]+instance.duration(a.job, a.task-1);
       					 	scheduled.add(a);
       					//1ère tâche d'un job, mais après une autre tâche sur la machine => va attendre qu'on ait réalisé la tâche precedente 
       					//qui occupe la machine (en théorie pas atteignable sinon #break en fin, mais tjrs + propre)
	        			}else if(j>=1 && a.task==0  && scheduled.contains(new Task(jobs[i][j-1].job,jobs[i][j-1].task))) {
	        				Task z=jobs[i][j-1];
	        				//le temps de start est le temps par rapport à l'ordre des taches par machine: il y a eu attente
	        				startTimes[a.job][a.task]=startTimes[z.job][z.task]+instance.duration(z.job, z.task);
       					 	scheduled.add(a);
       					 //kème tâche d'un job, en ordre nème=> s'éxécute si on a réalisé la tâche précédente dans l'ordre du job ET 
       					 	// la tâche précédente su la machine (en théorie pas atteignable sinon #break en fin, mais tjrs + propre)
	        			}else if((j>=1 && a.task!=0 && scheduled.contains(new Task(a.job,a.task-1)) && scheduled.contains(new Task(jobs[i][j-1].job,jobs[i][j-1].task)))) {
	        				Task z=jobs[i][j-1];
	        				//le temps de start est soit le temps de la tache précédente par rapport à l'ordre des tâches du job, soit par rapport 
	        				// à l'ordre des taches par machine s'il y a eu attente
	        				startTimes[a.job][a.task]=Math.max(startTimes[a.job][a.task-1]+instance.duration(a.job, a.task-1),
	        						startTimes[z.job][z.task]+instance.duration(z.job, z.task));
       					 	scheduled.add(a);
	        			}
        				//s'assure de respecter l'ordre sur les machines,
        				//si on ne peut executer cette tache on ne pourra pas executer les suivantes sur la même machine
	        			else {
	        				//System.out.print("\n else");
	        				break; 
	        			}
        			}
        		
        			
        		}
        	}
       	   if(nb_scheduled==scheduled.size()) {
       		   System.err.println(" \n /!\\ Warning: impossible to schedule the task ! Check the task order ! Returning null schedule \n");
       		   return null;
       	   }
        	   nb_scheduled=scheduled.size();
        	
        }

    return new Schedule(instance, startTimes);
	}


}
