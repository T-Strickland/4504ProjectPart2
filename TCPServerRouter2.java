//################################################################# 
//                      Command Line Args
//  1. TCPClient thread count + 1 <-- Used to assign starting logical address value
//  2. TCPClien2 thread count
//################################################################# 

import java.net.*;
import java.io.*;

public class TCPServerRouter2 {
   public static void main(String[] args) throws IOException {

   Socket         clientSocket      = null;                       //   Socket for the thread
   Object [][]    RoutingTable      = new Object [50][3];         //   Routing table
   Boolean        Running           = true;
   int            SockNum           = 5556;                       //   Port number
   int            routerPort        = 5555;
   int            ind               = 0;                          //   Index in the routing table	
   int            logicalIPCount    = Integer.valueOf(args[0]);   //   Concatenated to the end of the logicalIP  
   int            clientCount       = Integer.valueOf(args[1]);   //   Prevent clients from network 1 being added to the routing table
   String         logicalIP         = "192.0.0.";
   String         routerName        = "ROUTERNAME";          // ServerRouter host name

   //Accepting connections
   ServerSocket serverSocket = null;                            //   Server socket for accepting connections

   try {
      serverSocket = new ServerSocket(SockNum);
      System.out.println("ServerRouter is Listening on port: " + String.valueOf(SockNum));
   }
      catch (IOException e) {
      System.err.println("Could not listen on port: 5555.");
      System.exit(1);
   }  

   // Creating threads with accepted connections
   String logicalAddress;  // FAKE IP to simulate real routing table
   while (Running){  
      try {
        clientSocket = serverSocket.accept();
        logicalAddress = logicalIP + String.valueOf(logicalIPCount);

        SThread t = new SThread(RoutingTable, clientSocket, logicalAddress, ind, routerName, routerPort, clientCount); // creates a thread with a random port
        t.start(); // starts the thread
        if(ind < clientCount)
            System.out.println("ServerRouter connected with Client/Server: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " " + "Logical Address: " + logicalAddress);
        else
            System.out.println("ServerRouter connected with Client/Server: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

        logicalIPCount++;
        ind++; // increments the index
      }
         catch (IOException e) {
         System.err.println("Client/Server failed to connect.");
         System.exit(1);
      }
   }//end while
   
   //closing connections
   clientSocket.close();
   serverSocket.close();
   }
}
