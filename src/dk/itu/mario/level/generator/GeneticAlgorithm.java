package dk.itu.mario.level.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.Level;
import dk.itu.mario.level.MyLevel;

public class GeneticAlgorithm 
{
	//		    Coins Jumps Goombas
	//Hunter     3    20     5
	//Collector  35   55     0
	//Jumper     0    124    0
	
	
	private static final int NORMAL_POPULATION = 10;
	private static final int MAX_POPULATION = 50;
	private static final double THRESHOLD = 0.00005;
	private static final double PROB_CROSSOVER = 0.90;
	private static final double PROB_MUTATE = 0.60;
	private static final int MAX_ITERATIONS = 7000;
	public int interationCount = 0;
	private double prevError = Double.MAX_VALUE;
	public Level bestLevel;
	public double deltaError = THRESHOLD;
	
	private static final double COINSCALE = 5;
	private static final double JUMPSCALE = 8;
	private static final double GOOMBASCALE = 2;
	
	private static final double AVG_JUMPS = 60;
	private static final double AVG_COINS = 10;
	private static final double AVG_GOOMBA = 2;
	
	private static final byte COIN = (byte) (2 + 2 * 16);
	private static final byte HILL_TOP_LEFT = (byte) (4 + 8 * 16);
	private static final byte CANNON_TOP = (byte) (14 + 0 * 16);
	private static final byte TUBE_TOP_LEFT = (byte) (10 + 0 * 16);
	private static final byte LEFT_GRASS_EDGE = (byte) (0+9*16);
	private static final byte HILL_FILL = (byte) (5 + 9 * 16);
    private static final byte HILL_LEFT = (byte) (4 + 9 * 16);
    private static final byte HILL_RIGHT = (byte) (6 + 9 * 16);
    private static final byte HILL_TOP = (byte) (5 + 8 * 16);
    private static final byte HILL_TOP_RIGHT = (byte) (6 + 8 * 16);
    private static final byte HILL_TOP_LEFT_IN = (byte) (4 + 11 * 16);
    private static final byte HILL_TOP_RIGHT_IN = (byte) (6 + 11 * 16);
    private static final byte TUBE_TOP_RIGHT = (byte) (11 + 0 * 16);
    private static final byte TUBE_SIDE_LEFT = (byte) (10 + 1 * 16);
    private static final byte TUBE_SIDE_RIGHT = (byte) (11 + 1 * 16);
	
	
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
		ArrayList<Pair<Level, Double>> populationOld = population;
		
		while(population.size()<MAX_POPULATION)
		{
			Pair<Level, Double> level1 = weightedPick(populationOld);
			Pair<Level, Double> level2 = weightedPick(populationOld);
			
			Level offspring = null;
			if(rand.nextDouble()<=PROB_CROSSOVER)
			{
				//TODO: do crossover of level1 and level2
//				System.out.println("Crossover");
				offspring = crossover(level1.level, level2.level);
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
//				System.out.println("Mutation");
				//TODO: do mutation of offspring
				offspring = mutate(offspring);
			}
			
			population.add(new Pair<Level,Double>(offspring, Fitness(offspring)));
		}
		
		for(int i=0;i<population.size();i++)
			error+=population.get(i).fitness;
		
