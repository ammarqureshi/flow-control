/**
 * 
 */
package cs.tcd.ie;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import tcdIO.*;

/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "localhost";	
	char nextOne = 'a';
	static ACKTimer at;
	boolean resendPacket = false;
	boolean packetSent = false;
	int togFrame=0;
	int i =0;			// index for going through array.
	String serverMessage ="OK";
	DatagramPacket packet;
	DatagramPacket thisPacket;
	Terminal terminal;
	InetSocketAddress dstAddress;
	boolean recvACKfromServer = false;
	String str=" ";

	public synchronized void check() throws IOException, InterruptedException {


		System.out.println("checking....");

		if(packetSent == true){

			if(serverMessage.contains("DUPLICATE")){

				i++;		//move to next packet now, as previous packet was a duplicate.
				nextOne = str.charAt(i);
				terminal.println("sending:" + nextOne );

				byte[] send = new byte[2];
				terminal.println("Trying to send: " + nextOne);
				toggle();		//sending next seq no.
				send[0] = (byte)togFrame;
				send[1] = (byte) nextOne;

				packet = new DatagramPacket(send,send.length, dstAddress);

				thisPacket = packet;	//store a copy of the packet that has been sent.

				socket.send(packet);			

				terminal.println("Packet sent");

				System.out.println("Sending next packet");

			}

			//else if ACK was bounced off.
			if(recvACKfromServer == false){

				//get datagram packet that was last sent to server and resend
				terminal.println("NO ACK RECEIVED FROM RECEIVER RESENDING PACKET" );
				System.out.println("Server:NO ACK RECEIVED RESENDING PACKET ");
				System.out.println("have not received ACK");

				socket.send(thisPacket);		

			}

			else{
				System.out.println("Onto next packet");
			}
		}

		else{
			System.out.println("packet still not sent");

		}
	}


	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(Terminal terminal, String dstHost, int dstPort, int srcPort) {
		try {
			this.terminal= terminal;
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	/**
	 * Assume that incoming packets contain a String and print the string.
	 * @throws InterruptedException 
	 */
	public synchronized void onReceipt(DatagramPacket packet) throws InterruptedException {
		recvACKfromServer = true;
		StringContent content= new StringContent(packet);
		serverMessage = content.toString();
		terminal.println("server:" + serverMessage);
		terminal.println("");


		if(serverMessage.contains("NOT")){
			terminal.println(" \n resend packet true");
			resendPacket = true;

		}

		terminal.println();
		this.notifyAll();
	}


	/**
	 * Sender Method
	 * 
	 */
	public synchronized void start() throws Exception {

		byte[] data= null;
		DatagramPacket packet= null;

		//obtain data from terminal
		str = (terminal.readString("String to send : "));
		str.trim();

		//iterate through the data
		for( i=0;i<str.length();i++){

			char c = str.charAt(i);
			byte[] tosend = new byte[2];
			terminal.println("Trying to send: " + c);
			tosend[1] = (byte)togFrame;
			tosend[0] = (byte) c;


			packet = new DatagramPacket(tosend, tosend.length, dstAddress);
			thisPacket = packet;
			socket.send(packet);			
			terminal.println("Packet sent");

			recvACKfromServer=false;
			packetSent = true;	
			this.wait();

			//have been notified that a response has been received from the receiver.
			recvACKfromServer=true;
			toggle();		//next seq.no.

		}

	}


	private void toggle() {
		if(togFrame==0){
			togFrame=1;
		}
		else{
			togFrame=0;
		}
	}


	/**
	 * Test method
	 * 
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Client");		
			Client c = new Client(terminal, DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT);
			at = new ACKTimer(15000, c);
			c.start();
			at.t.cancel();
			Thread.sleep(100);
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}