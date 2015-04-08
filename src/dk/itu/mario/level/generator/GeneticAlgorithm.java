package dk.itu.mario.level.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.level.Level;
import dk.itu.mario.level.MyLevel;

public class GeneticAlgorithm 
{
	private int MAX_POPULATION = 50;
	public static final double THRESHOLD = 0.00005;
	public int interationCount = 0;
	private double prevError = Double.MAX_VALUE;
	public Level bestLevel;
	public double deltaError = THRESHOLD;
	
	private ArrayList<Pair<Level, Double>> population = new ArrayList<Pair<Level, Double>>();
	private GamePlay model;
	
	
	public GeneticAlgorithm(int width, int height, int type, GamePlay playerMetrics)
	{
		this.model = playerMetrics;
		
		//Create initial population
		for(int i=0;i<MAX_POPULATION;i++)
		{
			Level level = new MyLevel(width, height, System.currentTimeMillis(), 0, type, playerMetrics);
			population.add(new Pair<Level, Double>(level, Fitness(level)));
		}
	}
	
	public void run()
	{
		this.interationCount++;
		int error = 0;
		population.sort(new LevelComparator());
		
		
		//TODO: write genetic algorithm run sequence
		
		
		
		this.deltaError = Math.abs(error-this.prevError);
		this.prevError = error;
		population.sort(new LevelComparator());
		this.bestLevel = population.get(0).level;
	}
	
	public boolean isDone()
	{
		return this.deltaError<THRESHOLD;
	}
	
	public static double Fitness(Level level)
	{
		double fitness = 0;
		//TODO: write fitness function
		
		
		return fitness;
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