package dk.itu.mario.level.generator;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.MyLevel;

public class MyLevelGenerator extends CustomizedLevelGenerator implements LevelGenerator{

	public LevelInterface generateLevel(GamePlay playerMetrics)
	{
		int width = 320;
		int height = 15;
		
        GeneticAlgorithm ga = new GeneticAlgorithm(width, height, LevelInterface.TYPE_OVERGROUND, playerMetrics);
		while(!ga.isDone())
		{
			ga.run();
			if((ga.interationCount%1000)==0)
				System.out.println("Running iteration: " + ga.interationCount);
		}
		LevelInterface level = ga.getLevel();
		return level;
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
