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



public class Detection
{
	List<CircleInfo> circleList = new ArrayList<CircleInfo>(); 
	List<CircleInfo> virusDetectorList = new ArrayList<CircleInfo>(); 
	Const c=new Const();
	Dimension screenSize;
	
	public Detection(){
        GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = g.getScreenDevices();
        screenSize = new Dimension(devices[0].getDisplayMode().getWidth(), devices[0].getDisplayMode().getHeight());
        Const.SCREEN_X_SIZE = (int)screenSize.getWidth();
        Const.SCREEN_Y_SIZE = (int)screenSize.getHeight();
        Const.ORIGINAL_SCREEN_X_SIZE = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        Const.ORIGINAL_SCREEN_Y_SIZE = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    }
    
    public void detect() throws Exception {

		Robot robot = new Robot();
		Color color;
		boolean selfDetected = false;
		int rgb[][][]=new int[(int)Const.SCREEN_Y_SIZE][(int)Const.SCREEN_X_SIZE][3];
		BufferedImage img = robot.createScreenCapture(new Rectangle(screenSize));
		// ImageIO.write(img, "PNG", new File(Const.SCREEN_SHOT));//("agar2/screenShot"+count2+".png"));
		// BufferedImage img=ImageIO.read(new File(Const.SCREEN_SHOT));
		
		for(int j=0; j<Const.SCREEN_Y_SIZE; j++)		 
		{
			for(int k=0; k<Const.SCREEN_X_SIZE; k++)
			{
				if(j>=Const.SCREEN_Y_SIZE*(1 - 90.0/1800) && k<=(300.0/3200)*Const.SCREEN_X_SIZE)		// erase score on bottom left corner  300/3200 & 1800 - 90/1800
				{
					rgb[j][k][0]=0;
					rgb[j][k][1]=0;
					rgb[j][k][2]=0;
				}
				else
				{
					color = new Color(img.getRGB(k,j)+Const.OFFSET);
					rgb[j][k][0]=color.getRed();
					rgb[j][k][1]=color.getGreen();
					rgb[j][k][2]=color.getBlue();
				}
			}
		}
		
		for(int j=0; j<Const.SCREEN_Y_SIZE; j+=5)	
		{
			for(int k=0; k<Const.SCREEN_X_SIZE; k+=5)
			{
				if(!selfDetected)
				{				
					// To ensure bot is detected first
					j=Const.SCREEN_Y_SIZE/2;
                    k=Const.SCREEN_X_SIZE/2;
				}
				if(colorCompare(rgb,j,k,Const.WHITE_COLOR))
				{
					int leftEnd, rightEnd, horizontalRadius;
					int upEnd, downEnd, verticalRadius, kPrime;
					int radius, x, y;

					leftEnd = getLeftEnd(rgb, j, k, Const.WHITE_COLOR);
					rightEnd = getRightEnd(rgb, j, k, Const.WHITE_COLOR);
					horizontalRadius = (rightEnd-leftEnd)/2;
					
					kPrime = leftEnd + horizontalRadius;
					upEnd = getUpEnd(rgb, j, kPrime, Const.WHITE_COLOR);
					downEnd = getDownEnd(rgb, j, kPrime, Const.WHITE_COLOR);
					verticalRadius = (downEnd-upEnd)/2;
					
					radius = Math.max(horizontalRadius, verticalRadius);
					x = leftEnd + horizontalRadius;
					y = upEnd + verticalRadius;
										
					eraseCircle(rgb, upEnd, downEnd, kPrime, Const.WHITE_COLOR);	// erase circle
					
					if(radius >= 3)				// Ignore stray dots, add rest to list of circles
					{
						if(circleList.size()!=0 && radius < 10){
							circleList.add( new CircleInfo(x,y,radius,0,'p') );
						}
						else if(circleList.size()!=0)
						{
							double dist=euclidean(circleList.get(0).x,circleList.get(0).y,x,y);
							double w = weightage(radius,circleList.get(0).r,dist,'c');
							circleList.add( new CircleInfo(x,y,radius,w,'c') );
						}
						else
							circleList.add( new CircleInfo(x,y,radius,0,'b') );
                            
                        //System.out.println("X : "+x+" Y : "+y+" R : "+radius);
					}
					else{
						virusDetectorList.add( new CircleInfo(x,y,radius,0,'s') );
                        //System.out.println("------X : "+x+" Y : "+y+" R : "+radius);
					}
				}
				if(!selfDetected)			
				{		
					// Once bot is detected, begin from the top left
					j=0;k=0;
					selfDetected=true;
				}
			}
		}
		
		for (int i = 1; i < circleList.size(); i++) 
		{
			CircleInfo enemy,stray;
			enemy = circleList.get(i);
			if(enemy.type=='c')
			{
				for (int j = 1; j < virusDetectorList.size(); j++) 
				{
					stray = virusDetectorList.get(j);
					double distFactor=euclidean(enemy.x,enemy.y,stray.x,stray.y)-enemy.r;
					if( distFactor>=-10 && distFactor<=10)
						enemy.type='v';
				}
			}
		}
		//To determine Indices of the left most, right most, up most, down most circles
		if(circleList.size()!=0)
		{
			int leftIndex, rightIndex, upIndex, downIndex;	
			CircleInfo bot = circleList.get(0), enemy;
			int leftX = bot.x;
			int rightX = bot.x;
			int upY = bot.y;
			int downY = bot.y;

			for(int i=0; i<circleList.size(); i++){
				enemy = circleList.get(i);

				if(enemy.x < leftX){
					leftX = enemy.x;
					leftIndex = i;
				}
				if(enemy.x > rightX){
					rightX = enemy.x;
					rightIndex = i;
				}
				if(enemy.y < upY){
					upY = enemy.y;
					upIndex = i;
				}
				if(enemy.x > downY){
					downY = enemy.y;
					downIndex = i;
				}
			}

			// add a fake enemy in case bot gets to close a border, as a repulsion
			if( leftX > Const.SCREEN_X_SIZE/4)
				circleList.add(new CircleInfo(0,bot.y,leftX,weightage(2*leftX,bot.r,bot.x,'w'),'w'));
			else if( rightX < 3*Const.SCREEN_X_SIZE/4)
				circleList.add(new CircleInfo(Const.SCREEN_X_SIZE,bot.y,rightX,weightage(2*rightX,bot.r,Const.SCREEN_X_SIZE-bot.x,'w'),'w'));
			else if( upY > (Const.SCREEN_Y_SIZE)/4)
				circleList.add(new CircleInfo(bot.x,0,upY,weightage(2*upY,bot.r,bot.y,'w'),'w'));
			else if( downY < 3*(Const.SCREEN_Y_SIZE)/4)
				circleList.add(new CircleInfo(bot.x,Const.SCREEN_Y_SIZE,downY,weightage(2*downY,bot.r,Const.SCREEN_Y_SIZE-bot.y,'w'),'w'));
		}
	}

