package com.rpi.lrc.lightmaskclient;

import java.text.DecimalFormat;
import java.util.Calendar;

import com.ti.msp430.usb.hiddemo.management.HidCommunicationManager;
import com.ti.msp430.usb.hiddemo.management.HidCommunicationManager.HidCommunicationException;

public class LightMaskManager implements DataReceivedActionListener {

	private HidCommunicationManager hMan;
	private HidDataReceiveThread readThread;
	@SuppressWarnings("unused")
	private int[] interfaces;
	private String[] serials;
	int inf;
	int vid;
	int pid;
	String[] lightOnTimes;
	String[] lightOffTimes;
	
	
	
	public LightMaskManager(){
		interfaces=new int[0];
		serials=new String[0];
		//serial = "890188460F001A00";
		inf = -1;
		hMan = new HidCommunicationManager();
		vid = getFormattedVid();
		pid = getFormattedPid();
		lightOnTimes = new String[7];
		lightOffTimes = new String[7];
		
	}
	
	//Look for device with VID and PID
	//Only one program can be connected to the LightMask at a time
	public boolean find_mask() {
		
		try {
			/* Calling our native functions and processing the results */
			interfaces = hMan.getInterfacesForVidPid(vid, pid);
		} catch (Exception e) {
			e.printStackTrace();
			LightMaskClient.setMainText("Alert: getInterfacesForVidPid error");
			ErrorLog.write("Alert: getInterfacesForVidPid error");
			return false;
		}
		
		try {
			/* Calling our native functions and processing the results */
			serials = hMan.getSerialsForVidPid(vid, pid);
		} catch (Exception e) {
			e.printStackTrace();
			LightMaskClient.setMainText("Alert: getSerialsForVidPid error");
			ErrorLog.write("Alert: getInterfacesForVidPid error");
			return false;
		}
		
		if (serials.length > 0){
			try {
				//inf = Integer.parseInt("" + temp.charAt(temp.length() - 1));
				hMan.connectDevice(vid, pid, serials[0], inf);
			} catch (final com.ti.msp430.usb.hiddemo.management.HidCommunicationManager.HidCommunicationException e) {
				e.printStackTrace();
				LightMaskClient.setMainText("Alert: connectDevice error");
				ErrorLog.write("Alert: connectDevice error");
				return false;
			}
			readThread = new HidDataReceiveThread(hMan);
			readThread.setListener(this);
			readThread.start();
				
			return true;
		}
		else{
			return false;
		}
	}
	//convert PID to hex
	private int getFormattedPid() {
		String pid = "0x0301";
		return Integer.parseInt(pid.replace("0x", ""), 16);
	}
	//Convert VID to hex
	private int getFormattedVid() {
		String vid = "0x2047";
		return Integer.parseInt(vid.replace("0x", ""), 16);
	}
	
	//return formatted MaskSchedule
	public String getMaskSchedule(int day){
		if (lightOnTimes[day] == null && lightOffTimes[day] == null)
			return "";
		else
			return "        " + lightOnTimes[day] + "                     " + lightOffTimes[day] + '\n';
	}
	public void clearTimes(){
		lightOnTimes = new String[7];
		lightOffTimes = new String[7];
	}
	
	//Send a string to the MSP430 microcontroller through the HID datapipe
	public void sendCommand(String command){
		try{
			hMan.sendData(command);
		} catch (final HidCommunicationException e) {
			System.out.println(e);
			LightMaskClient.setMainText("Unable to send buffer!");
			ErrorLog.write("Unable to send buffer");
		}
	}
	
	//given the array of Calendar objects send the on and off times to the MSP430
	public void sendTimes(Calendar[] onT, Calendar[] offT){
		String formatedTimes;
		DecimalFormat formatter = new DecimalFormat("00");
		clearTimes();
		for (int i =0; i< 7; i++){
			if (onT[i] != null){  //If valid calendar on times
				formatedTimes = "on_Times:"+i+","+onT[i].get(Calendar.YEAR)+","+formatter.format(onT[i].get(Calendar.MONTH) + 1)+","+formatter.format(onT[i].get(Calendar.DAY_OF_MONTH))+","+formatter.format(onT[i].get(Calendar.HOUR_OF_DAY))+","+formatter.format(onT[i].get(Calendar.MINUTE))+ "!";
				sendCommand(formatedTimes);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					ErrorLog.write(e.getMessage());
					e.printStackTrace();
				}
			}
			if (offT[i] != null){  //If valid calendar off times
				formatedTimes = "offTimes:"+i+","+offT[i].get(Calendar.YEAR)+","+formatter.format(offT[i].get(Calendar.MONTH))+","+formatter.format(offT[i].get(Calendar.DAY_OF_MONTH))+","+formatter.format(offT[i].get(Calendar.HOUR_OF_DAY))+","+formatter.format(offT[i].get(Calendar.MINUTE))+ "!";
				sendCommand(formatedTimes);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					ErrorLog.write(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		//LightMaskClient.checkSchedule();	
	}
	
	//parses on and off time responses from the MSP430
	public void fireStringReceivedEvent(String s) {
		if (!s.equals("")) {
			if (s.contains("On time")){
				int index = Integer.parseInt(s.substring(10, 11));
				lightOnTimes[index] = s.substring(13);
			}
			else if (s.contains("Off time")){
				int index = Integer.parseInt(s.substring(11, 12));
				lightOffTimes[index] = s.substring(14);
			}
			else if (s.contains("offTime")) {
				MatlabODESolver.waitIncrement();
			}
			else {
				LightMaskClient.appendMainText(s);
			}
		}
	}

	public void fireUnableToReadEvent() {
		LightMaskClient.setMainText("Light Mask disconnected.");
		LightMaskClient.maskConnected = false;			
	}
	
}
