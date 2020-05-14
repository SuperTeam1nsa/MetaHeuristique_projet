package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1 (=> on veut la 1ère et la deuxième tache de la machine 1 pas de lien avec les jobs) 
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    public static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
        public String toString() {
        	return " machine:"+machine+" first: "+firstTask+" last: "+lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swap with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        // machine on which to perform the swap
        public final int machine;
        // index of one task to be swapped
        public final int t1;
        // index of the other task to be swapped
        public final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder ro) {
           Task task_aux=ro.jobs[machine][t1];
           ro.jobs[machine][t1]=ro.jobs[machine][t2];
           ro.jobs[machine][t2]=task_aux;
        }
    }
//Elle se termine avec un optimum local (le voisinage ne permet pas de trouver de meilleure solution) ou lorsqu’un temps limite de calcul est atteint.
//anonymous fonction doesn't allow modification of local variable inside
    private ResourceOrder current;
    private ResourceOrder best;
    private  int bestmakespan;
    @Override
    public Result solve(Instance instance, long deadline) {
    	//GloutonTime solv=new GloutonTime("LRPT");
    	//current=new ResourceOrder(solv.solve(instance, deadline).schedule);
    	//ordre simple => 2 fois + rapide que de faire tourner un solver avant, meilleure solution en moyenne
    	//current=new ResourceOrder(new BasicSolver().solve(instance, deadline).schedule);
    	//ou
    	current=new ResourceOrder(new GloutonTime("LRPT").solve(instance, deadline).schedule);//ou Glouton
    	//ou
    	//current=new ResourceOrder(instance);current.autoFillNotOptimalOrder();
    	best=current.copy();
    	int old_bestmakespan;
		bestmakespan=best.toSchedule().makespan();
    	do{
    		 old_bestmakespan= bestmakespan;
    		 //on extrait chaque block du chemin critique et pour chacun on teste tous les swap possibles, si le swap améliore sa solution devient la meilleure
    		 blocksOfCriticalPath(best)
    		 		.forEach(block ->{neighbors(block)
    		 		.forEach(swap -> {
    		 				current=best.copy();
    		 				swap.applyOn(current);
    		 				int currentmakespan =current.toSchedule().makespan();
    		 				if(currentmakespan<bestmakespan) {
    		 					bestmakespan=currentmakespan;
    		 					best=current;
    		 				}
    		 			});
    		 		});
    		 
    	 } while(deadline - System.currentTimeMillis() > 1 && old_bestmakespan > bestmakespan);
    	//Timeout ou on n'améliore plus le résultat
    	if(old_bestmakespan == bestmakespan)
    		return new Result(instance, best.toSchedule(),Result.ExitCause.Blocked);
    	else
    		return new Result(instance, best.toSchedule(),Result.ExitCause.Timeout);
    }

    /** Returns a list of all blocks of the critical path. */
   // Block =tâches qui utilisent toutes la même machine et sont consécutives le long du chemin critique
    public List<Block> blocksOfCriticalPath(ResourceOrder ro) {
    	List<Block> blocks=new ArrayList<Block>();
    	Schedule sched= ro.toSchedule();
    	List<Task> criticalTask=sched.criticalPath();
    	//numero des machines utilisés actuellement et précédemment
    	int machine_actuelle=-1;
    	int machine_ancienne=-1;
    	//nombre de taches réalisées à la suite sur la machine utilisée précédemment
    	int cpt=1;
    	Task first=null,last=null;
    	for(Task t:criticalTask) {
   			machine_actuelle=ro.instance.machine(t.job,t.task);
    		//si changement de machine
    		if(ro.instance.machine(t.job,t.task) != machine_ancienne) {
    			//on save le block s'il contient +de 2 taches
    			if(cpt >=2) {
    				int f=-1,l=-1;
    				//System.out.print("\n Taches sur la machine :"+Arrays.toString(ro.jobs[machine_ancienne]));
    	    		int i=0;
    	    		for(Task _:ro.jobs[machine_ancienne]) {
    					if(ro.jobs[machine_ancienne][i].equals(first))
    						f=i;
    					else if(ro.jobs[machine_ancienne][i].equals(last)) {
    						l=i;
    						//last always after first
    						break;
    					}
    					i++;
    				}
    				if(f==-1 || l==-1) {
    					System.err.print(" EPIC FAIL searching for : "+first+" and "+last+" in "+Arrays.toString(ro.jobs[machine_ancienne]));
    				}
    				 blocks.add(new Block(machine_ancienne,f,l)); 
    			}
   			    first=t.copy();
    			cpt=1;
    		}
    		//on reste sur la même machine => augmentation du compteur et sauvegarde de la tache courante
    		else {
    			last=t.copy();
    			cpt++;
    			
    		}
    		machine_ancienne=machine_actuelle;
    	}
    	if(cpt >= 2) {
    		int f=-1,l=-1;
    		int i=0;
    		for(Task t: ro.jobs[machine_ancienne]) {
				if(ro.jobs[machine_ancienne][i].equals(first))
					f=i;
				if(ro.jobs[machine_ancienne][i].equals(last)) {
					l=i;
					//last always after first
					break;
				}
				i++;
			}
			if(f==-1 || l==-1) {
				System.err.print(" EPIC FAIL searching for : "+first+" and "+last+" in "+Arrays.toString(ro.jobs[machine_ancienne]));
			}
			
			 blocks.add(new Block(machine_ancienne,f,l)); 
    	}
    	return blocks;
        //throw new UnsupportedOperationException();
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood 
     * Le voisinage à implémenter consiste à permuter les deux tâches en début de bloc et les deux
tâches en fin de bloc.
lorsqu’un bloc comporte deux tâches, il ne permet de générer qu’une
seule solution voisine. Tout bloc ayant au moins trois tâches permet de générer EXACTEMENT deux solutions
voisines.
internal operation if, and only if, it is neither the first nor the last operation of the block.
Reversing the order of two operations on a critical path in a candidate schedule s never results in an infeasible candidate solution. 
Reversing the order of two operations that are not on a critical path cannot lead to a reduction of the makespan.
Reversing the order of two internal operations in a block of a critical path cannot lead to a reduction of the makespan.
Reversing the order of the first two operations on the first machine block or the last two operations on the last block cannot reduce the makespan.(Non demandé ici)
https://www.sciencedirect.com/topics/computer-science/neighbourhood-graph
(i)swap two consecutive jobs at position i and i+1 (swap move), (ii) exchange jobs at positions i and j (exchange move), 
(iii) remove job at position i and insert it at position j (insertion move)
https://hal.archives-ouvertes.fr/hal-00678053/document
 * @param block 
     * 
     * */
    public static List<Swap> neighbors(Block block) {
    	int nbTasks =block.lastTask-block.firstTask+1;
    	ArrayList<Swap> permu=new ArrayList<Swap>();
    	//NB: nbTask >=2 car block size min=2
    	if(nbTasks==2)
    		permu.add(new Swap(block.machine,block.firstTask,block.lastTask));
    	else if(nbTasks>2) {
    		permu.add(new Swap(block.machine,block.firstTask,block.firstTask+1));
    		permu.add(new Swap(block.machine,block.lastTask-1,block.lastTask));
    	}else {
    		System.err.print("Warning ! Receive invalid block in neighbors ! machine:"+block.machine+" first:"+block.firstTask+" last:"+block.lastTask );
    	}
    	return permu;
    }

}
