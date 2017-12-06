package com.haw.vsp;

public class Message {
	String status;
	String type;
	String message;

	public Message(String status, String type, String message) {
		this.status = status;
		this.type = type;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setType(String type) {
		this.type = type;
	}	
}
