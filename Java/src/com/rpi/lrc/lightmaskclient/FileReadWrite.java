package com.rpi.lrc.lightmaskclient;

import java.io.File;
import java.util.Date;

import processing.core.PApplet;

//NOT USED
public class FileReadWrite extends PApplet{
	
	static Date date = new Date("05/15/2012 13:12");

	public String autoget_startfile(String name) {
		File[] roots = File.listRoots(); 
	  
	    //scan all directories for data_log.txt
	    for(int i=0;i<roots.length;i++) {
	    	File f = new File((roots[i] + name));
	      
	    	if(f.exists()) {
	    		//if the file is found, make sure it's the only file in the directory
	    		if(roots[i].listFiles().length == 2) {
	          
	    			//if it is, make sure it was created at the right time (within 27.7 hours)
	    			if(abs(f.lastModified() - date.getTime()) < 100000000) {
	    				return f.getPath();
	    			}
	    		}
	    	}
	    }
	    return "nothing";
	}
	
	public String[] readFile(String file_location){
		return loadStrings(file_location);
	}
}
