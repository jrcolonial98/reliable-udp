package shared;

import java.io.Serializable;

public class RequestData implements Serializable {
	private String name;
	public RequestData(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
