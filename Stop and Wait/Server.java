package cs.tcd.ie;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.NumberFormat;

import tcdIO.Terminal;


public class Server extends Node {
	static final int DEFAULT_PORT = 50001;
	static int expectedFrame=0;
	Terminal terminal;

	/*
	 * 
	 */
	Server(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void  onReceipt(DatagramPacket packet) {
		try {


			StringContent content= new StringContent(packet);

			byte[] data;

			data= packet.getData();
			int dataTog = data[1];
			String receivedToggle = Integer.toString(dataTog);
			terminal.println("tog from receiver is:"+receivedToggle + "expecting :" + expectedFrame);
			terminal.println("Recv: " + content.toString());
			terminal.println();
			
			DatagramPacket response;
			
			if(dataTog == expectedFrame){	

				//only move to next when the correct packet has been received
				toggle();		//now receiver expects the nex seq. no. 
				response= (new StringContent("OK" )).toDatagramPacket();
				terminal.println("GOT IT!");

			}
			
			//else discard it.
			else{		
				response= (new StringContent("DUPLICATE")).toDatagramPacket();
				terminal.println("ALREADY RECEIVED " + receivedToggle + " EXPECTING " + expectedFrame);
				System.out.println("ALREADY RECEIVED " + receivedToggle + " EXPECTING " + expectedFrame);
			}


			response.setSocketAddress(packet.getSocketAddress());
			socket.send(response);

		}
		catch(Exception e) {

			if (!(e instanceof SocketException)) 
			{
				synchronized(this){

					System.err.println("Server:Negative Acknowledgement by server_SEND PACKET AGAIN");						


				}

			}
		}
	}



	private void toggle() {
		if(expectedFrame==0){
			expectedFrame=1;
		}
		else{
			expectedFrame=0;
		}
	}


	public synchronized void start() throws Exception {
		terminal.println("Waiting for contact");
		this.wait();
	}

	/*
	 * 
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Server");
			(new Server(terminal, DEFAULT_PORT)).start();
			terminal.println("Program completed!");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
