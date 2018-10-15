package server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import shared.*;

public class Server {
	private static final int WINDOW_SIZE = 10;
	
	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException {
		if (args.length != 2) {
			throw new IllegalArgumentException("Parameter(s): <Port> <filename>");
		}
		int port = Integer.parseInt(args[0]);
		String filename = args[1];
		
		System.out.println("Starting a new " + filename + " server at port " + port + "...");
		
		File file = new File(filename);
		
		//while loop - wait for requests
		while(true) {
			System.out.println("Waiting for new download request");
			
			// receive download request
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			DatagramSocket socket = new DatagramSocket(port);
			socket.receive(packet);
			System.out.println("Received request.");
			buf = packet.getData();
			ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
			
			InetAddress destinationAddress = packet.getAddress();
			int destinationPort = packet.getPort();
			RequestData data = (RequestData)is.readObject();
			
			is.close();
			socket.close();
			
			System.out.println("Setting up new connection with " + data.getName() + "...");
			
			// set up threads
			Scanner scanner = new Scanner(file);
			Window window = new Window(WINDOW_SIZE, scanner);
			DataSender ds = new DataSender(destinationAddress, destinationPort, window);
			AckReceiver ar = new AckReceiver(port, destinationAddress, destinationPort, window);
			Thread sender = new Thread(ds);
			Thread receiver = new Thread(ar);
			
			// run
			sender.start();
			receiver.start();
			
			sender.join();
			receiver.join();
			
			System.out.println("Download complete.");
		}
		//    create thread for that request
	}
}
