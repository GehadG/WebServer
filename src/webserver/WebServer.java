/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Gehad
 */
public class WebServer {

    /**
     * @param args the command line arguments
     */

     public static void main(String args[]) 
   throws Exception {
         int port=80;
      ServerSocket ssock = new ServerSocket(port);
      System.out.println("Listening to port :"+port);
      while (true) {
         Socket sock = ssock.accept();
     new Thread(new HttpRequest(sock)).start();
         
      }
   }
    
    /*public static void main(String[] args) throws IOException {
        final ServerSocket socket = new ServerSocket(8081);
        final Socket accept = socket.accept();
        final InputStream inputStream = accept.getInputStream();
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        char readChar;
        while ((readChar = (char) inputStreamReader.read()) != -1) {
            System.out.print(readChar);
        }
        inputStream.close();
        accept.close();
        System.exit(1);
    }*/
   
}
    

