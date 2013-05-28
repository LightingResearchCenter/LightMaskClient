package com.rpi.lrc.lightmaskclient;

import processing.core.PApplet;
import processing.core.PFont;

class StatusBar extends PApplet{  
	
	//Main Class in charge of the drawing the GUI
	LightMaskClient app;
	
	PFont f20;
	boolean connected;
	String mtext;
	String statusText;
	int location;
	int length;
	int textLocation;
	  
	public StatusBar(LightMaskClient lmc, String title, int loc, int len){
		app = lmc;
		f20 = loadFont("Calibri-20.vlw");
		mtext = title;
	    statusText = title;
	    location = loc;
	    length = len;
	    connected = false;
	    textLocation = loc + 80;
	}

	void display(){
	    app.fill(200);
	    app.strokeWeight(1);
	    app.stroke(200);
	    app.rectMode(CORNER);
	    app.rect(location, 0, length, 31, 0, 0, 5, 5);
	    app.fill(0);
	    app.textFont(f20, 20);
	    app.text(statusText, textLocation, 22);
	    app.ellipseMode(CENTER);
	    if (connected) {
	    	app.fill(0, 255, 0);
	    	app.stroke(0, 255, 0);
	    }
	    else {
	    	app.fill(255, 0, 0);
	    	app.stroke(255, 0, 0);
	    }
	    app.ellipse(location+length-25, 15, 20, 20);
	}
	
	void setText(String tex, int location){
		statusText = tex;
	    textLocation = location;    
	}
	
	void setStatus(boolean status){
	    connected = status;
	}	  
}