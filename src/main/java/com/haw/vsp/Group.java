package com.haw.vsp;

import java.util.ArrayList;

public class Group {
	String id;
	ArrayList<User> mitglieder_List = new ArrayList<>();
	String message;
	ArrayList<Assignment> assignment_List = new ArrayList<>();
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void addMitglied(User mitglied) {
		mitglieder_List.add(mitglied);
	}
	public void removeMitglieder(User mitglied) {
		mitglieder_List.remove(mitglied);
	}
	public ArrayList<User> getMitglieder_List() {
		return mitglieder_List;
	}
	public void addAssignment(Assignment asgmt) {
		assignment_List.add(asgmt);
	}
	public void removeAssignment(Assignment asgmt) {
		for (int i = 0; i < assignment_List.size(); i++) {
			if(assignment_List.get(i).getId().equals(asgmt.getId())){
				assignment_List.remove(assignment_List.get(i));
			}
		}
	}
	public ArrayList<Assignment> getAssignment_List() {
		return assignment_List;
	}
	public Group(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
