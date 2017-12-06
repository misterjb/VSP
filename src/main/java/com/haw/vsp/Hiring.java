package com.haw.vsp;

public class Hiring {
	String group;
	String quest;
	String message;

	public Hiring(String group, String quest, String message) {
		this.group = group;
		this.quest = quest;
		this.message = message;
	}

	public String getGroup() {
		return group;
	}

	public String getMessage() {
		return message;
	}

	public String getQuest() {
		return quest;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setQuest(String quest) {
		this.quest = quest;
	}
}
