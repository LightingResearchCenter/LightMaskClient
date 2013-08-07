package com.rpi.lrc.lightmaskclient;

import processing.core.PApplet;

public class MakeList extends PApplet{
  public static String[] MakeList(int[] buffer){
    String ret_list[] = new String[16];
    int n = 0;
    String line = new String();
    for(int i = 0;i < buffer.length;i++){
      char item = (char)buffer[i];
      if (item == '*'){
        return ret_list;
      }
      else if (item == '\n'){
        ret_list[n] = line;
        line = new String();
        n++;
      }
      else {
        line += item;
      }
    }
    return ret_list;
  }
}