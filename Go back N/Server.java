package cs.tcd.ie;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import tcdIO.Terminal;


public class Server extends Node {
	static final int DEFAULT_PORT = 50001;
	int togNum=0;
	int expectedSequence =0;
	DatagramPacket response;
	Terminal terminal;
	String strSeq="0";

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


			//create a buffer to store the packets that are recieved in order.
			ArrayList<DatagramPacket> recvPackets = new ArrayList<DatagramPacket>();

			StringContent content= new StringContent(packet);

			byte[] data;
			data= packet.getData();

			int senderSequence = data[1];
			char value = (char) data[0];
			terminal.println("Recv: " + value);

			String receivedSeq = Integer.toString(senderSequence);
			terminal.println("Received sequence number is: "+receivedSeq + " expecting:" + expectedSequence);

			if(senderSequence == expectedSequence){
				terminal.println();

				//slide window
				expectedSequence++;		//expecting next sequence number

				terminal.println("obtained data and expecting " + expectedSequence );

				//store in buffer
				recvPackets.add(packet);

				//send ACK of seq number to be expected next.
				DatagramPacket response;
				String strSeq = Integer.toString(expectedSequence);
				response = (new StringContent(strSeq).toDatagramPacket());

				terminal.println("seq into packet:" + strSeq);

				response.setSocketAddress(packet.getSocketAddress());
				socket.send(response);

			}

			//else send the previous ACK
			else{

				DatagramPacket response;
				String strSeq = Integer.toString(expectedSequence);
				response = (new StringContent(strSeq).toDatagramPacket());
				terminal.println("seq into packet:" + strSeq);
				response.setSocketAddress(packet.getSocketAddress());
				terminal.println("Sequence number OUT OF ORDER, DISCARDING");
				System.out.println("ACK lost for " + strSeq);
				socket.send(response);

			}


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
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
