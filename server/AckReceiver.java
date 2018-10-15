package server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import shared.*;

public class AckReceiver implements Runnable {
	InetAddress clientAddress;
	int clientPort;
	int serverPort;
	
	Window window;
	
	private static DatagramSocket socket;
	
	public AckReceiver(int serverPort, InetAddress address, int port, Window window) throws SocketException {
		this.clientAddress = address;
		this.clientPort = port;
		this.serverPort = serverPort;
		
		this.window = window;
		
		createSocket();
	}
	
	private void createSocket() throws SocketException {
		socket = new DatagramSocket(serverPort);
	}
	private void closeSocket(){
		socket.close();
	}
	
	private LineData receiveLineData() throws IOException, ClassNotFoundException {
		byte[] buf = new byte[256];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		buf = packet.getData();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
		
		LineData data = (LineData)is.readObject();
		is.close();
		return data;
	}

	public void run() {
		while(true) {
			try {
				LineData data = receiveLineData();
				
				window.acknowledge(data.getSequenceNumber());
				
				if (window.done()) {
					break;
				}
				
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			
		}
		closeSocket();
	}
}