		this.deltaError = Math.abs(error-this.prevError);
		this.prevError = error;
		population.sort(new LevelComparator());
		population = new ArrayList<Pair<Level,Double>>(population.subList(0, NORMAL_POPULATION));
		this.bestLevel = population.get(0).level;
		System.out.print(Fitness(this.bestLevel));
		System.out.print(" ");
		System.out.println(Arrays.toString(countElements(this.bestLevel)));
	}
	
	public boolean isDone()
	{
		return this.deltaError<THRESHOLD || this.interationCount>MAX_ITERATIONS;
	}
	
	public double Fitness(Level level)
	{
		int[] counts = countElements(level);
		int numCoins = counts[0];
		int numJumps = counts[1];
		int numGoomba = counts[2];
		Random rand = new Random();
		
		int r = (int)(this.model.coinsCollected*COINSCALE/AVG_COINS);
		int coinRand = 0;
		if(r>0)
			coinRand = rand.nextInt(r);
		double coinFit = numCoins*1.0/(this.model.coinsCollected+coinRand);
		
		r = (int)(this.model.jumpsNumber*JUMPSCALE/AVG_JUMPS);
		coinRand = 0;
		if(r>0)
			coinRand = rand.nextInt(r);
		double jumpFit = numJumps*1.0/(this.model.jumpsNumber+coinRand);
		
		r = (int)(this.model.GoombasKilled*GOOMBASCALE/AVG_GOOMBA);
		coinRand = 0;
		if(r>0)
			coinRand = rand.nextInt(r);
		double goombaFit = numGoomba*1.0/(this.model.GoombasKilled+coinRand);

		if(coinFit>1.5)
			coinFit = 1/(coinFit*2);
		else if(coinFit>1.0)
			coinFit = 1.0;
		if(jumpFit>1.5)
			jumpFit = 1/(jumpFit*2);
		else if(jumpFit>1.0)
			jumpFit = 1.0;
		if(goombaFit>1.5)
			goombaFit = 1/(goombaFit*2);
		else if(goombaFit>1.0)
			goombaFit = 1.0;
		
		/*
		 * if(coinFit>1.0)
		 * 		coinFit = 1.0;
		 * if(goombaFit>1.0)
		 * 		goombaFit = 1.0;
		 * if(jumpFit>1.0)
		 * 		jumpFit = 1.0;
		 * 
		 */
		
		return (coinFit+jumpFit+goombaFit)/3.0;
	}
	
	private Level crossover(Level l1, Level l2)
	{
		Level ret = l1;
		Random rand = new Random();
		int split = rand.nextInt(l1.getWidth());
		
		for(int i=0;i<l1.getHeight();i++)
		{
			byte b = l1.getBlock(split, i);
			if(b == HILL_FILL || b == HILL_LEFT || b == HILL_RIGHT || 
					b == TUBE_SIDE_LEFT || b == TUBE_SIDE_RIGHT)
			{
				return crossover(l1,l2);
			}
		}
		
		for(int i=split;i<l1.getWidth();i++)
		{
			for(int j=0;j<l1.getHeight();j++)
			{
				//TODO Check either side of this block and try to change surrounding blocks to create transition
				ret.setBlock(i, j, l2.getBlock(i, j));
				ret.setSpriteTemplate(i, j, l2.getSpriteTemplate(i, j));
			}
		}
		
		return ret;
	}
	
	private Level mutate(Level offspring)
	{
		Random rand = new Random();
		int x = rand.nextInt(offspring.getWidth());
		int y = rand.nextInt(offspring.getHeight());
		if(offspring.getBlock(x, y) == 0)
			offspring.setBlock(x, y, COIN);
		return offspring;
	}
	
	private static double map(double X, double A, double B, double C, double D)
	{
		return (X-A)/(B-A) * (D-C) + C;
	}
	
	private Pair<Level, Double> weightedPick(ArrayList<Pair<Level, Double>> population)
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
		
		return population.get(rand.nextInt(population.size()));
	}
	
	private static int[] countElements(Level level)
	{
		//Coins Jumps Goombas
		int[] ret = {0,0,0};
		byte[][] map = level.getMap();
		SpriteTemplate[][] sprites = level.getSpriteTemplate();
		
		for(int x=0;x<level.getWidth();x++)
		{
			for(int y=0;y<level.getHeight();y++)
			{
				if(map[x][y] == COIN)
					ret[0]++;
				if(map[x][y] == HILL_TOP_LEFT || map[x][y] == CANNON_TOP || map[x][y] == LEFT_GRASS_EDGE 
						|| map[x][y] == TUBE_TOP_LEFT)
					ret[1]++;
				if(sprites[x][y]!=null && sprites[x][y].type==SpriteTemplate.GOOMPA)
				{
					ret[2]++;
					ret[1]++; //Have to jump over goombas
				}
			}
		}
		return ret;
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