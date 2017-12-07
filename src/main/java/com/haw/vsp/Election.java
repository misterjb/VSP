package com.haw.vsp;

public class Election {
	String algorithm;
	String payload;
	String user;
	Assignment job;
	String message;

	public Election(String algorithm, String payload, String user, Assignment job, String message) {
		this.algorithm = algorithm;
		this.payload = payload;
		this.user = user;
		this.job = job;
		this.message = message;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Assignment getJob() {
		return job;
	}

	public String getMessage() {
		return message;
	}

	public String getPayload() {
		return payload;
	}

	public String getUser() {
		return user;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setJob(Assignment job) {
		this.job = job;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
