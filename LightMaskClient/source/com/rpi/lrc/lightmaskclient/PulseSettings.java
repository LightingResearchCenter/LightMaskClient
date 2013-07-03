package com.rpi.lrc.lightmaskclient;

import java.awt.Frame;

import processing.core.PApplet;
import controlP5.*;

public class PulseSettings extends PApplet {

	  int w, h;
	  Frame fr;
	  Textfield txt [] = new Textfield [3];
	  
	  public String pulseDur, pulseInt, pulseRep;
	  
	  
	  
	  public void setup() {
	    size(w, h);
	    frameRate(25);
	    cp5 = new ControlP5(this);
	    
	    txt[0] = cp5.addTextfield("Pulse Duration");
	    txt[1] = cp5.addTextfield("Pulse Intensity");
	    txt[2] = cp5.addTextfield("Pulse Repetition Period");
	    for (int i = 0; i < 3; i++) {
	    	txt[i].setWidth(150)
	    		.setPosition(10, 40 * i + 10);
	    }
	    
	    cp5.addButton("ledCheck")
	    	.setPosition(10, 130);
	    cp5.addButton("Submit")
	    	.setPosition(10, 160);
	    cp5.addButton("Cancel")
    		.setPosition(90, 160);
	  }

	  public void draw() {
	      background(100);
	      checkValidValues();
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
	  
	  void ledCheck (int theValue) {
		  if (checkValidValues()) {
			  String tempInt = txt[1].getText();
			  String tempDur = txt[0].getText();
			  parent.testPulseSettings(tempInt, tempDur);
		  }
	  }
	  
	  ControlP5 cp5;

	  LightMaskClient parent;
	  
	  public void controlEvent(ControlEvent theEvent) {
		  if(theEvent.getName() == "Submit") {
			  pulseDur = txt[0].getText();
			  pulseInt = txt[1].getText();
			  pulseRep = txt[2].getText();
			  
			  if (!checkValidValues()) {
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
	  
	  boolean isInRange (int n, int min, int max) {
			return ((n > min) && (n < max));
		}
	  
	  public static boolean isInteger(String s) {
		    try { 
		        Integer.parseInt(s); 
		    }
		    catch(NumberFormatException e) { 
		        return false; 
		    }
		    // only got here if we didn't return false
		    return true;
		}
	  
	  boolean checkValidValues () {
			boolean valid = true;
			for (int i  = 0; i < 3; i++) {
				valid &= isValidValue(txt[i], 0, 9999);
			}
			return valid;
		}

		boolean isValidValue(Textfield t, int min, int max) {
			if(isInteger(t.getText()) && isInRange(Integer.parseInt(t.getText()), min, max)) {
				t.setColorForeground(color(45, 185, 0));
				return true;
			}
			else {
				t.setColorForeground(color(183, 0, 0));
				return false;
			}
		}
	}