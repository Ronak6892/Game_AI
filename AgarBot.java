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
    public static final int DELAY = 1000;
    public static final int RADIUS = 400;
	

    public static void main(String... args) throws Exception {
		//screenSize.setSize(1280,720);
        Robot robot = new Robot();
        Random random = new Random();
		Detection dct=new Detection();
		
		//double width = screenSize.getWidth();
		//double height = screenSize.getHeight();
		//System.out.println(width+" : "+height);
		
		Thread.sleep(DELAY*5);
		
		//int count=0;
		while(true){
			dct.detect();//count);
			if(dct.circleList.size()!=0){
				int x0=dct.circleList.get(0).x;
				int y0=dct.circleList.get(0).y;
				int r0=dct.circleList.get(0).r;
				double min=1280;
				int minx=x0,miny=y0;
				for (int i = 1; i < dct.circleList.size(); i++) {
					int x=dct.circleList.get(i).x;
					int y=dct.circleList.get(i).y;
					int r=dct.circleList.get(i).r;
					
					double dist=euclidean(x0,y0,x,y);
					if(dist<min && r0>=r*1.25){
						min=dist;
						minx=x;
						miny=y;
					}
				}
				System.out.println("minx: "+minx+" | miny: "+miny + " list size : "+dct.circleList.size());
				robot.mouseMove((int)(minx*1920.0/1280),(int)(miny*1080.0/720));
				dct.circleList.clear();
				//count++;
			}
			Thread.sleep(DELAY/10);
		}
		/*
		double i=0;
        while (i<10*Math.PI) {
            robot.mouseMove((int)(200 * Math.sin(i)+width/2), (int)(200 * Math.cos(i)+height/2));
			i=i+0.1;
			//System.out.println(i%1.0 + " " + i%2);
			
			process(robot);
            //robot.mouseMove(random.nextInt(MAX_X), random.nextInt(MAX_Y));
            Thread.sleep(DELAY/100);
		}*/
    }
	static double euclidean(int x1,int y1,int x2,int y2){
		return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
	}
}