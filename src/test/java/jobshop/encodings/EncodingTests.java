package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {
	
	@Test
	public void convertion() throws IOException {

		//NOTE: ENCODING: [0, 1, 1, 0, 0, 1] produit le même schedule que ENCODING: [0, 1, 0, 1, 0, 1]
        //donc equivalent en définitive, la transformation ne peut pas être bijective
		//=> on va tester que le schedule produit et obtenu après un changement de représentation est le même
		//#consistant, et non pas que la représentation dans la base est la même (si elle génére le même schedule que celui qu'elle
		//a reçu en entrée, cela signifie qu'elle est identique, ou équivalente en définitive
		
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
		
        //Test de conversion de JN vers RN et re vers JN
				 JobNumbers enc = new JobNumbers(instance);
		         enc.jobs[enc.nextToSet++] = 0;
		         enc.jobs[enc.nextToSet++] = 1;
		         enc.jobs[enc.nextToSet++] = 1;
		         enc.jobs[enc.nextToSet++] = 0;
		         enc.jobs[enc.nextToSet++] = 0;
		         enc.jobs[enc.nextToSet++] = 1;
		
		         System.out.println("\nENCODING: " + enc);
		         Schedule sched = enc.toSchedule();
		         
		         assert sched.isValid();
		         assert sched.makespan() == 12;
		        
		        //test de convertion à partir de Schedule de JN
		        ResourceOrder ro2=new ResourceOrder(sched);
		        System.out.print(ro2);
		        Schedule t1=ro2.toSchedule();
		        System.out.println(t1);
		        assert t1.isValid();
		        assert t1.makespan() == 12;
		        
		        //test de convertion à partir de Schedule de JN 
		        JobNumbers jn=new JobNumbers(sched);
		        System.out.print(jn);
		        Schedule t2=jn.toSchedule();
		        System.out.println(t2);
		        assert t1.isValid();
		        assert t1.makespan() == 12;
		        
		        //Les deux conversion sont égales et valent le schedule d'orgine
		        assert t1.toString().equals(t2.toString()); 
		        assert t1.toString().equals(sched.toString());
        
        //test de conversion de RO vers JN
        // Ro -> Schedule -> JN -> Schedule -> Ro
		        ResourceOrder ro=new ResourceOrder(instance);
		        ro.manualFill(0, 0, 0, 0);
		        ro.manualFill(0, 1, 1, 1);
		        ro.manualFill(1, 0, 1, 0);
		        ro.manualFill(1, 1, 0, 1);
		        ro.manualFill(2, 0, 0, 2);
		        ro.manualFill(2, 1, 1, 2);
		        System.out.print(ro);
		        Schedule s2 =ro.toSchedule();
		        System.out.println(s2);
		        
		        JobNumbers jn2=new JobNumbers(s2);
		        System.out.print(jn2);
		        Schedule s3=jn.toSchedule();
		        System.out.print(s3);
		        
		        //même schedule entre 2 convertions
		        assert s2.toString().equals(s3.toString());
		        
		        ResourceOrder ro3=new ResourceOrder(s3);
		        System.out.print(ro3);
		        Schedule s4=ro3.toSchedule();
		        System.out.print(s4);
		        //même schedule entre 2 convertions
		        assert s2.toString().equals(s4.toString());
		        
		       // L'ensemble du processus nous a fait revenir à l'état initial
		        assert ro.toString().equals(ro3.toString());
	}
	
	 @Test
	    public void testRessourceOrder() throws IOException {
	        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

	        //  numéro de jobs : [0 1 1 0 0 1](cf exercices)
	       /* Machine 0 :(0, 0}(1, 1}
	        Machine 1 :(1, 0}(0, 1}
	        Machine 2 :(0, 2}(1, 2}*/
	        ResourceOrder ro=new ResourceOrder(instance);
            //ro.autoFillNotOptimalOrder();
            ro.manualFill(0, 0, 0, 0);
            ro.manualFill(0, 1, 1, 1);
            ro.manualFill(1, 0, 1, 0);
            ro.manualFill(1, 1, 0, 1);
            ro.manualFill(2, 0, 0, 2);
            ro.manualFill(2, 1, 1, 2);
	        System.out.println(ro);
            

	        Schedule sched = ro.toSchedule();
	        System.out.println(sched);
	        /*
	            (0,0):0 | (0,1):3 | (0,2):6 |	
 				(1,0):0 | (1,1):3 | (1,2):8 |
	         */
	        assert sched.isValid();
	        assert sched.makespan() == 12;

	        /* Machine 0 :(1, 1}(0, 0}
	        Machine 1 :(1, 0}(0, 1}
	        Machine 2 :(0, 2}(1, 2}*/
	        ro=new ResourceOrder(instance);
            ro.manualFill(0, 0, 1, 1);
	        ro.manualFill(0, 1, 0, 0);
            ro.manualFill(1, 0, 1, 0);
            ro.manualFill(1, 1, 0, 1);
            ro.manualFill(2, 0, 0, 2);
            ro.manualFill(2, 1, 1, 2);
	        System.out.println(ro);

	        sched = ro.toSchedule();
	        System.out.println(sched);
	        /*
	         *  (0,0):4 | (0,1):7 | (0,2):10 |	
 				(1,0):0 | (1,1):2 | (1,2):12 |
	         */
	        assert sched.isValid();
	        assert sched.makespan() == 16;
	        
	        //impossible schedule :
	        /* Machine 0 :(1, 1}(0, 0}
	        Machine 1 :(0, 1}(1, 0}
	        Machine 2 :(0, 2}(1, 2}*/
	        ro=new ResourceOrder(instance);
            ro.manualFill(0, 0, 1, 1);
	        ro.manualFill(0, 1, 0, 0);
            ro.manualFill(1, 0, 0, 1);
            ro.manualFill(1, 1, 1, 0);
            ro.manualFill(2, 0, 0, 2);
            ro.manualFill(2, 1, 1, 2);
	        System.out.println(ro);
	        
	        sched = ro.toSchedule();
	        assert sched==null;
	    }

    @Test
    public void testJobNumbers() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 14;
    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }

}
