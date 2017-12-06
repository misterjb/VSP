package com.haw.vsp;

public class Assignment {
	String id;
	String task;
	String resource;
	String method;
	String data;
	String callback;
	String message;
	String user;

	public Assignment(String id, String task, String resource, String method, String data, String callback,
			String message) {
		this.id = id;
		this.task = task;
		this.resource = resource;
		this.method = method;
		this.data = data;
		this.callback = callback;
		this.message = message;

	}public String getUser() {
		return user;
	}

	public void updateAssigment(String method,String data,String user,String message){
		this.method=method;
		this.data=data;
		this.user=user;
		this.message=message;
	}
	public String getCallback() {
		return callback;
	}

	public String getData() {
		return data;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getMethod() {
		return method;
	}

	public String getResource() {
		return resource;
	}

	public String getTask() {
		return task;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public void setTask(String task) {
		this.task = task;
	}
}
