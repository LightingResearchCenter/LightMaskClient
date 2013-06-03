package com.rpi.lrc.lightmaskclient;

import java.awt.Frame;

import processing.core.PApplet;
import controlP5.*;

public class PulseSettings extends PApplet {

	  int w, h;
	  Frame fr;
	  
	  public String pulseDur, pulseInt, pulseRep;
	  
	  
	  
	  public void setup() {
	    size(w, h);
	    frameRate(25);
	    cp5 = new ControlP5(this);
	    
	    cp5.addTextfield("Pulse Duration")
	    	.setWidth(150)
	    	.setPosition(10, 10);
	    cp5.addTextfield("Pulse Intensity")
	    	.setWidth(150)
    		.setPosition(10, 50);
	    cp5.addTextfield("Pulse Repetition Period")
	    	.setWidth(150)
    		.setPosition(10, 90);
	    cp5.addButton("Submit")
	    	.setPosition(10, 140);
	    cp5.addButton("Cancel")
    	.setPosition(90, 140);
	  }

	  public void draw() {
	      background(100);
	  }
	  
	  public PulseSettings(LightMaskClient theParent, Frame f, int theWidth, int theHeight) {
	    parent = theParent;
	    w = theWidth;
	    h = theHeight;
	    fr = f;
	    pulseRep = pulseInt = pulseDur = null;
	  }


	  public ControlP5 control() {
	    return cp5;
	  }
	  
	  
	  ControlP5 cp5;

	  LightMaskClient parent;
	  
	  public void controlEvent(ControlEvent theEvent) {
		  if(theEvent.getName() == "Submit") {
			  pulseDur = cp5.get(Textfield.class, "Pulse Duration").getText();
			  pulseInt = cp5.get(Textfield.class, "Pulse Intensity").getText();
			  pulseRep = cp5.get(Textfield.class, "Pulse Repetition Period").getText();
			  
			  if (!isInteger(pulseDur) || !isInteger(pulseInt) || !isInteger(pulseRep)) {
				  return;
			  }
			  
			  println(pulseDur + "\n" + pulseInt + "\n" + pulseRep);
			  parent.setPulseSettings(pulseDur, pulseInt, pulseRep);
			  fr.dispose();		  
		  }
		  
		  else if(theEvent.getName() == "Cancel") {
			  fr.dispose();
		  }
		}
	  
	  public static boolean isInteger(String s) {
		    try { 
		        Integer.parseInt(s); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    }
		    // only got here if we didn't return false
		    return true;
		}

	  
	}