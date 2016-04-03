package agar;

import java.awt.*;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Rectangle;

public class AgarBot {
	
	static Detection dct = new Detection();
		
    public static void main(String... args) throws Exception {

    	Robot robot = new Robot();
        double newLocation[];
        double newLocationTemp[];

		if(args.length==0)
			Thread.sleep(Const.DELAY*5);		// default delay of 5 seconds before bot starts
		else
			Thread.sleep(Const.DELAY*Integer.parseInt(args[0]));	
		
		while(true)
		{
			dct.detect();							// Capture screen and process it
			if(dct.circleList.size()!=0)
			{
				newLocation = flee();
				double angle,range;
				CircleInfo bot=dct.circleList.get(0);
				if(newLocation[0]==bot.x && newLocation[1]==bot.y){	//no flee, seek range -PI to PI
					angle=0;
					range=Math.PI;
				}
				else{					
					angle = angleFinder(newLocation[0],newLocation[1]);
					range = rangeFinder(angle);
					//System.out.println("angle: "+angle+" | range: "+range);
				}
				newLocationTemp = seek(angle,range);
				if(!(newLocationTemp[0]==0 && newLocationTemp[1]==0)){
					newLocation=newLocationTemp;
				}

				// System.out.println(" | x: "+newLocation[0]+" | y: "+newLocation[1] + " list size : "+dct.circleList.size());

				robot.mouseMove((int)(newLocation[0] * 1920.0/1280),(int)(newLocation[1] * 1080.0/720));	
				
				//if( dct.circleList.size() > 150 )	// after game ends, circle List floods. End program
				//	break;
				dct.circleList.clear();	
			}
		}
    }
	static double euclidean(double x1,double y1,double x2,double y2){
		return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
	}

	static double[] flee()
	{
		CircleInfo bot, enemy;
		double xDelta, yDelta, magnitude, min=1280;
		double newLocation[] = new double[3];
		int minX, minY;
		
		bot = dct.circleList.get(0);
		xDelta = 0;
		yDelta = 0;

		for (int i = 1; i < dct.circleList.size(); i++) 
		{
			enemy = dct.circleList.get(i);

			if( enemy.r >= bot.r*1.25 )		// Enemy 25% bigger than us
			{
				xDelta += (bot.x-enemy.x)*enemy.weightage;
				yDelta += (bot.y-enemy.y)*enemy.weightage;
			}
		}
		// System.out.print("xDelta: "+xDelta+" | yDelta: "+yDelta);

		magnitude = euclidean(xDelta,yDelta,0,0);	// magnitude of the net vector
		if( magnitude==0 )
			magnitude = 1;							// to avoid divide by zero error

		// Offset considering bot as origin
		newLocation[0] = (bot.x + (200*xDelta/magnitude));	
		newLocation[1] = (bot.y + (200*yDelta/magnitude));
		return newLocation;
	}

	static double angleFinder(double x,double y){
		CircleInfo bot=dct.circleList.get(0);
		double angle=Math.atan2(y-bot.y,x-bot.x);
		return angle;
		
	}
	static double rangeFinder(double angle){
		
		double rangeFactor=0;
		CircleInfo enemy,bot=dct.circleList.get(0);
		for (int i = 1; i < dct.circleList.size(); i++) 
		{
			enemy = dct.circleList.get(i);

			if( enemy.r >= bot.r*1.25 )		// Enemy 25% bigger than us
			{
				if((angle+Math.PI/4)>angleFinder(enemy.x,enemy.y) && (angle-Math.PI/4)<angleFinder(enemy.x,enemy.y))
					rangeFactor+=enemy.r;
			}
		}
		rangeFactor/=bot.r;
		double maxRange=20*Math.PI/180;
		double minRange=0;
		return Math.max(0,maxRange-((maxRange-minRange)/5)*rangeFactor);
	}
	static double[] seek(double angle,double range)
	{
		CircleInfo bot, enemy;
		
		bot = dct.circleList.get(0);
		
		double xDelta, yDelta, distance, min=1280;
		double newLocation[] = new double[2];
		int	minX = 0, minY = 0;


		for (int i = 1; i < dct.circleList.size(); i++) 
		{
			enemy = dct.circleList.get(i);
			distance = euclidean(bot.x, bot.y, enemy.x, enemy.y);

			if(angle+range>angleFinder(enemy.x,enemy.y) && angle-range<angleFinder(enemy.x,enemy.y))
			{
				//System.out.println("ex: "+enemy.x+" | ey: "+enemy.y+" eangle:"+angleFinder(enemy.x,enemy.y));
				//System.out.println("yes");
				if(distance < min && bot.r >= enemy.r*1.25)
				{
					min = distance;
					minX = enemy.x;
					minY = enemy.y;
				}
			}
		}
		distance = euclidean(minX, minY, bot.x, bot.y);

		if(distance >= 200)				// Normalizing
		{
			newLocation[0] = bot.x + (200*(minX-bot.x)/distance);
			newLocation[1] = bot.y + (200*(minY-bot.y)/distance);
		}
		else
		{
			newLocation[0] = minX;
			newLocation[1] = minY;
		}
		return newLocation;
	}
}