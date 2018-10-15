package server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import shared.LineData;

public class DataSender implements Runnable {
	InetAddress clientAddress;
	int clientPort;
	
	Window window;
	
	private static DatagramSocket socket;
	
	public DataSender(InetAddress address, int port, Window window) throws SocketException {
		this.clientAddress = address;
		this.clientPort = port;
		this.window = window;
		
		createSocket();
	}
	
	private void createSocket() throws SocketException {
		socket = new DatagramSocket();
	}
	private void closeSocket(){
		socket.close();
	}
	
	private void sendLineData(LineData data) throws IOException, ClassNotFoundException {	
		if (data == null) return;
		
		// SIMULATE LOST/CORRUPTED ACK BY NOT SENDING
		Random random = new Random();
		int r = random.nextInt();
		if (r % 4 == 0) {
			return; // 25% chance
		}
		// slows it down big time. Decreasing timeout time or fail chance would speed it up.
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(256); 
		ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream)); 
		os.flush();
		os.writeObject(data); 
		os.flush();
		
		byte[] sendBuf = byteStream.toByteArray();  
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, clientAddress, clientPort); 
		socket.send(sendPacket);
		os.close();
	}
	public void run() {
		while(true) {
			
			// send packets until the window is full
			while (!window.isFull()) {
				LineData next = window.get();
				try {
					sendLineData(next);
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// upon timeout, go back N
			if (window.timerIsUp()) {
				window.goBack();
			}
			
			// break if done
			if (window.done()) {
				break;
			}
		}
		
		closeSocket();
	}
}
