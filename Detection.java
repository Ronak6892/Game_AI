package agar;

import java.awt.*;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Rectangle;

class CircleInfo{
		int x;
		int y;
		int r;
		
		CircleInfo(int a,int b, int c)
		{
			x = a;
			y = b;
			r = c;
		}
	}

public class Detection{
	
	List<CircleInfo> circleList = new ArrayList<CircleInfo>(); 
	
	public Detection(){
	}
    public void detect() throws Exception {//int count2) throws Exception {
		
		Const c=new Const();
        Robot robot = new Robot();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenSize.setSize(1280,720);
		

		BufferedImage img = robot.createScreenCapture(new Rectangle(screenSize));
		//ImageIO.write(img, "PNG", new File("agar/screenShot"+count2+".png"));//(Const.SCREEN_SHOT));
		//BufferedImage img=ImageIO.read(new File(Const.SCREEN_SHOT));
		//int rgb[]=img.getRGB(0,0,(int)width-1,(int)height-1,null,0,1);
		
		int rgb[][][]=new int[(int)Const.SCREEN_Y_SIZE][(int)Const.SCREEN_X_SIZE][3];
		for(int j=85;j<Const.SCREEN_Y_SIZE-50;j++){
			for(int k=0;k<Const.SCREEN_X_SIZE;k++){
				if(j>=636 && j<=656 && k>=19 && k<=182){
					rgb[j][k][0]=0;
					rgb[j][k][1]=0;
					rgb[j][k][2]=0;
				}
				else{
					Color color = new Color(img.getRGB(k,j)+Const.OFFSET);
					//System.out.println(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
					rgb[j][k][0]=color.getRed();
					rgb[j][k][1]=color.getGreen();
					rgb[j][k][2]=color.getBlue();
				}
			}
			//System.out.println();
		}
		int count=0;
		//boolean flag=false;
		boolean selfDetected=false;
		for(int j=85;j<Const.SCREEN_Y_SIZE-50;j+=5){
			for(int k=0;k<Const.SCREEN_X_SIZE;k+=5){
				if(!selfDetected){
					j=378;k=640;
				}
				if(colorComp(rgb,j,k,Const.WHITE_COLOR)){
					int left=leftEnd(rgb,j,k,Const.WHITE_COLOR);
					int right=rightEnd(rgb,j,k,Const.WHITE_COLOR);
					int hr=(right-left)/2;
					
					//System.out.println("("+j+","+k+")");
					//System.out.println("("+j+","+left+")");
					//System.out.println("("+j+","+right+")");
					//System.out.println("("+hr+")");
					
					int kprime=left+hr;
					int up=upEnd(rgb,j,kprime,Const.WHITE_COLOR);
					int down=downEnd(rgb,j,kprime,Const.WHITE_COLOR);
					int vr=(down-up)/2;
					
					//System.out.println("("+up+","+kprime+")");
					//System.out.println("("+down+","+kprime+")");
					//System.out.println("("+vr+")");
					
					int r=Math.max(hr,vr);
					int rx=left+hr;
					int ry=up+vr;
										
					removeCircle(rgb,up,down,kprime,Const.WHITE_COLOR);
					
					//System.out.print(++count+" ["+r+"] \t");
					//System.out.println("("+ry+","+rx+")");
					circleList.add(new CircleInfo(rx,ry,r));
					//flag=true;
					//break;
				}
				if(!selfDetected){
					j=85;k=0;
					selfDetected=true;
				}
				//if(flag)
				//	break;
			}
			//System.out.println();
		}
		//System.out.println(neighborComp(rgb,110,440,Const.WHITE_COLOR));
		//System.out.println(rgb[110][440][0]+","+rgb[110][440][1]+","+rgb[110][440][2]);
		//System.out.println(rgb[400][100][0]+","+rgb[400][100][1]+","+rgb[400][100][2]);
	}
	public boolean neighborComp(int rgb[][][],int j,int k,int val){
		if((rgb[j][k][0]>val && rgb[j][k][1]>val && rgb[j][k][2]>val)
			&& (rgb[j-1][k][0]>val && rgb[j-1][k][1]>val && rgb[j-1][k][2]>val)
			&& (rgb[j+1][k][0]>val && rgb[j+1][k][1]>val && rgb[j+1][k][2]>val)
			&& (rgb[j][k-1][0]>val && rgb[j][k-1][1]>val && rgb[j][k-1][2]>val)
			&& (rgb[j][k+1][0]>val && rgb[j][k+1][1]>val && rgb[j][k+1][2]>val))
			return true;
		return false;
	}
	int leftEnd(int rgb[][][],int j,int k,int val){
		for(int i=k;;i--){
			if(i==-1 || !colorComp(rgb,j,i,val)){
				return i+1;
			}
		}
	}
	int rightEnd(int rgb[][][],int j,int k,int val){
		for(int i=k;;i++){
			if(i==Const.SCREEN_X_SIZE || !colorComp(rgb,j,i,val)){
				return i-1;
			}
		}
	}
	int upEnd(int rgb[][][],int j,int k,int val){
		for(int i=j;;i--){
			if(i==85-1 || !colorComp(rgb,i,k,val)){
				return i+1;
			}
		}
	}
	int downEnd(int rgb[][][],int j,int k,int val){
		for(int i=j;;i++){
			if(i==Const.SCREEN_Y_SIZE-50 || !colorComp(rgb,i,k,val)){
				return i-1;
			}
		}
	}
	boolean colorComp(int rgb[][][],int j,int k,int val){
		if(rgb[j][k][0]>val && rgb[j][k][1]>val && rgb[j][k][2]>val)
			return true;
		return false;
	}
	void removeCircle(int rgb[][][],int up,int down,int k,int val){
		for(int i=up;i<=down;i++){
			for(int l=k;l>-1;l--){
				if(colorComp(rgb,i,l,val)){
					rgb[i][l][0]=0;
					rgb[i][l][1]=0;
					rgb[i][l][2]=0;
				}
				else{
					//System.out.println("Left("+i+","+l+")");
					break;
				}
			}
			for(int l=k+1;l<Const.SCREEN_X_SIZE;l++){
				if(colorComp(rgb,i,l,val)){
					rgb[i][l][0]=0;
					rgb[i][l][1]=0;
					rgb[i][l][2]=0;
				}
				else{
					//System.out.println("Right("+i+","+l+")");
					break;
				}
			}
		}
	}
	
	
	
	
	/*
	public static void process(Robot robot) throws Exception{
		BufferedImage screenShot = robot.createScreenCapture(new Rectangle(screenSize));
		//ImageIO.write(screenShot, "PNG", new File("screenShot.png"));
	}*/
}