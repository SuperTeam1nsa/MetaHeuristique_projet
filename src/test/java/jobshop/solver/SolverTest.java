package jobshop.solver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import jobshop.Instance;
import jobshop.Schedule;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.DescentSolver.Block;

public class SolverTest {
	
	@Test 
	public void testPermutationRo() throws IOException{
		 Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
		 ResourceOrder ro=new ResourceOrder(instance);
         ro.autoFillNotOptimalOrder();
         System.out.println("RO: \n"+ro);
         DescentSolver solv=new DescentSolver();
         //critical path visualisation
         Schedule sched= ro.toSchedule();
     	List<Task> criticalTask=sched.criticalPath();
     	System.out.print("\n Critical path : "+ criticalTask);
     	
         List<Block> blocks =solv.blocksOfCriticalPath(ro);
        System.out.print("\n Blocks : \n"+blocks);
         blocks.forEach(b -> DescentSolver.neighbors(b)
         	  .forEach(s ->{ System.out.print("\n Permutation sur la machine:"+s.machine+" des taches : "+new Task(s.t1,s.t2) );
         	  Task t2 =ro.jobs[s.machine][s.t2];
         	  Task t1 =ro.jobs[s.machine][s.t1];
         	  s.applyOn(ro);
         	  assert ro.jobs[s.machine][s.t1]==t2;
         	  assert ro.jobs[s.machine][s.t2]==t1;
         	  }));
         System.out.println("\n new RO: \n"+ro);
		
	}
	@Test
	public void DescentSolver() throws IOException {
		   Instance instance = Instance.fromFile(Paths.get("instances/la40"));
		   ResourceOrder ro=new ResourceOrder(instance);
           ro.autoFillNotOptimalOrder();
           Schedule sched =ro.toSchedule();
           int notOptiMakespan=sched.makespan();
           
           DescentSolver solv=new DescentSolver();
           Schedule schedSolv =solv.solve(instance, 3000).schedule;
           System.out.println("SCHEDULE: " + schedSolv.toString());
           System.out.println("VALID: " + schedSolv.isValid());
           System.out.println("MAKESPAN: " + schedSolv.makespan());
           assert schedSolv.isValid()==true;
           assert schedSolv.makespan()<notOptiMakespan;
	}

}
