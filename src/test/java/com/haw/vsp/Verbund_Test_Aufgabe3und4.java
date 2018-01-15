package com.haw.vsp;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Verbund_Test_Aufgabe3und4 {
	String IP_1 = "http://localhost:4567";
	String IP_2 = "http://localhost:4568";
	String IP_3 = "http://localhost:4569";

	@Test
	public void posttestmessages() throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("status", "ALARM");
		jo.put("type", "WICHTIG");
		jo.put("message", "testnachricht1");
		HttpResponse<JsonNode> jsonResponse = Unirest.post(IP_1 + "/messages").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		assertEquals(201, jsonResponse.getStatus());
		System.out.println("############posttestmessages#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		JSONObject jo1 = new JSONObject();
		jo1.put("status", "kein ALARM");
		jo1.put("type", "unWICHTIG");
		jo1.put("message", "testnachricht2");
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP_2 + "/messages").body(jo1).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############posttestmessages#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		JSONObject jo2 = new JSONObject();
		jo2.put("status", "kein ALARM");
		jo2.put("type", "unWICHTIG");
		jo2.put("message", "testnachricht2");
		HttpResponse<JsonNode> jsonResponse2 = Unirest.post(IP_3 + "/messages").body(jo2).asJson();
		JSONObject jsonObj2 = jsonResponse2.getBody().getObject();
		assertEquals(201, jsonResponse2.getStatus());
		System.out.println("############posttestmessages#############\n");
		System.out.println(jsonObj2 + "\n");
		System.out.println("####################################\n");
	}
	
//	@Test
	public void testElection() throws UnirestException{
		JSONObject jo = new JSONObject();
		jo.put("algorithm", "bully algo");
		jo.put("payload", "payload");
		jo.put("user", App.my_IP);
		jo.put("job", new JSONObject().put("id", "id").put("task", "task").put("resource", "resource").put("method", "method")
				.put("data", "data").put("callback", "http://localhost:4567").put("message", "message1"));
		jo.put("message", "message2");
		HttpResponse<JsonNode> jsonResponse = Unirest.post("http://localhost:4567" + "/election").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############postElection#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
	}
	
	
//	@Test
	public void testmutex() throws UnirestException, InterruptedException {
		JSONObject jsonsetmutex = new JSONObject();
		jsonsetmutex.put("mutex", "held");
		HttpResponse<String> response1 = Unirest.post("http://localhost:4568/setmutex").body(jsonsetmutex).asString();
		System.out.println("http://localhost:4568 " + response1.getBody());

		System.out.println("Client A wants to enter the critical section");
		JSONObject jsonsetmutex2 = new JSONObject();
		jsonsetmutex2.put("mutex", "wanting");
		try {
			HttpResponse<String> response2 = Unirest.post("http://localhost:4567/setmutex").body(jsonsetmutex2)
					.asString();
			System.out.println("http://localhost:4567 " + response2.getBody());
		} catch (Exception e) {
			
		}
		
		System.out.println("sleep");
		Thread.sleep(5000);
		try {
			System.out.println("C wants to enter the critical section & sends request to A, B, C");
			JSONObject jsonsetmutex3 = new JSONObject();
			jsonsetmutex3.put("mutex", "wanting");
			HttpResponse<String> response3 = Unirest.post("http://localhost:4569/setmutex").body(jsonsetmutex3)
					.asString();
			System.out.println("http://localhost:4569 " + response3.getBody());
		} catch (Exception e) {
			
		}
		System.out.println("sleep");
		Thread.sleep(5000);
		System.out.println("B finishes his critical section");
		JSONObject jsonsetmutex4 = new JSONObject();
		jsonsetmutex4.put("mutex", "released");
		HttpResponse<String> response4 = Unirest.post("http://localhost:4568/setmutex").body(jsonsetmutex4).asString();
		System.out.println("http://localhost:4568 " + response4.getBody());
		
		System.out.println("sleep");
		Thread.sleep(5000);
		System.out.println("A has finished his critical section");
		JSONObject jsonsetmutex5 = new JSONObject();
		jsonsetmutex5.put("mutex", "released");
		HttpResponse<String> response5 = Unirest.post("http://localhost:4567/setmutex").body(jsonsetmutex5).asString();
		System.out.println("http://localhost:4567 " + response5.getBody());

	}
}
