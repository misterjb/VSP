package com.haw.vsp;

import static spark.Spark.get;
import static spark.Spark.post;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import spark.Spark;

public class PlayerService2ForTesting {
	public static String my_IP;
	private static int my_PORT = 6001;
	private static String my_Group = "22";

	public static void main(String[] args) {

		Spark.port(my_PORT);
		// TODO
		try {
			my_IP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {		
			e1.printStackTrace();
		}
		if (my_IP.equals("192.168.2.51")) {
			my_IP = "http://localhost:" + my_PORT;
		} else {
			my_IP = "http://" + my_IP + ":" + my_PORT;
		}
		System.out.println(my_IP);
		
		// TODO Aufgabe 2
	}
}
