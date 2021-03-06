package jobshop;


import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import jobshop.solvers.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
//la01 la02 la03 la04 la05 la06 la07 la08 la09 la10 la11 la12 la13 la14 la15 la16 la17 la18 la19 la20 la21 la22 la23 la24 la25 la26 la27 la28 la29 la30 la31 la32 la33 la34 la35 la36 la37 la38 la39 la40
/*
 * --solver basic random random_glouton SPT LPT SRPT LRPT EST_SPT EST_LPT EST_SRPT EST_LRPT descentSolver TabouSolver -t 2 --instance ft06 ft10 ft20 la01 la02 la03 la04 la05 la06 la07 la08 la09 la10 la11 la12 la13 la14 la15 la16 la17 la18 la19 la20 la21 la22 la23 la24 la25 la26 la27 la28 la29 la30 la31 la32 la33 la34 la35 la36 la37 la38 la39 la40 
 */
	
	/** All solvers available in this program */
    private static HashMap<String, Solver> solvers;
    static {
        solvers = new HashMap<>();
        solvers.put("basic", new BasicSolver());
        solvers.put("random", new RandomSolver());
        solvers.put("random_glouton", new GloutonAleatoire());
        solvers.put("SPT", new Glouton("SPT"));
        solvers.put("LPT", new Glouton("LPT"));
        solvers.put("SRPT", new Glouton("SRPT"));
        solvers.put("LRPT", new Glouton("LRPT"));   
        solvers.put("EST_SPT", new GloutonTime("SPT"));
        solvers.put("EST_LPT", new GloutonTime("LPT"));
        solvers.put("EST_SRPT", new GloutonTime("SRPT"));
        solvers.put("EST_LRPT", new GloutonTime("LRPT")); 
        solvers.put("descentSolver", new DescentSolver()); 
        solvers.put("TabouSolver", new TabouSolver(800000000,20)); 
        solvers.put("RecuitSolver", new RecuitSolver(100000000,100,175)); //100 175 1s 
        solvers.put("genetique", new GeneticSolver(50,10000000,40, 1f,4)); //80
        // add new solvers here
    }


    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("jsp-solver").build()
                .defaultHelp(true)
                .description("Solves jobshop problems.");

        parser.addArgument("-t", "--timeout")
                .setDefault(1L)
                .type(Long.class)
                .help("Solver timeout in seconds for each instance");
        parser.addArgument("--solver")
                .nargs("+")
                .required(true)
                .help("Solver(s) to use (space separated if more than one)");

        parser.addArgument("--instance")
                .nargs("+")
                .required(true)
                .help("Instance(s) to solve (space separated if more than one)");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        PrintStream output = System.out;

        long solveTimeMs = ns.getLong("timeout") * 1000;

        List<String> solversToTest = ns.getList("solver");
        for(String solverName : solversToTest) {
            if(!solvers.containsKey(solverName)) {
                System.err.println("ERROR: Solver \"" + solverName + "\" is not avalaible.");
                System.err.println("       Available solvers: " + solvers.keySet().toString());
                System.err.println("       You can provide your own solvers by adding them to the `Main.solvers` HashMap.");
                System.exit(1);
            }
        }
        List<String> instancePrefixes = ns.getList("instance");
        List<String> instances = new ArrayList<>();
        for(String instancePrefix : instancePrefixes) {
            List<String> matches = BestKnownResult.instancesMatching(instancePrefix);
            if(matches.isEmpty()) {
                System.err.println("ERROR: instance prefix \"" + instancePrefix + "\" does not match any instance.");
                System.err.println("       available instances: " + Arrays.toString(BestKnownResult.instances));
                System.exit(1);
            }
            instances.addAll(matches);
        }

        float[] runtimes = new float[solversToTest.size()];
        float[] distances = new float[solversToTest.size()];

        try {
            output.print(  "                         ");
            for(String s : solversToTest)
                output.printf("%-30s", s);
            output.println();
            output.print("instance size  best      ");
            for(String s : solversToTest) {
                output.print("runtime makespan ecart        ");
            }
            output.println();


            for(String instanceName : instances) {
                int bestKnown = BestKnownResult.of(instanceName);


                Path path = Paths.get("instances/", instanceName);
                Instance instance = Instance.fromFile(path);

                output.printf("%-8s %-5s %4d      ",instanceName, instance.numJobs +"x"+instance.numTasks, bestKnown);

                for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                    String solverName = solversToTest.get(solverId);
                    Solver solver = solvers.get(solverName);
                    long start = System.currentTimeMillis();
                    long deadline = System.currentTimeMillis() + solveTimeMs;
                    Result result = solver.solve(instance, deadline);
                    long runtime = System.currentTimeMillis() - start;
                    System.gc();
                    if(!result.schedule.isValid()) {
                        System.err.println("ERROR: solver returned an invalid schedule");
                        System.exit(1);
                    }

                    assert result.schedule.isValid();
                    int makespan = result.schedule.makespan();
                    float dist = 100f * (makespan - bestKnown) / (float) bestKnown;
                    runtimes[solverId] += (float) runtime / (float) instances.size();
                    distances[solverId] += dist / (float) instances.size();

                    output.printf("%7d %8s %5.1f        ", runtime, makespan, dist);
                    output.flush();
                }
                output.println();

            }


            output.printf("%-8s %-5s %4s      ", "AVG", "-", "-");
            for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                output.printf("%7.1f %8s %5.1f        ", runtimes[solverId], "-", distances[solverId]);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

