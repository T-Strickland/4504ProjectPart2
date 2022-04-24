//################################################################# 
//  Maybe clear routing table after peer connection is finished
//	only is we want run multiple tests without restarting router
//################################################################# 

import java.io.*;
import java.net.*;

public class SThread extends Thread 
{
	private Object [][] RTable; // routing table
	private PrintWriter out, outTo; // writers (for writing back to the machine and to destination)
    private BufferedReader in; // reader (for reading from the machine connected to)
	private String inputLine, outputLine, addr; // communication strings
	private String [] destination;
	private Socket outSocket; // socket for communicating with a destination
	private Socket router;
	private int ind;  // index in the routing table
	private int senderPort; 

	private Socket routerSocket;
	private int routerPort;
	private String routerName;
	private PrintWriter routerOut;
	private BufferedReader routerIn;
	private int clientCount;

	// Constructor
	SThread(Object [][] Table, Socket toClient, String logicalAddress, int index, String routerName, int routerPort, int clientCount) throws IOException
	{
		out = new PrintWriter(toClient.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
		RTable = Table;
		addr = toClient.getInetAddress().getHostAddress();
		senderPort = toClient.getPort();

		this.clientCount = clientCount;
		this.routerPort = routerPort;
		this.routerName = routerName;
		//################################################################# 
		//				CHANGE TO PORT IP AND PORT NUMBER
		//################################################################# 
		boolean found = false;
		ind = index;
		for(int x = 0; x < RTable.length; x++){
			if(String.valueOf(RTable[x][0]).equals(logicalAddress)){
				found = true;
				break;
			}
		}
		if(!found && ind < clientCount){
			RTable[ind][0] = logicalAddress;
			//RTable[index][0] = addr; // IP addresses 
			RTable[ind][1] = senderPort; 
			RTable[ind][2] = toClient; // sockets for communication
		}
	}
	// Run method (will run for each machine that connects to the ServerRouter)
	public void run(){
		try{
			// Initial sends/receives
			String temp = in.readLine();
			if(temp == null) { return; }		
			destination = temp.split(":"); // initial read (the destination for writing)
			//System.out.println("Forwarding to " + destination);
			//out.println("Connected to the router."); // confirmation of connection
			
			// waits 10 seconds to let the routing table fill with all machines' information
			if(ind < clientCount)
			{
				try{ 
					Thread.currentThread().sleep(10000); 
				}
				catch(InterruptedException ie){
					System.out.println("Thread interrupted");
				}
			}
			
			System.out.println("Thread " + String.valueOf(ind) + " " + addr + ":" + senderPort + " is looking up: " + destination[0]);
			// loops through the routing table to find the destination
			for (int i = 0; i < RTable.length; i++) 
			{
				if(RTable[i][0] == null) { break; }
				//System.out.println("RTable Thread " + String.valueOf(ind) + ": " + RTable[i][0] + " " + RTable[i][1]);
				//	If the destination is found in THIS router
				if (destination[0].equals((String)RTable[i][0]) && (int)RTable[i][1] != senderPort)
				{
					//	Respond with IP to the requester
					//	MODIFICATION: RESPOND WITH FOUND CODE INSTEAD
					out.println(RTable[i][0]);
					//	Forward requester IP and Port number to the reciever so they can open a socket
					outSocket = (Socket) RTable[i][2]; 	// gets the socket for communication from the table
					//System.out.println("Found destination: " + destination);
					System.out.println("Thread " +  String.valueOf(ind) + " Found destination: " + RTable[i][0] + ":" + RTable[i][1] + " " + "Forwarding info...");
					outTo = new PrintWriter(outSocket.getOutputStream(), true); 	// assigns a writer
					outTo.println(addr + ":" + destination[1]);
					//outSocket.close();
					return;
				}
			}
			//################################################################# 
			//	If the destination is NOT found in this router
			//	Establish connection with router 2 and ask if it contains the IP
			//	RQ <-- Request added to ensure that the router do not get into a 
			//		   infinite loop of asking each other if they have an IP
			if(!destination[2].equals("RQ"))
			{
				routerSocket = new Socket(routerName, routerPort);
				routerOut = new PrintWriter(routerSocket.getOutputStream(), true);
				routerIn = new BufferedReader(new InputStreamReader(routerSocket.getInputStream()));

				System.out.println("Thread " +  String.valueOf(ind) + " Consulting other router");
				routerOut.println(destination[0] + ":" + destination[1] + ":RQ" );

				System.out.println("Thread " +  String.valueOf(ind) + " Sent to other router");
				temp = routerIn.readLine();
				
				System.out.println("Thread " +  String.valueOf(ind) + " Recieved from other router " + temp);
				//	If both router don't have the IP then responed with NOT FOUND ERROR
				if (temp.equals("404")) 
				{ 
					out.println("404");
				}
				out.println(temp);
				routerSocket.close();
			}
			else
			{
				out.println("404");
			}
			//routerSocket.close();

			//	Responsd with IP to the requester
			//outTo.println(temp);
			//################################################################# 
			
			// Communication loop	
			//while ((inputLine = in.readLine()) != null) 
			//{
			//	System.out.println("Client/Server said: " + inputLine);
			//	if (inputLine.equals("Bye.")) // exit statement
			//			break;
			//	outputLine = inputLine; // passes the input from the machine to the output string for the destination
					
			//		if ( outSocket != null){				
			//		outTo.println(outputLine); // writes to the destination
			//		}			
			//}// end while		 
		}// end try
		catch (IOException e) 
		{
			System.err.println("Could not listen to socket.");
			System.exit(1);
		}
	}
}