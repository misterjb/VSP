package com.haw.vsp;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Verbund_Test_Aufgabe3und4 {
	String IP_1= "http://localhost:4567";
	String IP_2= "http://localhost:4568";
	String IP_3= "http://localhost:4569";

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
}
