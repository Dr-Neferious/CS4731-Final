package dk.itu.mario.level.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
	private static final int MAX_ITERATIONS = 10000;
	public int interationCount = 0;
	private double prevError = Double.MAX_VALUE;
	public double deltaError = THRESHOLD;
	
	private static final double COINSCALE = 5;
	private static final double JUMPSCALE = 8;
	private static final double GOOMBASCALE = 2;
	
	private static final double AVG_JUMPS = 20;
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
	
	
	private ArrayList<MyLevel> population = new ArrayList<MyLevel>();
	private GamePlay model;
	
	MyLevel bestLevel = null;
	
	public GeneticAlgorithm(int width, int height, int type, GamePlay playerMetrics)
	{
		this.model = playerMetrics;
		Random rand = new Random();
		
		//Create initial population
		for(int i=0;i<NORMAL_POPULATION;i++)
		{
			MyLevel level = new MyLevel(width, height, rand.nextLong(), 0, type, playerMetrics);
			level.fitness = Fitness(level);
			population.add(level);
			bestLevel = level;
		}
	}
	
	public void run()
	{
		this.interationCount++;
		double error = 0;
		Random rand = new Random();
		population.sort(new LevelComparator());
//		ArrayList<Pair<Level, Double>> populationOld = population;
		
		while(population.size()<MAX_POPULATION)
		{
			MyLevel level1 = weightedPick(population);
			MyLevel level2 = weightedPick(population);
			
			MyLevel offspring = null;
			if(rand.nextDouble()<=PROB_CROSSOVER)
			{
				//TODO: do crossover of level1 and level2
//				System.out.println("Crossover");
				offspring = crossover(level1, level2);
			}
			else
			{
				if(rand.nextDouble()<=0.5)
					offspring = level1;
				else
					offspring = level2;
			}
			
			if(rand.nextDouble()<=PROB_MUTATE)
			{
//				System.out.println("Mutation");
				//TODO: do mutation of offspring
				offspring = mutate(offspring);
			}
			
			offspring.fitness = Fitness(offspring);
			population.add(offspring);
		}
		
		for(int i=0;i<population.size();i++)
			error+=population.get(i).fitness;
		
		this.deltaError = Math.abs(error-this.prevError);
		this.prevError = error;
		population.sort(new LevelComparator());
//		System.out.println(population.get(0).fitness + "\t" + population.get(population.size()-1).fitness);

		for(int i=NORMAL_POPULATION;i<population.size()-NORMAL_POPULATION;i++)
			population.remove(i);
		
		System.out.print(population.get(0).fitness);
		System.out.print(" ");
		System.out.print(Arrays.toString(countElements(population.get(0))));
		System.out.print(" ");
		System.out.println(bestLevel.fitness);
		System.out.print(" ");
		System.out.println(Arrays.toString(countElements(bestLevel)));
		
		if(population.get(0).fitness>bestLevel.fitness)
			bestLevel = population.get(0).cloneMyLevel();
		bestLevel.fitness = Fitness(bestLevel);
	}
	
	public boolean isDone()
	{
//		return this.deltaError<THRESHOLD || this.interationCount>MAX_ITERATIONS;
		return this.interationCount>MAX_ITERATIONS;
	}
	
	public Level getLevel()
	{
		return bestLevel;
	}
	
	public double Fitness(MyLevel level)
	{
		int[] counts = countElements(level);
		int numCoins = counts[0];
		int numJumps = counts[1];
		int numGoomba = counts[2];
//		Random rand = new Random();
		
		int r = (int)(this.model.coinsCollected*COINSCALE/AVG_COINS);
//		int coinRand = 0;
//		if(r>0)
//			coinRand = rand.nextInt(r);
		double coinFit = numCoins*1.0/(this.model.coinsCollected);
		
		r = (int)(this.model.jumpsNumber*JUMPSCALE/AVG_JUMPS);
//		coinRand = 0;
//		if(r>0)
//			coinRand = rand.nextInt(r);
		double jumpFit = numJumps*1.0/(this.model.jumpsNumber);
		
		r = (int)(this.model.GoombasKilled*GOOMBASCALE/AVG_GOOMBA);
//		coinRand = 0;
//		if(r>0)
//			coinRand = rand.nextInt(r);
		double goombaFit = numGoomba*1.0/(this.model.GoombasKilled);
		
		if(Double.isNaN(coinFit))
			coinFit = 0.0;
		if(Double.isNaN(goombaFit))
			goombaFit = 0.0;
		if(Double.isNaN(jumpFit))
			jumpFit = 0.0;

		if(coinFit>2)
			coinFit = -1;
		else if(coinFit>1.5)
			coinFit = 1/(coinFit*2);
		else if(coinFit>1.0)
			coinFit = 1.0;
		
		if(jumpFit>2)
			jumpFit = -1;
		else if(jumpFit>1.5)
			jumpFit = 1/(jumpFit*2);
		else if(jumpFit>1.0)
			jumpFit = 1.0;

		if(goombaFit>3)
			goombaFit = -1;
		else if(goombaFit>2)
			goombaFit = 1/(goombaFit*2);
		else if(goombaFit>1.0)
			goombaFit = 1.0;
		
		double fitness = (coinFit+jumpFit+goombaFit)/3.0;
		if(Double.isNaN(fitness) || Double.isInfinite(fitness))
			return 0.0;
		return fitness;
	}
	
	private MyLevel crossover(MyLevel l1, MyLevel l2)
	{
		MyLevel ret = l1;
		Random rand = new Random();
		int split = rand.nextInt(l1.getWidth());
		boolean redo = false;
		do
		{
			redo = false;
			split = rand.nextInt(l1.getWidth());
			
			for(int i=0;i<l1.getHeight();i++)
			{
				byte b = l1.getBlock(split, i);
				if(b == HILL_FILL || b == HILL_LEFT || b == HILL_RIGHT || 
						b == TUBE_SIDE_LEFT || b == TUBE_SIDE_RIGHT)
				{
					redo = true;
					break;
				}
			}
		} while(redo);
		
		for(int i=split;i<l1.getWidth();i++)
		{
			for(int j=0;j<l1.getHeight();j++)
			{
				//TODO Check either side of this block and try to change surrounding blocks to create transition
				ret.setBlock(i, j, l2.getBlock(i, j));
				ret.setSpriteTemplate(i, j, l2.getSpriteTemplate(i, j));
			}
		}
		
		//TODO Clear end of level and place goal
		
		return ret;
	}
	
	private MyLevel mutate(MyLevel offspring)
	{
		Random rand = new Random();
		int choice = rand.nextInt(8);
		switch(choice)
		{
			//Add coin
			case(0):
			{
				int x = rand.nextInt(offspring.getWidth());
				int y = rand.nextInt(offspring.getHeight());
				while(offspring.getBlock(x, y)!=0)
				{
					x = rand.nextInt(offspring.getWidth());
					y = rand.nextInt(offspring.getHeight());
				}
				offspring.setBlock(x, y, COIN);
				break;
			}
			//Remove coin
			case(1):
			{
				removeRandomCoin(offspring);
				break;
			}
			//Add Goomba
			case(2):
			{
				int x = rand.nextInt(offspring.getWidth());
				int y = rand.nextInt(offspring.getHeight());
				if(offspring.getBlock(x, y)==0)
					offspring.setSpriteTemplate(x, y, new SpriteTemplate(SpriteTemplate.GOOMPA, false));
				break;
			}
			//Remove Goomba
			case(3):
			{
				removeRandomGoomba(offspring);
				break;
			}
			//Add Hills
			case(4):
			{
				
				break;
			}
			//Remove Hills
			case(5):
			{
				
				break;
			}
			//Add Pipes
			case(6):
			{
				
				break;
			}
			//Remove Pipes
			case(7):
			{
				
				break;
			}
			default:
				break;
		}
				
		return offspring;
	}
	
	private static double map(double X, double A, double B, double C, double D)
	{
		return (X-A)/(B-A) * (D-C) + C;
	}
	
	private MyLevel weightedPick(ArrayList<MyLevel> population)
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
	
	private static void removeRandomCoin(Level level)
	{
		ArrayList<int[]> coinsLocations = new ArrayList<int[]>();
		byte[][] map = level.getMap();
		
		for(int x=0;x<level.getWidth();x++)
		{
			for(int y=0;y<level.getHeight();y++)
			{
				if(map[x][y] == COIN)
				{
					int[] t = {x,y};
					coinsLocations.add(t);
				}
			}
		}
		
		Random rand = new Random();
		if(coinsLocations.isEmpty())
			return;
		int[] loc = coinsLocations.get(rand.nextInt(coinsLocations.size()));
		level.setBlock(loc[0], loc[1], (byte)0);
	}
	
	private static void removeRandomGoomba(Level level)
	{
		ArrayList<int[]> goombaLocations = new ArrayList<int[]>();
		SpriteTemplate[][] sprites = level.getSpriteTemplate();
		
		for(int x=0;x<level.getWidth();x++)
		{
			for(int y=0;y<level.getHeight();y++)
			{
				if(sprites[x][y] != null && sprites[x][y].type == SpriteTemplate.GOOMPA)
				{
					int[] t = {x,y};
					goombaLocations.add(t);
				}
			}
		}
		
		Random rand = new Random();
		if(goombaLocations.isEmpty())
			return;
		int[] loc = goombaLocations.get(rand.nextInt(goombaLocations.size()));
		level.setSpriteTemplate(loc[0], loc[1], null);
	}

}

class Pair<A, B>
{
	public A level;
	public B fitness;
	public Pair(A level, B fitness)
	{
		this.level = level;
		this.fitness = fitness;
	}
}

class LevelComparator implements Comparator<MyLevel>
{
	public int compare(MyLevel arg0, MyLevel arg1)
	{
		double f = arg0.fitness-arg1.fitness;
		if(f>0)
			return -1;
		else if(f<0)
			return 1;
		else
			return 0;
	}
}


