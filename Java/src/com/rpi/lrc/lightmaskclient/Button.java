package com.rpi.lrc.lightmaskclient;

import processing.core.PApplet;

	public class Button extends PApplet{
		int recx;
		int recy;
		int recsizex = 160;
		int recsizey = 100;
		LightMaskClient app;
		//button color
		int r;
		int g;
		int b;
		//stroke color
		int sr;
		int sg;
		int sb;
		String name;
		int reccorners = 8;
		
		public Button(){
			recx = 50;
			recy = 50;
			name = "NAME";
		}
	  
		public Button(LightMaskClient lmc, int x, int y, String n){
		    app = lmc;
			recx = x;
		    recy = y;
		    name = n;
		}
	 
		public void display(){
		   //textFont(f28, 28);
		   app.strokeWeight(5);
		   app.rectMode(CORNER);
		   if (over()) {
			   app.stroke(255, 255, 255);
		   }
		   else {
			   app.stroke(sr, sg, sb);
		   }
		   app.fill(r, g, b);
		   app.rect(recx, recy, recsizex, recsizey, reccorners, reccorners, reccorners, reccorners);
		   app.fill(255);
		   app.textAlign(CENTER);
		   app.text(name, recx + recsizex/2 , recy + recsizey/2 -8);
		} 
	 
	  //true when mouse is over the button
	  public boolean over() {
	    if((app.mouseX > (recx)) && (app.mouseX < (recx + recsizex)) && (app.mouseY > (recy)) && (app.mouseY < (recy + recsizey))) {
	      return true;
	    }
	    else {
	      return false;
	    }
	  }
	  
	  public void setText(String input){
	    name = input;
	  }
	  
	  public void setRecColor(int rcolor , int gcolor, int bcolor){
	    r = rcolor;
	    g = gcolor;
	    b = bcolor;
	  }
	  
	  public void setStrokeColor(int rcolor, int gcolor, int bcolor){
	    sr = rcolor;
	    sg = gcolor;
	    sb = bcolor;
	  }
	  
	  public void setLocation(int x, int y) {
	    recx = x;
	    recy = y;
	  
	  }
	}