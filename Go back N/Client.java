/**
 * 
 */
package cs.tcd.ie;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import tcdIO.Terminal;

/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node {
	static ACKTimer at;
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "localhost";	
	static int togNum=0;
	int markPacket ;
	boolean buffered = false;
	ArrayList<Integer>markACK =  new ArrayList<Integer>();;
	ArrayList<DatagramPacket> bufferedPackets;
	int i =0;
	String serverAckMessage;
	int serverSeq;
	DatagramPacket thisPacket;
	final int w_size = 15;
	int prvPackReceived=0;
	int indexOfLastPakSent=0;
	int checkingPointer=-1;
	int j=0;
	int sizeOfMessage =0;
	int current_w_size=0;
	Terminal terminal;
	InetSocketAddress dstAddress;
	boolean firstFrameSent = false;


	public synchronized void check() {


		System.out.println(markACK.toString());
		//if any packet received
		if(prvPackReceived >=0){
			System.out.println("last pack ACKD" + prvPackReceived);
		}

		int i;
		int count =0;
		//checking the window size.
		for(i=0;i<markACK.size();i++){

			if(markACK.get(i)==1){
				count++;
			}
			current_w_size = count;
		}


		if(buffered == true){

			if(checkingPointer<=(sizeOfMessage-1) && current_w_size>=0){
				System.out.println("prv pack:" + prvPackReceived + " size:"  + sizeOfMessage);
				System.out.println("checking Pointer :" + checkingPointer);

				//checkingPointer points to the oldest UNACK'D packet.
				//if it has not yet been ACK'D before timer times out ->resend 
				if(checkingPointer>=0){
					System.out.println("checking if index " + checkingPointer + "is still not ACK'D");

					if(markACK.get(checkingPointer) ==1){
						int index;	
						System.out.println("Need to send again.");	

						//resend the whole window
						for(index = checkingPointer;index<(checkingPointer + current_w_size);index++){
							System.out.println("\t SENDING...");
							DatagramPacket resend = bufferedPackets.get(index);

							try {
								socket.send(resend);	
							} 
							catch (IOException e) {
								e.printStackTrace();

							}
						}
					}


					else{
						System.out.println("previouos packet has been ACK'D");
					}


				}

			}

			System.out.println("window size: " + count);
			terminal.println("window size: " + count);
			int c;
			for(c=0;c<markACK.size();c++){

				if(markACK.get(c)==1){
					System.out.println("index:" + c +"is the oldest UNACK'D packet");	
					break;
				}
			}

			checkingPointer = c;


		}

		// if have reached the end
		if(markACK.get(sizeOfMessage -1) ==2 ){
			System.out.println("ALL PACKETS SENT HAVE BEEN ACKNOWLEDGED");
			at.t.cancel();		//cancel timer.

		}

		terminal.println("\n");

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
	 */
	public synchronized void onReceipt(DatagramPacket packet) {

		StringContent content= new StringContent(packet);
		serverAckMessage = content.toString();
		String numberOnly = serverAckMessage.replaceAll("[^0-9]", "");	//extract number from the message.
		serverSeq = Integer.parseInt(numberOnly); 
		terminal.println();
		this.notify();

		try {
			this.wait();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Sender Method
	 * 
	 */
	public synchronized void start() throws Exception {

		byte[] data= null;
		DatagramPacket packet= null;
		String str = (terminal.readString("String to send: "));
		str.trim();
		sizeOfMessage = str.length();
		int seqNumber =0;
		int lastSeq = str.length()-1;		//index starts from 0.
		bufferedPackets = new ArrayList<DatagramPacket>();		//create an array list of DatagramPackets
		int index;

		//markACK
		//0-> NOT SENT		1->SENT BUT NOT YET ACK'D  	2->SENT AND ACK'D

		//mark them as not sent yet
		for(index=0;index<str.length();index++){
			markACK.add(0);
		}

		int i;
		//add all the packets to the buffer
		for( i=0;i<str.length();i++){
			char c = str.charAt(i);
			byte[] tosend = new byte[2];
			tosend[0] = (byte) c;
			tosend[1] = (byte)seqNumber;
			packet = new DatagramPacket(tosend, tosend.length, dstAddress);	//create packet
			seqNumber++;
			bufferedPackets.add(i,packet);	
		}

		buffered = true;
		int currentSeq =0;

		while(currentSeq<w_size && currentSeq!=str.length()){

			DatagramPacket packetTosend = bufferedPackets.get(j);	//GET PACKET FROM Sliding window.

			socket.send(packetTosend);			//send the packet
			terminal.println("Packet sent with ack no:" + j);
			System.out.println("packet sent");

			markACK.set(j,1);		//sent but not ACK'D

			j++;
			currentSeq++;	

		}
		firstFrameSent= true;
		indexOfLastPakSent =j-1;

		System.out.println("enter loop");
		boolean sendAgain=false;
		boolean getACKD = true;

		int inx=0;
		for(inx=0;inx<markACK.size();inx++){

			terminal.print( "|"+markACK.get(inx)+"|");

		}
		terminal.println("\n");
		while(true){
			while(getACKD){

				this.wait();

				terminal.println("");
				markPacket = serverSeq -1;
				terminal.println("packet no.:" + markPacket + " ACK'D");
				terminal.println("prev pack recv:" + prvPackReceived);

				if(markPacket>=0 && markPacket<lastSeq){

					int k;	
					//mark all the packets before it in case previous ACK were lost->Cummulative acknowledgement.
					for(k=0;k<=markPacket;k++){
						markACK.set(k, 2);	//set to 2 if sent and ACK'D		
					}


					if(currentSeq<=lastSeq){

						int diffACK = serverSeq - prvPackReceived; 
						terminal.println("diff" + diffACK);
						int ix;
						int currentPointer = 0;

						for(ix=1;ix<=diffACK;ix++){
							terminal.println("last pack" + indexOfLastPakSent);
							currentPointer = indexOfLastPakSent + ix;
							terminal.println("current Pointer" + currentPointer);
							DatagramPacket packetTosend = bufferedPackets.get(currentPointer);	//GET PACKET FROM Sliding window.
							socket.send(packetTosend);			//send the packet
							terminal.println("Window sliding..");
							terminal.println("Packet sent with seq. no:" + currentSeq);

							markACK.set(currentPointer,1);		//1 means sent but not ACK'D							

							j++;
							currentSeq++;
						}

						indexOfLastPakSent=currentPointer;

					}

				}
				//if ACK recieved was the last one
				else if(markPacket==lastSeq){
					int m;
					for(m=0;m<=markPacket;m++){
						markACK.set(m, 2);	//set to 2 if sent and ACK'D		
					}


					int ix=0;

					for(ix=0;ix<markACK.size();ix++){
						terminal.print( "|"+markACK.get(ix)+"|");
					}
					terminal.println("");
					terminal.println("Have received last ACK");
					getACKD =false;
					break;		
				}

				prvPackReceived = serverSeq;
				int in;

				for(in=0;in<markACK.size();in++){
					terminal.print( "|"+markACK.get(in)+"|");
				}
				terminal.println();

				this.notify();
			}

			if(getACKD == false){break;}



		} 		
	}



	/**
	 * Test method
	 * 
	 * Sends a packet to a given address
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//try {					
		Terminal terminal= new Terminal("Client");		
		Client c = new Client(terminal, DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT);
		at = new ACKTimer(10000, c);	//10 000
		//ACKTimerTask att = new ACKTimerTask(c);
		c.start();
		terminal.println("Program completed");
		at.t.cancel();
	}




}