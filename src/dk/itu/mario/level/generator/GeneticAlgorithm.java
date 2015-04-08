package dk.itu.mario.level.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.level.Level;
import dk.itu.mario.level.MyLevel;

public class GeneticAlgorithm 
{
	private static final int NORMAL_POPULATION = 10;
	private static final int MAX_POPULATION = 50;
	private static final double THRESHOLD = 0.00005;
	private static final double PROB_CROSSOVER = 0.80;
	private static final double PROB_MUTATE = 0.50;
	public int interationCount = 0;
	private double prevError = Double.MAX_VALUE;
	public Level bestLevel;
	public double deltaError = THRESHOLD;
	
	private ArrayList<Pair<Level, Double>> population = new ArrayList<Pair<Level, Double>>();
	private GamePlay model;
	
	
	public GeneticAlgorithm(int width, int height, int type, GamePlay playerMetrics)
	{
		this.model = playerMetrics;
		Random rand = new Random();
		
		//Create initial population
		for(int i=0;i<NORMAL_POPULATION;i++)
		{
			Level level = new MyLevel(width, height, rand.nextLong(), 0, type, playerMetrics);
			population.add(new Pair<Level, Double>(level, Fitness(level)));
		}
	}
	
	public void run()
	{
		this.interationCount++;
		double error = 0;
		Random rand = new Random();
		population.sort(new LevelComparator());

		//TODO: write genetic algorithm run sequence
		while(population.size()<MAX_POPULATION)
		{
			Pair<Level, Double> level1 = weightedPick();
			Pair<Level, Double> level2 = weightedPick();
			
			Level offspring = null;
			if(rand.nextDouble()<=PROB_CROSSOVER)
			{
				//TODO: do crossover of level1 and level2
			}
			else
			{
				if(rand.nextDouble()<=0.5)
					offspring = level1.level;
				else
					offspring = level2.level;
			}
			
			if(rand.nextDouble()<=PROB_MUTATE)
			{
				//TODO: do mutation of offspring
			}
			
			population.add(new Pair<Level,Double>(offspring, Fitness(offspring)));
		}
		
		for(int i=0;i<population.size();i++)
			error+=population.get(i).fitness;
		
		this.deltaError = Math.abs(error-this.prevError);
		this.prevError = error;
		population.sort(new LevelComparator());
		population = (ArrayList<Pair<Level, Double>>) population.subList(0, NORMAL_POPULATION);
		this.bestLevel = population.get(0).level;
	}
	
	public boolean isDone()
	{
		return this.deltaError<THRESHOLD;
	}
	
	public static double Fitness(Level level)
	{
		double fitness = 0;
		//TODO: write fitness function and map to 0 - 1
		
		
		return fitness;
	}
	
	private static double map(double X, double A, double B, double C, double D)
	{
		return (X-A)/(B-A) * (D-C) + C;
	}
	
	private Pair<Level, Double> weightedPick()
	{
		Random rand = new Random();
		double sumOfWeights = 0;
		for(int i=0;i<population.size();i++)
			sumOfWeights+=population.get(i).fitness;
		
		double randNum = rand.nextDouble()*sumOfWeights;
		for(int i=0;i<population.size();i++)
		{
			if(randNum < population.get(i).fitness)
				return population.get(i);
			randNum-=population.get(i).fitness;
		}
		return null;
	}
}

class LevelComparator implements Comparator<Object>
{
	@SuppressWarnings("unchecked")
	@Override
	public int compare(Object arg0, Object arg1)
	{
		Pair<Level,Double> o1 = (Pair<Level,Double>)arg0;
		Pair<Level,Double> o2 = (Pair<Level,Double>)arg1;
		double f = o1.fitness-o2.fitness;
		if(f>0)
			return 1;
		else if(f<0)
			return -1;
		else
			return 0;
	}
}

class Pair<A, B>
{
	A level;
	B fitness;
	public Pair(A level, B fitness)
	{
		this.level = level;
		this.fitness = fitness;
	}
}