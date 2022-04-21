import java.io.*;
import java.net.*;

//################################################################# 
//                Thread client code 
//  SENDER will send DIFFERENT Port numbers to the router for communication
//  RECIEVER will thread to accept connections on the DIFFERENT ports
//################################################################# 

public class TCPClient {
   public static void main(String[] args) throws IOException {
   
   // Variables for setting up connection and communication
   Socket Socket = null; // socket to connect with ServerRouter
   PrintWriter out = null; // for writing to ServerRouter
   BufferedReader in = null; // for reading form ServerRouter
   InetAddress addr = InetAddress.getLocalHost();
   String host = addr.getHostAddress(); // Client machine's IP
   String routerName = "j263-08.cse1.spsu.edu"; // ServerRouter host name
   int SockNum = 5555; // port number
   
   // Tries to connect to the ServerRouter
   try {
      Socket = new Socket(routerName, SockNum);
      out = new PrintWriter(Socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
   } 
   catch (UnknownHostException e) {
      System.err.println("Don't know about router: " + routerName);
      System.exit(1);
   } 
      catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to: " + routerName);
      System.exit(1);
   }

   //################################################################# 
   //                   SENDER CLIENT
   // Ask router if the routing table contains the IP
   // Send Port number on which it wants to establish the connection on 
   // Catch error if the routing table does NOT contain the IP
   // Close connection with router

   // If router returns the IP
   // Establish a connection to the peer on specific port number
   // Set data to be sent
   // Send data
   // Enter communication loop
   // Wait for response and reply if need to
   // Exit communication loop
   // Close sockets
   //################################################################# 

   
   //################################################################# 
   //                      RECIEVER CLIENT (Server)
   // Enter communication loop
   // What for router to send a message that a peer is requesting a connection
   // Exit communication loop

   // Create socket on port number recieved from the router
   // Accept peer connection
   // Enter communication loop
   // Responsed and reply
   // Exit communication loop
   // Close all sockets
   //################################################################# 

   // Variables for message passing	
   Reader reader = new FileReader("file.txt"); 
   BufferedReader fromFile =  new BufferedReader(reader); // reader for the string file
   String fromServer; // messages received from ServerRouter
   String fromUser; // messages sent to ServerRouter
   String address =""; // destination IP (Server)
   long t0, t1, t;
   
   // Communication process (initial sends/receives
   out.println(address);// initial send (IP of the destination Server)
   fromServer = in.readLine();//initial receive from router (verification of connection)
   System.out.println("ServerRouter: " + fromServer);
   out.println(host); // Client sends the IP of its machine as initial send
   t0 = System.currentTimeMillis();
   
   // Communication while loop
   while ((fromServer = in.readLine()) != null) {
      System.out.println("Server: " + fromServer);
      t1 = System.currentTimeMillis();
      if (fromServer.equals("Bye.")) // exit statement
         break;
      t = t1 - t0;
      System.out.println("Cycle time: " + t);
      
      fromUser = fromFile.readLine(); // reading strings from a file
      if (fromUser != null) {
         System.out.println("Client: " + fromUser);
         out.println(fromUser); // sending the strings to the Server via ServerRouter
         t0 = System.currentTimeMillis();
      }
   }
   
   // closing connections
   out.close();
   in.close();
   Socket.close();
   }
}
