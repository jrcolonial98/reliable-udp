package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import shared.*;

public class DataReceiver implements Runnable {
	InetAddress serverAddress;
	int serverPort;
	
	private static DatagramSocket socket;
	
	private int nextExpected; // next expected sequence number
	FileWriter writer;
	
	public DataReceiver(InetAddress address, int port, String outFileName) throws IOException {
		this.serverAddress = address;
		this.serverPort = port;
		
		nextExpected = 0;
		writer = new FileWriter(outFileName);
		
		createSocket();
	}
	
	private void createSocket() throws SocketException {
		socket = new DatagramSocket();
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
	private void sendLineData(LineData data) throws IOException, ClassNotFoundException {	
		// SIMULATE LOST/CORRUPTED ACK BY NOT SENDING
		Random random = new Random();
		int r = random.nextInt();
		if (r % 4 == 0) {
			return; // 25% chance
		}
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(256); 
		ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream)); 
		os.flush();
		os.writeObject(data); 
		os.flush();
		
		byte[] sendBuf = byteStream.toByteArray();  
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, serverAddress, serverPort); 
		socket.send(sendPacket);
		os.close();
	}
	public void sendRequestData(RequestData data) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(256); 
		ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream)); 
		os.flush();
		os.writeObject(data); 
		os.flush();
		
		byte[] sendBuf = byteStream.toByteArray();  
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, serverAddress, serverPort); 
		socket.send(sendPacket);
		os.close();
	}

	public void run() {
		while(true) {
			try {
				LineData data = receiveLineData();
				
				// correct packet received 
				if (data.getSequenceNumber() == nextExpected) {
					// write 
					write(data.getData());
					
					// acknowledge
					sendLineData(createAck(nextExpected));
					nextExpected++;
					
					// if done, break
					if (data.isLast()) {
						break;
					}
				}
				else {
					sendLineData(createAck(nextExpected - 1));
				}
				
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// cleanup
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeSocket();
	}
	
	private void write(String line) throws IOException {
		writer.write(line + "\n");
		writer.flush();
	}
	
	private LineData createAck(int num) {
		LineData data = new LineData(null, num, true);
		return data;
	}
}
