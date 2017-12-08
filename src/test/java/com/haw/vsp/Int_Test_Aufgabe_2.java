package com.haw.vsp;

import static org.junit.Assert.assertEquals;
import static spark.Spark.get;
import static spark.Spark.post;

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

public class Int_Test_Aufgabe_2 {
	String myUsername="jannikb";
	
	@BeforeClass
	public static void beforeClass() throws UnirestException {
		App.main(null);
	}

	@AfterClass
	public static void afterClass() {
		Spark.stop();
	}

	@Test
	public void posttestmessages() throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("status", "ALARM");
		jo.put("type", "WICHTIG");
		jo.put("message", "testnachricht1");
		HttpResponse<JsonNode> jsonResponse = Unirest.post(App.my_IP + "/messages").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		assertEquals(201, jsonResponse.getStatus());
		System.out.println("############posttestmessages#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		JSONObject jo1 = new JSONObject();
		jo1.put("status", "kein ALARM");
		jo1.put("type", "unWICHTIG");
		jo1.put("message", "testnachricht2");
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(App.my_IP + "/messages").body(jo1).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############posttestmessages#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
	}
	@Test
	public void gettestmessages() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(App.my_IP + "/messages").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		assertEquals(201, jsonResponse.getStatus());
		System.out.println("############gettestmessages#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
	}

	@Test
	public void postAndputTaverna() throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("heroclass", "heroclasse");
		jo.put("capabilities", "");
		jo.put("url", App.my_IP);
		HttpResponse<JsonNode> jsonResponse = Unirest.post(App.my_IP + "/taverna/user").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		assertEquals(201, jsonResponse.getStatus());
		System.out.println("############postTaverna#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		JSONObject jo1 = new JSONObject();
		jo1.put("heroclass", "heroclasse");
		jo1.put("capabilities", "group");
		jo1.put("url", App.my_IP);
		HttpResponse<JsonNode> jsonResponse2 = Unirest.put(App.my_IP + "/taverna/user").body(jo1).asJson();
		JSONObject jsonObj2 = jsonResponse2.getBody().getObject();
		assertEquals(200, jsonResponse2.getStatus());
		System.out.println("############postTaverna#############\n");
		System.out.println(jsonObj2 + "\n");
		System.out.println("####################################\n");
	}

	@Test
	public void getTaverna() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(App.my_IP + "/taverna").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		assertEquals(200, jsonResponse.getStatus());
		System.out.println("############getTaverna#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
	}

	@Test
	public void createAndJoinOrNotJoinGroup() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(App.my_IP + "/taverna/groups").asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############getGroup#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");

		JSONObject json = new JSONObject();
		json.put("message", "ausrede123");
		HttpResponse<JsonNode> jsonResponse3 = Unirest.post(App.my_IP + "/taverna/groups/1").body(json).asJson();
		JSONObject jsonObj3 = jsonResponse3.getBody().getObject();
		assertEquals(406, jsonResponse3.getStatus());
		System.out.println("############notjoinGroup#############\n");
		System.out.println(jsonObj3 + "\n");
		System.out.println("####################################\n");

