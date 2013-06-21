package com.rpi.lrc.lightmaskclient;

import controlP5.*;
import processing.core.PApplet;
import processing.core.PFont;

public class DropdownMenu extends PApplet{
	
	ControlP5 cp5;
	public DropdownList researchTools;
	LightMaskClient app;

	public DropdownMenu () {}
	
	public DropdownMenu (LightMaskClient p) {  
		app = p;
		PFont f13 = createFont("Calibri", 13);
		cp5 = new ControlP5(app, f13);
		researchTools = cp5.addDropdownList("Research Menu", 0, 16, 400, 15);
		customize(researchTools);
		researchTools.hide();
	}

	//Adjust how the menu will look
	public void customize(DropdownList ddl) {
		ddl.setBarHeight(15);
		ddl.setItemHeight(15);
		ddl.setHeight(10*15);
		
		ddl.setCaptionLabel("Research Menu");
		ddl.addItem("Load File", 1);
		ddl.addItem("Check Mask Schedule", 2);
		ddl.addItem("Check Clock", 3);
		ddl.addItem("Set Clock", 4);
		ddl.addItem("Check Pulse Settings", 5);
		ddl.addItem("Set Pulse Settings", 6);
		ddl.addItem("Set Initial Run Values", 7);
		ddl.addItem("Complete Setup", 8);
		ddl.addItem("Toggle availability window dialog", 9);
		
		ddl.setColorBackground(color(200));
		ddl.setColorActive(color(200));

	}

}
