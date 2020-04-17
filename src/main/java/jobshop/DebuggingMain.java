package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.Glouton;
import jobshop.solvers.GloutonTime;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
            //ResourceOrder ro=new ResourceOrder(instance);
            //ro.autoFillNotOptimalOrder();
           /* ro.manualFill(0, 0, 0, 0);
            ro.manualFill(0, 1, 1, 1);
            ro.manualFill(1, 0, 1, 0);
            ro.manualFill(1, 1, 0, 1);
            ro.manualFill(2, 0, 0, 2);
            ro.manualFill(2, 1, 1, 2);*/

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

            //Schedule sched = enc.toSchedule();
            
            GloutonTime glou=new GloutonTime("SPT");
            Schedule sched= glou.solve(instance, 3000).schedule;
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
