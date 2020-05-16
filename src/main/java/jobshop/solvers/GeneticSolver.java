package jobshop.solvers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;

public class GeneticSolver implements Solver {
	public int taillePopulation;
	public int taillePopEnfant;
	public int nbGeneration;
	int tailleTournoi;
	int nbParents;
	JobNumbers[] parents;
	JobNumbers[] enfants;
	float probaMut;
	int tailleSequence;
	PopulationComparateur comparateur=new PopulationComparateur();
	/*
	 * Taille des population paire nécessaire, variante EPPX popEnfant multiple de 3
	 */
	public GeneticSolver(int taillePopulation,int nbGeneration, int taillePopEnfant, float probaMut,int tailleTournoi) {//12,200,6, 0.2,3
		this.taillePopulation=taillePopulation;
		this.nbGeneration=nbGeneration;
		this.taillePopEnfant=taillePopEnfant;
		this.nbParents=taillePopEnfant;//2 parents pour 2 enfants
		parents=new JobNumbers[nbParents];
		this.probaMut=probaMut;
		this.tailleTournoi=tailleTournoi;
		enfants=new JobNumbers[3];
	}
	private void tournamentSelection(ArrayList<JobNumbers> population,int tailleTournoi, int nbParents) {
		JobNumbers[] contestants=new JobNumbers[tailleTournoi];
		for(int p=0; p< nbParents; p++) {
			for(int i=0; i< tailleTournoi;i++) {
				int randomId = (int)(Math.random() * (taillePopulation));
		        contestants[i]=population.get(randomId);
			}
			Arrays.sort(contestants,new PopulationComparateur());
			parents[p]=contestants[0].copy();
		}
	}
	//Extensive Precedence Preservative Crossover (EPPX) https://aip-scitation-org.gorgone.univ-toulouse.fr/doi/pdf/10.1063/1.4801285
	//PPX with one more parent => +diversification
	private void croisementMultiple(JobNumbers pere, JobNumbers mere,JobNumbers ami ) {
		int mask[] = new int[tailleSequence];
		enfants[0] = new JobNumbers(pere.instance);
		enfants[1] = new JobNumbers(pere.instance);
		enfants[2] = new JobNumbers(pere.instance);
		//pour générer 3 enfants en retournant le masque
		for(int e=0; e<3;e++) {
				LinkedList<Integer> p1=new LinkedList<Integer>();
				LinkedList<Integer> p2=new LinkedList<Integer>();
				LinkedList<Integer> p3=new LinkedList<Integer>();
				//initialisation du masque et des copies des parents
				for(int i=0; i<tailleSequence; i++) {
					if(e==0)//premier enfant:générztion du masque
						mask[i]=(int)(Math.random() * (3));//entre 0 et 2 inclus
					else {
						if(mask[i]==0)
							mask[i]=3; // on inverse le masque
						else
							mask[i]--;
					}
					p1.add(pere.jobs[i]);
					p2.add(mere.jobs[i]);
					p3.add(ami.jobs[i]);
				}
				for(int k=0; k<tailleSequence; k++) {
					//choix des chromosomes chez le parent 1
					if(mask[k]==0) {
						//une fois sélectionné on supprime le gène (pour avancer dans les gènes du parent)
						int job=p1.removeFirst();
						//on affecte le gène à l'enfant
						enfants[e].jobs[k]=job;
						//on enlève la première occurence du gène chez l'autre parent
						p2.removeFirstOccurrence(job);
						p3.removeFirstOccurrence(job);
						//pareil mais avec les 2 parents
					}else if(mask[k]==1){
						int job=p2.removeFirst();
						enfants[e].jobs[k]=job;
						p1.removeFirstOccurrence(job);
						p3.removeFirstOccurrence(job);
					}else {
						int job=p3.removeFirst();
						enfants[e].jobs[k]=job;
						p1.removeFirstOccurrence(job);
						p2.removeFirstOccurrence(job);
					}
				}
		}
		//enfant.nextToSet=tailleSequence
	}
	//Precedence Preservative Crossover (PPX) https://pdfs.semanticscholar.org/adcb/9c1bd0e39541146149dca58ddef11945a384.pdf
	//avoid repairs, conserve position: The PPX perfectly respects the absolute order of genes in parental chromosomes
	private void croisement(JobNumbers pere, JobNumbers mere) {
		int mask[] = new int[tailleSequence];
		enfants[0] = new JobNumbers(pere.instance);
		enfants[1] = new JobNumbers(pere.instance);
		//pour générer 2 enfants en retournant le masque
		for(int e=0; e<2;e++) {
				LinkedList<Integer> p1=new LinkedList<Integer>();
				LinkedList<Integer> p2=new LinkedList<Integer>();
				//initialisation du masque et des copies des parents
				for(int i=0; i<tailleSequence; i++) {
					if(e==0)//premier enfant:générztion du masque
						mask[i]=(int)(Math.random() * (2));//entre 0 et 1 inclus
					else
						mask[i]--; // on inverse le masque
					p1.add(pere.jobs[i]);
					p2.add(mere.jobs[i]);
				}
				for(int k=0; k<tailleSequence; k++) {
					//choix des chromosomes chez le parent 1
					if(mask[k]==0) {
						//une fois sélectionné on supprime le gène (pour avancer dans les gènes du parent)
						int job=p1.removeFirst();
						//on affecte le gène à l'enfant
						enfants[e].jobs[k]=job;
						//on enlève la première occurence du gène chez l'autre parent
						p2.removeFirstOccurrence(job);
						//pareil mais avec l'autre parent
					}else {
						int job=p2.removeFirst();
						enfants[e].jobs[k]=job;
						p1.removeFirstOccurrence(job);
					}
				}
		}
		//enfant.nextToSet=tailleSequence
	}
	//swap aléatoire
	private void mutation(int id) {
		int randomId = (int)(Math.random() * (tailleSequence));
		int randomId2 = (int)(Math.random() * (tailleSequence));
		int aux =enfants[id].jobs[randomId];
		enfants[id].jobs[randomId]=enfants[id].jobs[randomId2];
		enfants[id].jobs[randomId2]=aux;
	}
    @Override
    public Result solve(Instance instance, long deadline) {
		tailleSequence=instance.numJobs*instance.numTasks;
    	ArrayList<JobNumbers> population=new ArrayList<JobNumbers>(taillePopulation);
    	ArrayList<JobNumbers> popEnfant=new  ArrayList<JobNumbers>(taillePopEnfant);
    	Random generator = new Random(0);
    	int gene=0;
    	int mks, bestM =Integer.MAX_VALUE;
    	population.add(new JobNumbers(instance));
		for(int j = 0 ; j<instance.numJobs ; j++) {
            for(int t = 0 ; t<instance.numTasks ; t++) {
            	population.get(0).jobs[population.get(0).nextToSet++] = j;
            }
        }
    	//génération de la population initiale
    	for(int i=1; i< taillePopulation;i++) {
    		population.add(population.get(i-1).copy());//rand.solve(instance, 1500).schedule);
    		//Shuffle from previous better ?
    		RandomSolver.shuffleArray(population.get(i-1).jobs, generator);  
    		//System.out.print(" :"+population[i].toSchedule());
    	}
    	JobNumbers best=null;
    	do{
    		gene++;
    		//Tri de la population
    		//choix des parents (dans parents[] )
    		//population.sort(comparateur);
    		
    		tournamentSelection(population,tailleTournoi,nbParents);
    		
    		//**Sélection et croisement
    		//i+1 if PPX 
    		for (int i = 0; i+2 < taillePopEnfant; i++)
    		{
    			/*PPX
    			//**SÉLECTION de deux parents
    			JobNumbers pere = parents[i];
    			JobNumbers mere = parents[i+1];

    			//**CROISEMENT entre les deux parents. Création de 2 enfants.
    			croisement(pere, mere);
    			popEnfant.add(enfants[0]);
				*/
    			//EPPX
    			//**SÉLECTION des parents
    			JobNumbers pere = parents[i];
    			JobNumbers mere = parents[i+1];
    			JobNumbers ami = parents[i+2];
    			//**CROISEMENT entre les deux parents et un ami. Création de 3 enfants.
    			croisementMultiple(pere, mere,ami);
    			if (Math.random() <probaMut)
    				mutation(0);
    			popEnfant.add(enfants[0]);
    			
    			//**MUTATION ou pas de l'enfant 2
    			if (Math.random() <probaMut)
    				mutation(1);
    			popEnfant.add(enfants[1]);
    			//si EPPX
    			if (Math.random() <probaMut)
    				mutation(2);
    			popEnfant.add(enfants[2]);
    		}
    		//**REMPLACEMENT de la population pour la prochaine génération
    		population.sort(comparateur);
    		mks=population.get(0).toSchedule().makespan();
    		if(mks < bestM) {
    			best =population.get(0).copy();
    			bestM=mks;
    		}
    		//System.out.print("b:"+bestM);
    		//tous les enfants et les meilleurs parents
    		int k=(taillePopulation-taillePopEnfant);
    		for(int i=0; i< taillePopEnfant;i++) {
    			population.set(k, popEnfant.get(i));
    			k++;
    		}
    		//k meilleurs parents et taillePopEnfant/2 meilleur enfants (50/50 si autant enfants que de parents)
    		/*popEnfant.sort(comparateur);
    		int k=(taillePopulation-taillePopEnfant/2);
    		for(int i=0; i< taillePopEnfant/2;i++) {
    			population.set(k, popEnfant.get(i));
    			k++;
    		}*/
    		//meilleurs only 
    		/*
    		ArrayList<JobNumbers> all =new ArrayList<JobNumbers>(taillePopEnfant+taillePopulation);
    		for(int i=0; i<taillePopulation;i++ ) {
    			all.add(population.get(i));
    		}
    		for(int i=taillePopulation; i<taillePopulation+taillePopEnfant;i++ ) {
    			all.add(popEnfant.get(i-taillePopulation));
    		}
    		all.sort(comparateur);
    		for(int i=0; i<taillePopulation;i++ ) {
    			population.set(i, all.get(i));
    		}*/
    		popEnfant.clear();
    		// "Meilleure solution trouvee (Generation =gene) :Arrays.sort(population, comparateur); et population[0]
    		
    	}while(deadline - System.currentTimeMillis() > 1 && gene <nbGeneration );
    	population.sort(comparateur);
    	mks=population.get(0).toSchedule().makespan();
   		System.out.print("m:"+best.toSchedule().makespan());
    	if(mks<bestM) {
    		return new Result(instance, population.get(0).toSchedule(), Result.ExitCause.Blocked);
    	}
    	else {
    		return new Result(instance,best.toSchedule(), Result.ExitCause.Blocked);
    	}
    }

}
