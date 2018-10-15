package server;

import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import shared.*;

public class Window {
	private final long TIMEOUT = 1000; // one second
	private long timer;
	
	private String[] window;
	private int size;
	
	private boolean done;
	
	private int start; // sequence number of first unacknowledged packet
	private int lastSent; // sequence number of last sent packet
	private int end; // index of last line of file
	
	private Scanner scanner; // feeds in new data
	
	private Lock mutex;
	
	public Window(int size, Scanner scanner) {
		window = new String[size * 2];
		
		done = false;
		
		// indices 0...size-1 are to be sent. size...2*size-1 are buffer.
		for (int i = 0; i < size * 2; i++) { // fill up buffer
			window[i] = scanner.nextLine();
		}
		start = 0;
		lastSent = -1;
		end = Integer.MAX_VALUE;
		
		this.size = size;
		this.scanner = scanner;
		mutex = new ReentrantLock(true);
		
		resetTimer();
	}
	
	// slide window over
	public void shift(int newStart) { 
		mutex.lock();
		int shift = newStart - start;
		
		for (int i = 0; i < shift; i++) {
			if (!scanner.hasNextLine()) {
				if (end == Integer.MAX_VALUE) {
					end = start - 1 + (size * 2);
				}
				window[start % (2 * size)] = null;
			}
			else {
				window[start % (2 * size)] = scanner.nextLine();
			}
			start++;
		}
		
		resetTimer();
		mutex.unlock();
	}
	
	// false means there are more packets to be sent
	public boolean isFull() {
		return (lastSent >= start + size || lastSent >= end);
	}
	public LineData get() {
		lastSent++;
		int index = lastSent % (size * 2);
		LineData data = new LineData(window[index], lastSent);
		
		if (window[index] == null) {
			return null;
		}
		if (lastSent == end) {
			data.setIsLast(true);
		}
		
		return data;
	}
	
	public void acknowledge(int sequenceNumber) {
		if (sequenceNumber == end) {
			done = true;
		}
		else if (sequenceNumber < start) {
			// ignore
		}
		else {
			shift(sequenceNumber + 1);
		}
	}
	
	// a packet was lost and timed out - go back N
	public void goBack() {
		lastSent = start - 1;
		resetTimer();
	}
	
	// true if the last packet has been acknowledged
	public boolean done() {
		return done;
	}
	
	// reset timeout countdown
	private void resetTimer() {
		timer = System.currentTimeMillis();
		
	}
	
	public boolean timerIsUp() {
		long currentTime = System.currentTimeMillis();
		return (currentTime - timer >= TIMEOUT);
	}

}
