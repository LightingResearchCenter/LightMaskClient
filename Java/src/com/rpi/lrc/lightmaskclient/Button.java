package com.rpi.lrc.lightmaskclient;

import controlP5.ControlP5;
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
		ControlP5 overlap;
		
		public Button(){
			recx = 50;
			recy = 50;
			name = "NAME";
		}
	  
		public Button(LightMaskClient lmc, int x, int y, String n, ControlP5 cp5){
		    app = lmc;
			recx = x;
		    recy = y;
		    name = n;
		    overlap = cp5;
		}
	 
		public void display(){
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
	    if(!(overlap.isMouseOver()) && (app.mouseX > (recx)) && (app.mouseX < (recx + recsizex)) && (app.mouseY > (recy)) && (app.mouseY < (recy + recsizey))) {
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