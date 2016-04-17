package agar;

public class test{

	public static void main(String args[]) throws Exception {
		Detection d = new Detection();
		d.detect();

		for(CircleInfo c : d.circleList)
			System.out.println("X : "+c.x+" Y : "+c.y+" R : "+c.r);
	}
}