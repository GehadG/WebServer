/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

/**
 *
 * @author Gehad
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest implements Runnable {
 
 final static String CRLF = "\r\n";//For convenience
 Socket socket;
 public HttpRequest(Socket socket) throws Exception
 {
  this.socket = socket;
 }
 public void run()
 {
  try {
   processRequest();
  } catch (Exception e) {
   System.out.println(e);
  }
 }
 
 private void processRequest() throws Exception
 {
  InputStream is = socket.getInputStream();
  DataOutputStream os = new DataOutputStream(
    socket.getOutputStream());
  BufferedReader br = new BufferedReader(
    new InputStreamReader(is));
   String requestLine = br.readLine();
   StringTokenizer tokens = new StringTokenizer(requestLine);
   String reqType = tokens.nextToken();
   if(reqType.equalsIgnoreCase("GET"))
   {
       reqGET(tokens,os);
   }
   else if(reqType.equalsIgnoreCase("POST"))
   {
       reqPOST(tokens,os);
   }
   else
   {
       String statusLine = "HTTP/1.1 404 Not Found: ";
   String contentTypeLine = "Content-Type: text/html" + CRLF;
    String entityBody = "<HTML> <HEAD><TITLE>Not Found</TITLE></HEAD> <BODY>ERROR 404: <br> File Not Found</BODY></HTML>";
  
 
   os.writeBytes(statusLine);
   os.writeBytes(contentTypeLine);
   os.writeBytes(CRLF);
     os.writeBytes(entityBody);
     os.close();
   }
    
    
  }
 private void reqGET(StringTokenizer tokens, DataOutputStream os) throws IOException, Exception{
     String fileName = tokens.nextToken();
    fileName = "." + fileName;
     System.out.println(fileName);
     fileName=fileName.replaceAll("%20", " ");
   FileInputStream fis = null;
   boolean fileExists = true;
   try {
    fis = new FileInputStream(fileName);
   } catch (FileNotFoundException e) {
    fileExists = false;
   }   

   String statusLine = null; 
   String contentTypeLine = null;
   String entityBody = null;
   if (fileExists) {
   statusLine = "HTTP/1.1 200 OK: ";
   contentTypeLine = "Content-Type: " +
    contentType(fileName) + CRLF;
   } else {
   statusLine = "HTTP/1.1 404 Not Found: ";
   contentTypeLine = "Content-Type: text/html" + CRLF;
    entityBody = "<HTML> <HEAD><TITLE>Not Found</TITLE></HEAD> <BODY>ERROR 404: <br> File Not Found</BODY></HTML>";
   }
     System.out.println(contentTypeLine);
   os.writeBytes(statusLine);
   os.writeBytes(contentTypeLine);
   os.writeBytes(CRLF);
   if (fileExists) {
    sendBytes(fis, os);
    fis.close();
   } else {
    os.writeBytes(entityBody);
   }
   os.close();
   socket.close();
 }

private void sendBytes(FileInputStream fis, OutputStream os)
throws Exception
{
   byte[] buffer = new byte[1024];
   int bytes = 0;
   while((bytes = fis.read(buffer)) != -1 ) {
      os.write(buffer, 0, bytes);
   }
}
private static String contentType(String fileName)
 {
 if(fileName.endsWith(".htm") || fileName.endsWith(".html"))
  return "text/html";
 if(fileName.endsWith(".jpg"))
  return "text/jpg";
 if(fileName.endsWith(".gif"))
  return "text/gif";
 if(fileName.endsWith(".css"))
  return "text/css";
 if(fileName.endsWith(".js"))
  return "text/js";
 return "text/html";
 }

    private void reqPOST(StringTokenizer tokens, DataOutputStream os) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
