//################################################################# 
//                      Command Line Args
//  1. Number of "clients" or threads
//  2. Name of file to test WITH extension
//################################################################# 

//################################################################# 
//                   Statistics To Track
// 1. Average message sizes
// 2. Average transmission time
// 3. Average time for the routing-table lookup
// 4. Number of bytes/units of data transferred per unit time 
//################################################################# 

import java.io.*;
import java.net.*;

public class TCPClient {
   public static void main(String[] args) throws IOException {
   
      // Variables for setting up connection and communication
      String routerName       = "ROUTERNAME";             // ServerRouter host name
      String peerAddress      = "192.0.0.";                    // FAKE Logical peer address (USED IN ROUTING TABLE)
      String realAddress      = "IPADDRESS";  
      String inputFile        = args[1];

      int numOfThreads        = Integer.valueOf(args[0]);     
      int routerPort          = 5555;                          // Router (1) port number
      int peerAddressCount    = numOfThreads + 1;              // All peers under Router (1) are "Servers/Senders"
      int threadId            = 0;  
      int tempPortNumber      = 49000;                         // Starting port # for P2P connections (Incremented by 1)      

      Socket s                = null;
      PrintWriter out         = null;
      BufferedReader in       = null;

      for(int x = 0; x < numOfThreads; x++)
      {
         // Tries to connect to the ServerRouter
         try {
           s = new Socket(routerName, routerPort);
           out = new PrintWriter(s.getOutputStream(), true);
           in = new BufferedReader(new InputStreamReader(s.getInputStream()));
           
           // Spawn thread for P2P connection
           TCPClientThread t = new TCPClientThread(s, out, in, (peerAddress + String.valueOf(peerAddressCount)), realAddress, threadId, tempPortNumber, inputFile);
           t.start();
           peerAddressCount++;
           threadId++;
           tempPortNumber++;
         } 
         catch (UnknownHostException e) {
            System.out.println("Don't know about router: " + routerName);
            System.exit(1);
         } 
            catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
         }
      }

   }
}

class TCPClientThread extends Thread{

   // Variables for setting up connection and communication
   private Socket          Socket         = null;             // Socket to connect with ServerRouter
   private PrintWriter     out            = null;             // For WRITING to ServerRouter
   private BufferedReader  in             = null;             // For READING from ServerRouter
   private String          peerAddress    = ""; 
   private String          realAddress    = "";
   private String          inputFile      = "";
   private int             id;
   private int             tempPortNumer;
   
   TCPClientThread(Socket Socket, PrintWriter out, BufferedReader in, String peerAddress, String realAddress, int id, int tempPortNumber, String inputFile)
   {
      this.Socket          = Socket;
      this.out             = out;
      this.in              = in;
      this.peerAddress     = peerAddress;
      this.realAddress     = realAddress;
      this.id              = id;
      this.tempPortNumer   = tempPortNumber;
      this.inputFile       = inputFile;
   }

   public void run() 
   {
      //################################################################# 
      //                   SENDER CLIENT
      //################################################################# 
      Socket peer = null;
      try
      {
         long t1;
         long t2 = 0;
         int msgCount = 0;
         int messageSize = 0;

         // Reservers open port number for P2P connection
         //ServerSocket temp = new ServerSocket(0);
         //int port = temp.getLocalPort();
         // Send router peer info
         // Ask router if the routing table contains the IP
         // Send Port number on which it wants to establish the connection on 
         t1 = System.currentTimeMillis();
         //out.println(peerAddress + ":" + port + ":Find");
         out.println(peerAddress + ":" + tempPortNumer + ":Find");
         // Read reply from router
         String fromRouter = in.readLine();
         t1 = System.currentTimeMillis() - t1;
         //System.out.println("Thread " + String.valueOf(id) + " Routing Table Lookup Time = " + String.valueOf(t1) + "ms");

         // Close connection with router
         Socket.close();
         // Terminate thread if peer is NOT found
         if(fromRouter.equals("404"))
         {
            System.out.println("Thread " + String.valueOf(id) + " Router could not find : " + peerAddress);
            //temp.close();
            return;
         }
         System.out.println("Thread " + String.valueOf(id) + " Router found " + fromRouter + " Closing router socket...");
        
         // Establish a connection to the peer on specific port number
         System.out.println("Thread " + String.valueOf(id) + " Connecting to peer...");
         //temp.close();
         //peer = new Socket(realAddress, port);
         peer = new Socket(realAddress, tempPortNumer);
         // Set data to be sent
         String [] temp = inputFile.split("\\.");
         if(temp[1].matches("wav|mp4"))
         {
            OutputStream peerOut = peer.getOutputStream();
            InputStream peerIn = peer.getInputStream();
            FileInputStream inAudio = new FileInputStream(inputFile);
            File outFile = new File(temp[0] + id + "." + temp[1]);
            outFile.createNewFile();
            FileOutputStream outAudio = new FileOutputStream(outFile.getName());
            byte buffer[] = new byte[2048];
            byte buffer2[] = new byte[2048];
            int count;
            int totalBytes = 0;
            //System.out.println("Sending video bytes");
            while((count = inAudio.read(buffer)) != -1)
            {
               t1 = System.nanoTime();
               peerOut.write(buffer, 0, count);
               peerIn.read(buffer2);
               
               t2 += System.nanoTime() - t1;
               outAudio.write(buffer2);
               totalBytes += 2048;
               msgCount++;
            }
            System.out.println(totalBytes + " bytes sent/recieved");
            System.out.println("Thread " + String.valueOf(id) + " Average Message Transmission Time = " + (String.valueOf(t2 / msgCount))  + "ns");
            System.out.println("Thread " + String.valueOf(id) + " Average Message Size = " + (String.valueOf(totalBytes/ msgCount))  + "bytes");
            inAudio.close();
            outAudio.close();
         }
         else
         {
            PrintWriter peerOut = new PrintWriter(peer.getOutputStream(), true);
            BufferedReader peerIn = new BufferedReader(new InputStreamReader(peer.getInputStream()));
            Reader reader = new FileReader(inputFile); 
            BufferedReader fromFile =  new BufferedReader(reader);
            //Enter communication loop
            // Wait for response and reply if need to
            String fromPeer = "";
            String toPeer = fromFile.readLine();
            peerOut.println(toPeer);
            t1 = System.currentTimeMillis();
            //System.out.println("Thread " + String.valueOf(id) + " YOU: " + toPeer);
            while((fromPeer = peerIn.readLine()) != null)
            {
               //System.out.println("Thread " + String.valueOf(id) + " PEER: " + fromPeer);
               // Send data
               toPeer = fromFile.readLine();
               if(toPeer != null)
               {
                  messageSize += toPeer.length();
               }
               t2 += System.currentTimeMillis() - t1;
               //System.out.println("Thread " + String.valueOf(id) + " Message Transmission Time = " + (String.valueOf(System.currentTimeMillis() - t1))  + "ms");
               if (toPeer != null)
               {
                  //System.out.println("Thread " + String.valueOf(id) + " YOU: " + toPeer);
                  peerOut.println(toPeer);
                  t1 = System.currentTimeMillis();
               }
               msgCount++;
               // Exit communication loop
               if(fromPeer.equals("\\\\EOF")) { break; } 
            }
            System.out.println("Thread " + String.valueOf(id) + " Average Message Transmission Time = " + (String.valueOf(t2 / msgCount))  + "ms");
            System.out.println("Thread " + String.valueOf(id) + " Average Message Size = " + (String.valueOf(messageSize / msgCount))  + "bytes");
            fromFile.close();
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
