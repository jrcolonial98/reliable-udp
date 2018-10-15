package client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import shared.*;

public class Client {
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		if (args.length != 3) {
			throw new IllegalArgumentException("Parameter(s): <Address> <Port> <Output filename>");
		}
		
		InetAddress serverAddress = InetAddress.getByName(args[0]);
		int serverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		
		System.out.println("What is your name?");
		Scanner scanner = new Scanner(System.in);
		String name = scanner.next();
		scanner.close();
		
		// send request packet to supplied address/port
		System.out.println("Connecting to " + serverAddress.toString() + ":" + serverPort + "...");
		RequestData data = new RequestData(name);
		
		// create receiver and join
		DataReceiver receiver = new DataReceiver(serverAddress, serverPort, fileName);
		receiver.sendRequestData(data);
		
		System.out.println("Downloading to " + fileName + "...");
		Thread receiverThread = new Thread(receiver);
		
		receiverThread.start();
		receiverThread.join();
		
		System.out.println("Done");
	}
	
}
