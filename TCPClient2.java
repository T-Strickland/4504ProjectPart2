//################################################################# 
//                      Command Line Args
//  1. Number of "clients" or threads
//  2. "txt" if you are sending a txt file otherwise anything else 
//################################################################# 

import java.io.*;
import java.net.*;

public class TCPClient2 {
   public static void main(String[] args) throws IOException {

      // Variables for setting up connection and communication
      Socket socket           = null;                       // socket to connect with ServerRouter
      PrintWriter out         = null;                       // for writing to ServerRouter
      BufferedReader in       = null;                       // for reading form ServerRouter
      String routerName       = "ROUTERNAME";          // ServerRouter host name
      String fileType         = args[1];
      int SockNum             = 5556;                       // Router (2) port number
      int numOfThreads        = Integer.valueOf(args[0]);
      int threadId            = 0;
      
      for(int x = 0; x < numOfThreads; x++){
         // Tries to connect to the ServerRouter
         try {
            socket = new Socket(routerName, SockNum);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Spawn thread for P2P
            TCPClient2Thread t = new TCPClient2Thread(socket, out, in, threadId, fileType);
            t.start();
            threadId++;
         } 
         catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
         } 
            catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
         }
      }
   }
}

class TCPClient2Thread extends Thread{

   Socket         socket   = null;     // socket to connect with ServerRouter
   PrintWriter    out      = null;     // for writing to ServerRouter
   BufferedReader in       = null;     // for reading form ServerRouter
   String         fileType;
   int            id;

   TCPClient2Thread(Socket socket, PrintWriter out, BufferedReader in, int id, String fileType){
      this.socket    = socket;
      this.out       = out;
      this.in        = in;
      this.id        = id;
      this.fileType  = fileType;
   }

   public void run(){
      //################################################################# 
      //                      RECIEVER CLIENT (Server)
      //################################################################# 
      Socket peer = null;
      try{
         // Wait for communication from router 
         System.out.println("Thread " + String.valueOf(id) + " Waiting for communication from router...");
         String[] peerInfo = in.readLine().split(":");
         System.out.println("Thread " + String.valueOf(id) + " " + peerInfo[0] + ":" + peerInfo[1] + " <-- Wants to connect to YOU");
         // Close router connection
         out.close();
         in.close();
         socket.close();
         // Create socket on port number recieved from the router
         System.out.println("Thread " + String.valueOf(id) + " Listening on port #" + peerInfo[1]);
         ServerSocket peerServer = new ServerSocket(Integer.parseInt(peerInfo[1]));
         // Accept peer connection
         peer = peerServer.accept();
         System.out.println("Thread " + String.valueOf(id) + " Peer connected.  Closing server socket...");
         peerServer.close();

         // Enter communication loop
         if(!fileType.equals("txt"))
         {
            InputStream peerIn = peer.getInputStream();
            OutputStream peerOut =  peer.getOutputStream();
            byte[] buffer = new byte[2048];
            int totalBytes = 0;
            while(peerIn.read(buffer) != -1)
            {
               peerOut.write(buffer);
               totalBytes += 2048;
            }
            System.out.println(totalBytes + " Bytes sent/recieved");
         }
         else
         {
            BufferedReader peerIn = new BufferedReader(new InputStreamReader(peer.getInputStream()));;
            PrintWriter peerOut = new PrintWriter(peer.getOutputStream(), true);
            // Responsed and reply
            String fromPeer = "";
            String toPeer = "";
            while((fromPeer = peerIn.readLine()) != null)
            {
               System.out.println("Thread " + String.valueOf(id) + " PEER: " + fromPeer);
               // Send data
               toPeer = fromPeer.toUpperCase();
               if (toPeer != null)
               {
                  System.out.println("Thread " + String.valueOf(id) + " YOU: " + toPeer);
                  peerOut.println(toPeer);
               }
               // Exit communication loop
               if(fromPeer.equals("\\\\EOF")) { break; } 
            }
         }
         // Close all sockets
         peer.close();
      }
      catch (UnknownHostException e) {
         System.out.println(e);
         System.exit(1);
         
      } 
      catch (IOException e) {
         System.out.println(e);
         System.exit(1);
      }
      catch (Exception e)
      {
         System.out.println(e);
      }
   }
}
