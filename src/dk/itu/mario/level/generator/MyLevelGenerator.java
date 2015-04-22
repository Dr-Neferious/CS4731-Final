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
		
		System.out.println(playerMetrics.coinsCollected);
		System.out.println(playerMetrics.jumpsNumber);
		System.out.println(playerMetrics.GoombasKilled);
		
        GeneticAlgorithm ga = new GeneticAlgorithm(width, height, LevelInterface.TYPE_OVERGROUND, playerMetrics);
		while(!ga.isDone())
		{
			ga.run();
			if((ga.interationCount%100)==0)
				System.out.println(ga.interationCount);
		}
		LevelInterface level = ga.bestLevel;
		
			
//		LevelInterface level = new MyLevel(320,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND,playerMetrics);
		return level;
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
