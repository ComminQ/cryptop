package net.cryptop.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IOUtils {
  
  public static String readStdIn(){
    
    
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      // empty the buffer
      while (!br.ready()) {
      }
      String input = br.readLine();
      return input;
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
  }

  public static int readStdInInt(){
    return Integer.parseInt(readStdIn());
  }



}