		HttpResponse<JsonNode> jsonResponse4 = Unirest.post(App.my_IP + "/taverna/groups/1").asJson();
		JSONObject jsonObj4 = jsonResponse4.getBody().getObject();
		assertEquals(202, jsonResponse4.getStatus());
		System.out.println("############joinGroup#############\n");
		System.out.println(jsonObj4 + "\n");
		System.out.println("####################################\n");
	}

	/*
	 * { "group":"<url to the created group at the taverna>", "quest":
	 * "<the quest which shall be solved with the group>", "message":
	 * "<something you want to tell the player you invite>" }
	 */
	//TODO hirings bis jetzt nur zu mir selbst geschickt
	@Test
	public void postHirings() throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("group", "/taverna/groups/1");
		jo.put("quest", "quest beschreibung");
		jo.put("message", "msg");
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(App.my_IP + "/hirings").body(jo).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############postHirings#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
	}

	/*
	 * {
	 * "id":"<some identity chosen by the initiator to identify this request>",
	 * "task":"<uri to the task to accomplish>",
	 * "resource":"<uri or url to resource where actions are required>",
	 * "method":"<method to take – if already known>",
	 * "data":"<data to use/post for the task>",
	 * "callback":"<an url where the initiator can be reached with the results/token>",
	 * "message": "<something you want to tell the other one>" 
	 * } 
	 * {
	 * "id":"<the identity chosen by the initiator for the request>",
	 * "task":"<same as assignment>", "resource":"<same as assignment>",
	 * "method":"<method used to get this result>",
	 * "data":"<the whole response data/result of the action>",
	 * "user":"<uri to the user solved the task (own account at the blackboard)>"
	 * , "message": "<something you want to tell the other one>" 
	 * }
	 */
	@Test
	public void postAssignments() throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("id", myUsername);
		jo.put("task", "<uri to the task to accomplish>");
		jo.put("resource", "<uri or url to resource where actions are required>");
		jo.put("method", "<method to take – if already known>");
		jo.put("data", "<data to use/post for the task>");
		jo.put("callback", "/assignments/callback");
		jo.put("message", "msg");
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(App.my_IP + "/assignments").body(jo).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############postAssignments#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		
		JSONObject jo2 = new JSONObject();
		jo2.put("id", myUsername);
		jo2.put("task", "<uri to the task to accomplish>");
		jo2.put("resource", "<uri or url to resource where actions are required>");
		jo2.put("method", "<method to take – if already known>");
		jo2.put("data", "<data to use/post for the task>");
		jo2.put("user",App.my_IP);
		jo2.put("message", "msg");
		HttpResponse<JsonNode> jsonResponse2 = Unirest.post(App.my_IP + "/assignments/callback").body(jo2).asJson();
		JSONObject jsonObj2 = jsonResponse2.getBody().getObject();
		assertEquals(201, jsonResponse2.getStatus());
		System.out.println("############postCallback#############\n");
		System.out.println(jsonObj2 + "\n");
		System.out.println("####################################\n");
	}
	/*
	 * {
		"algorithm":"<name of the algorithm used>",
		"payload":"<the payload for the current state of the algorithm>",
		"user":"<uri of the user sending this request>",
		"job":"<JSON description of the job to do>",
		"message": "<something you want to tell the other one>"
		}
		job json 
		{
		"id":"<some identity choosen by the initiator to identify this request>",
		"task":"<uri to the task to accomplish>",
		"resource":"<uri or url to resource where actions are required>",
		"method":"<method to take – if already known>",
		"data":"<data to use/post for the task>",
		"callback": "<an url where the initiator can be reached with the results/token>",
		"message": "<something you want to tell the other one>"
		}
	 */	
//	@Test
//	public void postElection() throws UnirestException {
//		String callback = App.my_IP+"/callback";
//		JSONObject jo = new JSONObject();
//		jo.put("algorithm", "bully");
//		jo.put("payload", "election");
//		jo.put("user", App.my_IP);
//		jo.put("job", new JSONObject()
//				.put("id", myUsername+"election1")
//				.put("task", "<uri to the task to accomplish>")
//				.put("resource", "<uri or url to resource where actions are required>")
//				.put("method", "<method to take – if already known>")
//				.put("data", "<data to use/post for the task>")
//				.put("callback", callback)
//				.put("message", "msg"));
//		jo.put("message", "msg");
//		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(App.my_IP + "/election").body(jo).asJson();
//		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
//		assertEquals(201, jsonResponse1.getStatus());
//		System.out.println("############postElection#############\n");
//		System.out.println(jsonObj1 + "\n");
//		System.out.println("####################################\n");	
//	}
}
