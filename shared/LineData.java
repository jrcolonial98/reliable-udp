package shared;

import java.io.Serializable;

public class LineData implements Serializable {
	private String data; // actual content
	boolean ack; // is the packet an acknowledgement
	int sequence; // sequence number
	boolean last; // is this the last packet
	public LineData(String line, int sequenceNumber) {
		this(line, sequenceNumber, false);
	}
	public LineData(String line, int sequenceNumber, boolean ack) {
		data = line;
		this.sequence = sequenceNumber;
		this.ack = ack;
		last = false;
	}
	public String getData() {
		return data;
	}
	public boolean getAck() {
		return ack;
	}
	public int getSequenceNumber() {
		return sequence;
	}
	
	public void setIsLast(boolean isLast) {
		last = isLast;
	}
	public boolean isLast() {
		return last;
	}
}