	int getLeftEnd(int rgb[][][],int j,int k,int val){
		for(int i=k;;i--){
			if(i==-1 || !colorCompare(rgb,j,i,val)){
				return i+1;
			}
		}
	}
	int getRightEnd(int rgb[][][],int j,int k,int val){
		for(int i=k;;i++){
			if(i==Const.SCREEN_X_SIZE || !colorCompare(rgb,j,i,val)){
				return i-1;
			}
		}
	}
	int getUpEnd(int rgb[][][],int j,int k,int val){
		for(int i=j;;i--){
			if(i==-1 || !colorCompare(rgb,i,k,val)){
				return i+1;
			}
		}
	}
	int getDownEnd(int rgb[][][],int j,int k,int val){
		for(int i=j;;i++){
			if(i==Const.SCREEN_Y_SIZE || !colorCompare(rgb,i,k,val)){
				return i-1;
			}
		}
	}

	boolean colorCompare(int rgb[][][],int j,int k,int val){	// check if color is (nearly) white
		if(rgb[j][k][0]>val && rgb[j][k][1]>val && rgb[j][k][2]>val)
			return true;
		return false;
	}
	void eraseCircle(int rgb[][][],int up,int down,int k,int val){		
		for(int i=up;i<=down;i++){
			for(int l=k;l>-1;l--){
				if(colorCompare(rgb,i,l,val)){
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
				if(colorCompare(rgb,i,l,val)){
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

	double weightage(double r,double r0,double dist,char type){
		dist=Math.max(1,dist-(r+r0));
		double gamma=2;
		double alpha,beta=(r+r0);
		if(type!='v')
		{
			if(r/r0>=5)
				alpha=5;
			else if(r/r0>=2.5)
				alpha=2.5;		// gamma * alpha/sizeRatio * beta/dist
			else if(r/r0>=1.2)	//0.05 buffer instead of 1.25
				alpha=1.25;
			else if(r/r0>0.8)
				alpha=1/2*0.8;
			else if(r/r0>=0.4)
				alpha=1/0.4;
			else
				alpha=1/0.2;
			if(alpha==5)
				return gamma * 0.5*Math.pow(beta/dist,1);	//aplha change to 2.5, r/r0 fixed to 5. So alpha/sizeRatio=2.5/5=0.5;
			else
				return gamma * alpha/(r/r0)*Math.pow(beta/dist,1);
		}
		else
		{
			
			if(r/r0>=1.25)
				alpha=1;
			else if(r/r0>=0.8)
				alpha=0.5;
			else
				alpha=1;
			return gamma * alpha*(r/r0)*Math.pow(beta/dist,1);
		}
	}
	static double euclidean(double x1,double y1,double x2,double y2){
		return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
	}
}