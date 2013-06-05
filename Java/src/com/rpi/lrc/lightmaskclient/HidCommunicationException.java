package com.rpi.lrc.lightmaskclient;

//@SuppressWarnings("serial")
public class HidCommunicationException extends Exception {
	public HidCommunicationException(String s) {
		super(s);
		ErrorLog.write("HidCommunicationException" + s);
	}
}