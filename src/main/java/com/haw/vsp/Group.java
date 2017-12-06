package com.haw.vsp;

import java.util.ArrayList;

public class Group {
	String id;
	ArrayList<String> mitglieder_List = new ArrayList<>();
	String message;
	Assignment assignment;
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}public Assignment getAssignment() {
		return assignment;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void addMitglieder(String mitglieder) {
		mitglieder_List.add(mitglieder);
	}public ArrayList<String> getMitglieder_List() {
		return mitglieder_List;
	}

	public Group(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
