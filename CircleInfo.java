package agar;

class CircleInfo{
		int x;
		int y;
		int r;
		double weightage;
		char type;		//c-cell, v-virus, s-stray virus detectors, w-wall, b-bot, p-pellet

		CircleInfo(int a,int b, int c, double w,char t)
		{
			x = a;
			y = b;
			r = c;
			weightage = w;
			type=t;
		}
	}