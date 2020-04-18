package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.DescentSolver.Block;
import jobshop.solvers.DescentSolver.Swap;
import jobshop.solvers.Glouton;
import jobshop.solvers.GloutonTime;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/la40"));
            //ResourceOrder ro=new ResourceOrder(instance);
            //ro.autoFillNotOptimalOrder();
           /* ro.manualFill(0, 0, 0, 0);
            ro.manualFill(0, 1, 1, 1);
            ro.manualFill(1, 0, 1, 0);
            ro.manualFill(1, 1, 0, 1);
            ro.manualFill(2, 0, 0, 2);
            ro.manualFill(2, 1, 1, 2);

            //System.out.print(ro.toSchedule().toString());
            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            System.out.println("\nENCODING: " + enc);

            Schedule sched = enc.toSchedule();*/
            ResourceOrder ro=new ResourceOrder(instance);
            ro.autoFillNotOptimalOrder();
            System.out.println("RO: \n"+ro);
            //GloutonTime glou=new GloutonTime("SPT");
            //Schedule sched= glou.solve(instance, 3000).schedule;
          // DescentSolver solv=new DescentSolver();
           //Schedule sched =solv.solve(instance, 3000).schedule;
           Schedule sched =ro.toSchedule();
           /* List<Block> blocks =solv.blocksOfCriticalPath(ro);
           System.out.print("\n Blocks : \n"+blocks);
            blocks.forEach(b -> DescentSolver.neighbors(b)
            	  .forEach(s ->{ System.out.print("\n Permutation sur la machine:"+s.machine+" des taches : "+new Task(s.t1,s.t2) );s.applyOn(ro);}));
            
            System.out.println("\n new RO: \n"+ro);*/
            
            System.out.println("SCHEDULE: " + sched.toString());
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
