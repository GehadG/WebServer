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
import com.sun.xml.internal.ws.util.StringUtils;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.*;
import javax.imageio.ImageIO;

public class HttpRequest implements Runnable {

    final static String CRLF = "\r\n";
    Socket socket;

    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void processRequest() throws Exception {
        writeImage("fdfd.jpg", null, CRLF, null);
        
        InputStream is = socket.getInputStream();

        DataOutputStream os = new DataOutputStream(
                socket.getOutputStream());
        BufferedReader br = new BufferedReader(
                new InputStreamReader(is));
        String requestLine = br.readLine();

        StringTokenizer tokens = new StringTokenizer(requestLine);
        String reqType = tokens.nextToken();

        if (reqType.equalsIgnoreCase("GET")) {
            reqGET(tokens, os);
        } else if (reqType.equalsIgnoreCase("POST")) {
            String line;

            reqPOST(br, os);

        } else {
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

    private void reqGET(StringTokenizer tokens, DataOutputStream os) throws IOException, Exception {
        String fileName = tokens.nextToken();
        fileName = "." + fileName;
        System.out.println(fileName);
        fileName = fileName.replaceAll("%20", " ");
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
            contentTypeLine = "Content-Type: "
                    + contentType(fileName) + CRLF;
        } else {
            statusLine = "HTTP/1.1 404 Not Found: ";
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML> <HEAD><TITLE>Not Found</TITLE></HEAD> <BODY>ERROR 404: <br> File Not Found</BODY></HTML>";
        }

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
            throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".jpg")) {
            return "text/jpg";
        }
        if (fileName.endsWith(".gif")) {
            return "text/gif";
        }
        if (fileName.endsWith(".css")) {
            return "text/css";
        }
        if (fileName.endsWith(".js")) {
            return "text/js";
        }
        return "text/html";
    }

    private void reqPOST(BufferedReader br, DataOutputStream os) throws IOException {
        String requestLine;
        String boundary = "----ghjkhfhgh";
        String fileName = "";
        String contType = "";

        while ((requestLine = br.readLine()) != null) {
            if (requestLine.contains("boundary=")) {
                contType = requestLine.substring(requestLine.indexOf("Content-Type: ") + "Content-Type: ".length(), requestLine.indexOf(";"));
                boundary = requestLine.substring(requestLine.indexOf("boundary=") + "boundary=".length());
            }
            if (requestLine.contains(boundary)) {
                if (!requestLine.endsWith("--")) {
                    requestLine = br.readLine();
                    fileName = requestLine.substring(requestLine.indexOf("filename=\"") + "filename=\"".length(), requestLine.length() - 1);
                    contType = br.readLine();
                    br.readLine();
                    if (contType.contains("text")) {
                        writeText(fileName, br, boundary, os);
                        System.out.println("DONE!");
                        break;
                    } else if (contType.contains("image")) {

                        writeImage(fileName, br, boundary, os);
                        break;
                    }
                } else {
                    break;
                }
            }

        }
    }

    private void writeText(String fileName, BufferedReader br, String boundary, DataOutputStream os) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
        String l;
        while ((l = br.readLine()) != null) {
            if (!l.contains(boundary)) {
                bw.write(l);
                bw.newLine();
            } else {
                respond(true, os);
                bw.close();
                return;
            }
        }

    }

    private void respond(boolean isOk, DataOutputStream os) throws IOException {
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (isOk) {
            statusLine = "HTTP/1.1 200 OK: ";
            contentTypeLine = CRLF;
        } else {
            statusLine = "HTTP/1.1 404 Not Found: ";
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML> <HEAD><TITLE>Not Found</TITLE></HEAD> <BODY>ERROR 404: <br> File Not Supported</BODY></HTML>";
        }

        os.writeBytes(statusLine);
        os.writeBytes(contentTypeLine);
        os.writeBytes(CRLF);
        os.close();

    }

    private void writeImage(String fileName, BufferedReader br, String boundary, DataOutputStream os) throws IOException {
        /*
         String blabla;
         String bytesImage="";
         while((blabla=br.readLine())!=null)
         {if(!blabla.contains(boundary))
         bytesImage+=blabla+CRLF;
    
         }

         System.out.println(bytesImage);
         byte[] bytes = bytesImage.getBytes();
         FileOutputStream fos = new FileOutputStream(fileName);
         try {
         fos.write(bytes);
         }
         finally {
         fos.close();
         }*/

        final InputStream inputStream = socket.getInputStream();

// Header end flag.
        boolean headerEnded = false;
        FileOutputStream foutStream = new FileOutputStream(fileName);
        byte[] bytes = new byte[2048*4];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            // If the end of the header had already been reached, write the bytes to the file as normal.
            if (headerEnded) {
                
                System.out.println("HEADER FINALLY DONE!");
                foutStream.write(bytes, 0, length);
            } // This locates the end of the header by comparing the current byte as well as the next 3 bytes
            // with the HTTP header end "\r\n\r\n" (which in integer representation would be 13 10 13 10).
            // If the end of the header is reached, the flag is set to true and the remaining data in the
            // currently buffered byte array is written into the file.
            else {
                for (int i = 0; i < 2045; i++) {
                    if (bytes[i] == 13 && bytes[i + 1] == 10 && bytes[i + 2] == 13 && bytes[i + 3] == 10) {
                        headerEnded = true;
                        foutStream.write(bytes, i + 4, 2048 - i - 4);
                        break;
                    }
                }
            }
        }
        System.out.println("DONE!");
        inputStream.close();
        foutStream.close();
    }

}
