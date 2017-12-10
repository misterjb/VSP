package com.haw.vsp;

import static org.junit.Assert.assertEquals;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.Body;

import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

public class App {
	private static String ausgabe = "";
	private static String blackboard_IP = "", blackboard_Port = "";
	private static String authenticationToken = "";
	private static HashMap<String, String> locationMap = new HashMap<>();
	private static String loginFehlerAusgabe = "";
	public static String my_IP;
	private static int my_PORT = 4567;
	private static ArrayList<User> user_List = new ArrayList<>();
	private static ArrayList<Message> message_List = new ArrayList<>();
	private static int groupanzahl = 1;
	private static ArrayList<Group> group_List = new ArrayList<>();
	private static ArrayList<Hiring> hiring_List = new ArrayList<>();
	//private static String my_IP = "172.19.0.7";
	private static HashMap<String,String> questList = new HashMap<>();
	private static HashMap<Object,Object> tokenMap = new HashMap<>();

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Spark.port(my_PORT);
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
		/*
		 * • heroclass:    describe your trade – you may be creative here •
		 * • capabilities: you have not earned any yet, so this is an empty
		 *   			   string. But it will become a comma separated list of capabilities you
		 *                 have earned through solving assignments. 
		 * • url: a fully qualified url to reach the player service!
		 */
		post("/tavern", (request, response) -> {
			User usr = new Gson().fromJson(request.body(), User.class);
			System.out.println(request.ip());
			user_List.add(usr);
			response.type("application/json");
			response.status(201);
			response.body(new Gson().toJson(usr));
			return response.body();
		});
		get("/tavern/users/:id", (request, response) -> {
			for (int i = 0; i < user_List.size(); i++) {
				if(user_List.get(i).getUrl().equals(request.params(":id"))){
					User usr =user_List.get(i);
					response.type("application/json");
					response.status(200);
					response.body(new Gson().toJson(usr));
					return response.body();
				}
			}
			response.type("application/json");
			response.status(400);
			response.body(new Gson().toJson(request.body()));
			return response.body();
		});
		put("/tavern", (request, response) -> {
			User regtemp;
			for (int i = 0; i < user_List.size(); i++) {
				System.out.println(request.ip());
				if (user_List.get(i).getUrl().equals(request.ip())) {
					User usr = new Gson().fromJson(request.body(), User.class);
					user_List.get(i).setCapabilities(usr.getCapabilities());
					response.body(new Gson().toJson(user_List.get(i)));
					response.status(200);
					return response.body();
				}
			}
			response.body(request.body());
			response.status(409);
			return response.body();

		});
		/*
		 * { "user":"<link to the registered user account>", "idle": <boolean,
		 * if you have no assignment currently>, "group":
		 * "<url to the group you are in>", "hirings":
		 * "<uri to which one may post to hire you for a group>", "assignments":
		 * "<uri to which one may post an assignment", "messages":
		 * "<uri to which one may post messages>", "election":
		 * "<uri to which one sends election messages to>" }
		 */
		get("/tavern", (request, response) -> {
			String groupurl ="";
			String userurl="";
			for (int i = 0; i < group_List.size(); i++) {
				if(group_List.get(i).getMitglieder_List().contains(request.ip())){
					groupurl = my_IP + "/tavern/groups/" + group_List.get(i).getId();
				}
			}
			for (int i = 0; i <user_List.size() ; i++) {
				if(user_List.get(i).getUrl().equals(request.ip())){
					userurl = my_IP+"/tavern/users/"+request.ip();
				}
			}
			if(userurl.equals("")){
				userurl= "not registered yet";
			}
			if(groupurl.equals("")){
				groupurl= "no group";
			}
			JSONObject json = new JSONObject();
			json.put("user", userurl);
			json.put("idle", "false");
			json.put("group",groupurl );
			json.put("hirings", "/hirings");
			json.put("assignments", "/assignments");
			json.put("messages", "/messages");
			json.put("election", "/election");
			response.type("application/json");
			response.status(200);
			response.body(json.toString());
			return response.body();
		});
		post("/hirings", (request, response) -> {
			Hiring grp = new Gson().fromJson(request.body(), Hiring.class);
			hiring_List.add(grp);
			response.status(201);
			response.body(new Gson().toJson(grp));
			return response.body();
		});
		get("/hirings", (request, response) -> {
			JSONObject json = new JSONObject();
			for (int i = 0; i < hiring_List.size(); i++) {
				json.put(""+i, new Gson().toJson(hiring_List.get(i)));
			}
			hiring_List.clear();
			response.status(201);
			response.body(json.toString());
			return response.body();
		});
		post("/assignments", (request, response) -> {
			Assignment asmnt = new Gson().fromJson(request.body(), Assignment.class);
			for (int i = 0; i < group_List.size(); i++) {
				for (User usr : group_List.get(i).getMitglieder_List()) {
					if (usr.getUrl().equals(request.ip())) {
						group_List.get(i).addAssignment(asmnt);
					}
				}
			}
			response.status(201);
			response.body(new Gson().toJson(asmnt));
			return response.body();
		});
		post("/assignments/callback", (request, response) -> {
			Assignment asgmt = new Gson().fromJson(request.body(), Assignment.class);
			for (int i = 0; i < group_List.size(); i++) {
				for (int j = 0; j < group_List.get(i).getAssignment_List().size(); j++) {
					if(group_List.get(i).getAssignment_List().get(j).getId().equals(asgmt.getId())){
						group_List.get(i).getAssignment_List().get(j).updateAssigment(asgmt.getMethod(), asgmt.getData(), asgmt.getUser(), asgmt.getMessage());
						response.status(201);
						response.body(request.body());
						return response.body();
					}
				}
			}
			response.status(400);
			response.body(request.body());
			return response.body();
			
		});
		get("/assignments", (request, response) -> {
			JSONObject json = new JSONObject();
			for (int i = 0; i < group_List.size(); i++) {
				for (User usr : group_List.get(i).getMitglieder_List()) {
					if (usr.getUrl().equals(request.ip())) {
						JSONArray jsArray = new JSONArray(group_List.get(i).getAssignment_List());					       
						json.put("Assigments",jsArray);
					}
				}
			}
			response.status(201);
			response.body(new Gson().toJson(json));
			return response.body();
		});
		post("/messages", (request, response) -> {
			Message msg = new Gson().fromJson(request.body(), Message.class);
			message_List.add(msg);
			response.status(201);
			response.body(new Gson().toJson(msg));
			return response.body();
		});
		get("/messages", (request, response) -> {
			JSONObject json = new JSONObject();
			for (int i = 0; i < message_List.size(); i++) {				
				json.put(""+i, new Gson().toJson(message_List.get(i).getMessage()));
			}
			message_List.clear();
			response.status(201);
			response.body(json.toString());
			return response.body();
		});
		post("/tavern/groups", (request, response) -> {
			group_List.add(new Group("" + groupanzahl));
			JSONObject json = new JSONObject();
			json.put("msg", "Gruppe wurde erstellt");
			json.put("groupnr", "" + groupanzahl);
			response.status(201);
			response.body(json.toString());
			groupanzahl++;
			return response.body();
		});
		get("/tavern/groups/:id", (request, response) -> {
			for (int i = 0; i < group_List.size(); i++) {
				if(group_List.get(i).getId().equals(request.params(":id"))){
					Group temp =group_List.get(i);
					response.body(new Gson().toJson(temp));
					response.status(200);
					return response.body();
				}
			}
			response.body(request.body());
			response.status(400);
			return response.body();
		});
		post("/tavern/groups/:id", (request, response) -> {
			if (request.body().length() != 0) {
				response.status(406);
				response.body(request.body());
				return response.body();
			} else {
				int grpnr = Integer.parseInt(request.params(":id"));
				Group grp = group_List.get(grpnr - 1);
				JSONObject json = new JSONObject();
				json.put("msg", "Gruppe beigetreten");
				json.put("groupnr", grp.getId());
				for (int i = 0; i < user_List.size(); i++) {
					if(user_List.get(i).getUrl().equals(request.ip())){
						grp.addMitglied(user_List.get(i));
						response.status(202);
						response.body(json.toString());
						return response.body();
					}
				}
				response.status(400);
				response.body(request.body());
				return response.body();
				
			}
		});		
		post("/election", (request, response) -> {
			Election elec = new Gson().fromJson(request.body(), Election.class);
			String callback = elec.getJob().getCallback();
			String user = elec.getUser();
			elec.setPayload("ok");
			elec.getJob().setCallback(my_IP + "/callback");
			// answer: confirm the entrance of an election message
			HttpResponse<JsonNode> jsonResponse1 = Unirest.post(callback).body(new Gson().toJson(elec)).asJson();

			// election
			ArrayList<HttpResponse<JsonNode>> jsonResponseList = new ArrayList<>();
			Group tempgrp =null;
			Thread.sleep(10);
			for (int i = 0; i < group_List.size(); i++) {
				for (int j = 0; j < group_List.get(i).getMitglieder_List().size(); j++) {
					if (user.equals(group_List.get(i).getMitglieder_List().get(j))) {
						tempgrp = group_List.get(i);
						for (User usr : group_List.get(i).getMitglieder_List()) {
							if (!usr.getUrl().equals(user)) {
								elec.setPayload("election");
								jsonResponseList.add(Unirest.post(usr + "/election").body(new Gson().toJson(elec)).asJson());
								System.out.println("election an"+usr +"geschickt");
							}
						}
					}
				}
			}
			Thread.sleep(10);
			for (int i = 0; i < jsonResponseList.size(); i++) {
				if (jsonResponseList.get(i).getBody().getObject().get("payload").equals("ok")) {
					elec.setMessage("got an answer, break the election, wait for the coordinator-message");
					response.body(new Gson().toJson(elec).toString());
					response.status(200);
					return response.body();
				} else {
					for (User usr : tempgrp.getMitglieder_List()) {
						if (!usr.getUrl().equals(user)) {
							elec.setPayload("cordinator");
							HttpResponse<JsonNode> jsonResponse2 = Unirest.post(usr.getUrl() + "/callback").body(new Gson().toJson(elec))
									.asJson();
						}
					}
				}
			}
			response.body(request.body());
			response.status(200);
			return response.body();
		});
		post("/callback", (request, response) -> {
			JSONObject jo = new JSONObject(request.body());
			if (jo.get("payload").equals("ok")) {
				response.status(200);
				return response;
			} else {
				response.body(request.body());
				return response.body();
			}
		});

		get("/", (request, response) -> {
			try {
				int port = 24000;

				// Create a socket to listen on the port.
				DatagramSocket dsocket = new DatagramSocket(port);

				// Create a buffer to read datagrams into. If a
				// packet is larger than this buffer, the
				// excess will simply be discarded!
				byte[] buffer = new byte[2048];

				// Create a packet to receive data into the buffer
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				// Wait to receive a datagram
				dsocket.receive(packet);

				// Convert the contents to a string, and display them
				String msg = new String(buffer, 0, packet.getLength());
				// ausgabe += "IP: " +
				// packet.getAddress().toString().substring(1) + "\n";
				msg = msg.substring(msg.lastIndexOf(':') + 1, msg.length() - 1);
				// ausgabe += "Port: " + msg + "\n";

				blackboard_Port = msg;
				blackboard_IP = packet.getAddress().toString().substring(1);

				// Reset the length of the packet before reusing it.
				packet.setLength(buffer.length);
				dsocket.close();
			} catch (Exception e) {
				System.err.println(e);
			}
			Map<String, String> model = new HashMap<>();
			model.put("Ausgabe", loginFehlerAusgabe);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/login.vtl"));
		});
		post("/login", (request, response) -> {
			if (request.queryParams("btn").equals("Register")) {
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("registerUser");
				System.out
						.println("-----------------------------------------------------------------------------------");

				registerUser(request.queryParams("txt_username"), request.queryParams("txt_password"));
				if (loginFehlerAusgabe.equals("Username already taken")) {
					response.redirect("/");
				}

			}
			if (!loginFehlerAusgabe.equals("Username already taken")) {
				loginFehlerAusgabe = "";
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("login");
				System.out
						.println("-----------------------------------------------------------------------------------");
				login(request.queryParams("txt_username"), request.queryParams("txt_password"));
				System.out
						.println("-----------------------------------------------------------------------------------");
				ausgabe += "-----------------------------------------------------------------------------------------\n";
				ausgabe += "Erfolgreich eingeloggt\n";
				ausgabe += "-----------------------------------------------------------------------------------------\n";
				response.redirect("/index");
			}
			return null;
		});
		get("/index", (request, response) -> {
			Map model = new HashMap<>();
			model.put("Ausgabe", ausgabe);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
		post("/index", (request, response) -> {
			String[] Inputs = null;
			String param="";
			if(request.queryParams("Input").contains(",")){
				Inputs = request.queryParams("Input").split(",");
			}else{
				param= request.queryParams("Input");
			}
			switch (request.queryParams("Quest")) {
			case "reset":
				resetAusgabe();
				break;
			case "deliverableDetails":
				showDeliverableDetails(param);
				break;
			case "deliveriesList":
				showDeliveriesList();
				break;
			case "deliveryDetails":
				showDeliveryDetails(param);
				break;
			case "questsList":
				showQuestsList();
				break;
			case "questDetails":
				showQuestDetails(param);
				break;
			case "questTaskList":
				showQuestTaskList(param);
				break;
			case "taskDetails":
				showTaskDetails(param);
				break;
			case "map":
				showMap();
				break;
			case "mapInfo":
				showMapInfo(param);
				break;
			case "userList":
				showUserList();
				break;
			case "userDetails":
				showUserDetails(param);
				break;
			case "gotoLocation":
				gotoLocation(Inputs[0],Inputs[1]);
				break;
			case "allinOne":
				completeQuestOne();
				break;
			case "allinOne2":
				completeQuestTwo();
				break;
			case "posttestmessages":
				posttestmessages();
				break;
			case "postHirings":
				if(Inputs.length==4){
					postHirings(Inputs[0],Inputs[1],Inputs[2],Inputs[3]);
				}else{
					postHirings(Inputs[0],Inputs[1],Inputs[2],"");	
				}
				break;
			case "gettestmessages":
				gettestmessages();
				break;
			case "getHirings":
				gethirings();
				break;	
			case "postTavern":
				if(Inputs.length==3){
					postTavern(Inputs[0],Inputs[1],Inputs[2]);
				}else{
					postTavern(Inputs[0],Inputs[1],"");
				}
				break;
			case "putTavern":
				if(Inputs.length==2){
					putTavern(Inputs[0],Inputs[1]);
				}else{
					putTavern(param,"");
				}
				break;
			case "getTavern":
				if(!param.equals("")){
					getTavern(param);
				}else{
					getTavern("");
				}
				break;
			case "createGroup":
				if(!param.equals("")){
					createGroupwithIP(param);
				}else{
					createGroupwithIP("");
				}
				break;	
			case "notjoinGroup":
				if(Inputs.length==3){
					notjoinGroupwithIP(Inputs[0],Inputs[1],Inputs[2]);
				}else{
					notjoinGroupwithIP(Inputs[0],Inputs[1],"");
				}
				break;
			case "joinGroup":
				if(!param.equals("")){
					joinGroupwithIP(param,"");
				}else{
					joinGroupwithIP(Inputs[0],Inputs[1]);
				}
				break;
			case "getGroup":
				if(!param.equals("")){
					getGroup(param,"");
				}else{
					getGroup(Inputs[0],Inputs[1]);
				}
				break;
			case "postAssignments":
				if(Inputs.length==7){
					postAssignments(Inputs[0],Inputs[1],Inputs[2],Inputs[3],Inputs[4],Inputs[5],Inputs[6]);
				}else{
					postAssignments(Inputs[0],Inputs[1],Inputs[2],Inputs[3],Inputs[4],Inputs[5],"");
				}
				break;
			case "postmessage":
				postmessage(Inputs[0],Inputs[1],Inputs[2],Inputs[3]);
				break;
			case "Question":
				showquestions();
				break;
			case "getAssignments":
				getAssignments();
				break;
			}
			System.out.println("Questparameter:"+request.queryParams("Quest"));
			System.out.println("Inputparameter:"+request.queryParams("Input"));
			Map model = new HashMap<>();
			model.put("Ausgabe", ausgabe);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
	}

	private static void getAssignments() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse1 = Unirest.get(App.my_IP + "/assignments").asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############getAssignments#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############getAssignments#############\n";
		ausgabe +=jsonObj1+ "\n";
		ausgabe +="########################################\n";
	}
	
	private static void showquestions() {
		ausgabe += "• Player/Heroservice – Blackboard = Blackboard\n";
		ausgabe += "• Everyone – Blackboard (discovery) = Broadcast\n";
		ausgabe += "• Player/Heroservice – Player/Heroservice = Peer-to-Peer\n";
		ausgabe += "• Player/Heroservice – Location = Token-Ring\n";
		ausgabe += "• Quest giver – Player =  Client-Server\n";
	}

	private static void resetAusgabe() {
		ausgabe = "";
	}

	private static void postmessage(String status, String type,String message,String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject jo = new JSONObject();
		jo.put("status", status);
		jo.put("type", type);
		jo.put("message", message);
		HttpResponse<JsonNode> jsonResponse = Unirest.post(IP + "/messages").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############postmessage#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############postmessage#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}

	private static void getGroup(String groupnr,String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		HttpResponse<JsonNode> jsonResponse = Unirest.get(IP + "/groups/"+groupnr).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############getGroup#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############getGroup#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}

	private static void posttestmessages() throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("status", "ALARM");
		jo.put("type", "WICHTIG");
		jo.put("message", "testnachricht1");
		HttpResponse<JsonNode> jsonResponse = Unirest.post(my_IP + "/messages").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############posttestmessage1#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		JSONObject jo1 = new JSONObject();
		jo1.put("status", "kein ALARM");
		jo1.put("type", "unWICHTIG");
		jo1.put("message", "testnachricht2");
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(my_IP + "/messages").body(jo1).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############posttestmessage2#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
	}

	private static void gettestmessages() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse =Unirest.get(my_IP + "/messages").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############gettestmessages#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############gettestmessages#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}
	
	private static void gethirings() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(my_IP + "/hirings").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############gethirings#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############gethirings#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}

	private static void postTavern(String heroclasse,String capabilities,String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject jo = new JSONObject();
		jo.put("heroclass", heroclasse);
		jo.put("capabilities", "");
		jo.put("url", App.my_IP);
		HttpResponse<JsonNode> jsonResponse = Unirest.post(IP + "/tavern").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############postTavern#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############postTavern#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}
	private static void putTavern(String capabilities,String IP) throws UnirestException{
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject jo1 = new JSONObject();
		jo1.put("heroclass", "heroclasse");
		jo1.put("capabilities",capabilities);
		jo1.put("url", App.my_IP);
		HttpResponse<JsonNode> jsonResponse2 = Unirest.put(IP + "/tavern").body(jo1).asJson();
		JSONObject jsonObj2 = jsonResponse2.getBody().getObject();
		System.out.println("############putTavern#############\n");
		System.out.println(jsonObj2 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############putTavern#############\n";
		ausgabe +=jsonObj2+ "\n";
		ausgabe +="########################################\n";
	}

	private static void getTavern(String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		HttpResponse<JsonNode> jsonResponse = Unirest.get(IP + "/tavern").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############getTavern#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############getTavern#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}

	private static void createGroupwithIP(String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP + "/tavern/groups").asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############createGroupwithIP#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############createGroupwithIP#############\n";
		ausgabe +=jsonObj1+ "\n";
		ausgabe +="########################################\n";
	}
	
	private static void notjoinGroupwithIP(String groupnr,String message,String IP) throws UnirestException{
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject json = new JSONObject();
		json.put("message", message);
		HttpResponse<JsonNode> jsonResponse3 = Unirest.post(IP + "/tavern/groups/"+groupnr).body(json).asJson();
		JSONObject jsonObj3 = jsonResponse3.getBody().getObject();
		System.out.println("############notjoinGroupwithIP#############\n");
		System.out.println(jsonObj3 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############notjoinGroupwithIP#############\n";
		ausgabe +=jsonObj3+ "\n";
		ausgabe +="########################################\n";
	}
	
	private static void joinGroupwithIP(String groupnr,String IP) throws UnirestException{
		if(IP.equals("")){
			IP = my_IP;
		}
		HttpResponse<JsonNode> jsonResponse4 = Unirest.post(IP + "/tavern/groups/"+groupnr).asJson();
		JSONObject jsonObj4 = jsonResponse4.getBody().getObject();
		System.out.println("############joinGroupwithIP#############\n");
		System.out.println(jsonObj4 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############joinGroupwithIP#############\n";
		ausgabe +=jsonObj4+ "\n";
		ausgabe +="########################################\n";
	}

	private static void postHirings(String groupuri,String questbeschreibung,String message,String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject jo = new JSONObject();
		jo.put("group", groupuri);
		jo.put("quest", questbeschreibung);
		jo.put("message", message);
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP + "/hirings").body(jo).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############postHirings#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############postHirings#############\n";
		ausgabe +=jsonObj1+ "\n";
		ausgabe +="########################################\n";
	}
	
	private static void postAssignments(String task,String resource,String method,String data,String message,String callback,String IP) throws UnirestException {
		String uri="";
		if(IP.equals("")){
			IP = my_IP;
		}
		if(callback.equals("yes")){
			uri ="/assignments/callback";
		}else{
			uri = "/assignments";
		}
		JSONObject jo = new JSONObject();
		jo.put("id", "jannikb");
		jo.put("task", task);
		jo.put("resource", resource);
		jo.put("method", method);
		jo.put("data", data);
		if(callback.equals("yes")){
			jo.put("user",App.my_IP);
		}else{
			jo.put("callback", "/assignments/callback");
		}
		jo.put("message", message);
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP + uri).body(jo).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############post"+uri+"#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############post"+uri+"#############\n";
		ausgabe +=jsonObj1 + "\n";
		ausgabe +="####################################\n";
	}

	private static void testalles() throws UnirestException {
		ausgabe = "";

		/*System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("Login\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "Login\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		login("jannikb","jannikb");*/


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showQuestList\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "showQuestList\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		showQuestsList();


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showTaskDetails\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "showTaskDetails\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		showTaskDetails("1");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMap\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "showMap\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		showMap();


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMapInfo Throneroom\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "showMapInfo Throneroom\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		showMapInfo("Throneroom");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("gotoThroneroom\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "gotoThroneroom\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		gotoLocation("visits","Throneroom");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliver\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "deliver\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		deliver("","1");
	}

	private static void completeQuestTwo() throws UnirestException {
		testalles2();
	}

	private static void testalles2() throws UnirestException {
		ausgabe = "";

		/*System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("Login\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="Login\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		login("jannikb","jannikb");*/


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showTaskDetails\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+= "showTaskDetails\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		showTaskDetails("3");


		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="showMap\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMap\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		showMap();


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showQuests\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="showQuestList\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		showQuestsList();


		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="showMapInfo Dungeon\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMapInfo Dungeon\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		showMapInfo("Dungeon");


		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="checkDungeon\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkDungeon\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		checkLocation("floor_u1", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkDungeonRats\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="checkDungeonRats\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		checkLocation("floor_u1/rats", "Dungeon");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkRat1\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="checkRat1\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		checkLocation("floor_u1/rats/1", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("killRat1\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="killRat1\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		kill("/floor_u1/rats/","Dungeon","1");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkRat2\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="checkRat2\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		checkLocation("floor_u1/rats/2", "Dungeon");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("killRat2\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="killRat2\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		kill("/floor_u1/rats/","Dungeon","2");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkRat3\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="checkRat3\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		checkLocation("floor_u1/rats/3", "Dungeon");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("killRat3\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="killRat3\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		kill("/floor_u1/rats/","Dungeon","3");


		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliverRatsInDungeon\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="deliverRatsInDungeon\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		deliverRatsInDungeon("Dungeon");



		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliverRat\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe+="-----------------------------------------------------------------------------------\n";
		ausgabe+="deliverRat\n";
		ausgabe+="-----------------------------------------------------------------------------------\n";

		deliver("Token:Kill rats","1");
	}

	private static void registerUser(String username, String password) throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("name", username);
		jo.put("password", password);
		System.out.println(jo);
		JsonNode jsonResponse = Unirest.post("http://" + blackboard_IP + ":" + blackboard_Port + "/users").body(jo)
				.asJson().getBody();
		System.out.println(jsonResponse);
		loginFehlerAusgabe = jsonResponse.getObject().get("message").toString();
	}

	private static void login(String username, String password) throws UnirestException {
		String loginResponse = Unirest.get("http://" + blackboard_IP + ":" + blackboard_Port + "/login")
				.basicAuth(username, password).asString().getBody();
		System.out.println(loginResponse);

		String[] parts = loginResponse.split(","); // split to get the token
													// part
		String[] parts2 = parts[1].split("\""); // split to get the token
		// the unclean way (does work at the moment)
		String token = parts2[3];

		authenticationToken = token;
		// how its supposed to be (does not work at the moment)
		/*
		 * authenticationToken = loginResponse.substring(loginResponse
		 * .indexOf("\"token\": \"") + 1, loginResponse.indexOf("\""));
		 */
		System.out.println("Logged in. Authentication token: " + authenticationToken);
	}

	private static void completeQuestOne() throws UnirestException {
		testalles();
	}

	// gotoLocation LocationName=name Visit Location host/visits
	private static void gotoLocation(String resource, String name) throws UnirestException {
		// search for name and take the host of it
		if (locationMap.containsKey(name)) {
			HttpResponse<JsonNode> questResponse = Unirest.post("http://" + locationMap.get(name) + "/" + resource)
					.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken)
					.asJson();
			ausgabe+= questResponse.getBody().toString()+"\n";
			System.out.println(questResponse.getBody().toString());
			tokenMap.put(questResponse.getBody().getObject().get("token_name"),questResponse.getBody().getObject().get("token"));
		} else {
			System.out.println("Map does not contain: " + name);
		}
	}

	// gotoLocation LocationName=name Visit Location host/visits
	private static void checkLocation(String resource, String name) throws UnirestException {
		// search for name and take the host of it
		if (locationMap.containsKey(name)) {
			HttpResponse<JsonNode> locationResponse = Unirest
					.get("http://" + locationMap.get(name) + "/" + resource)
					.header("Accept", "application/json")
					.header("Authorization", "Token " + authenticationToken)
					.asJson();
			String locationString = locationResponse.getBody().toString();
			System.out.println(locationString);
			ausgabe+= locationResponse.getBody().toString()+"\n";
			System.out.println(locationResponse.getBody().toString());
		} else {
			System.out.println("Map does not contain: " + name);
		}
	}

	// UserDetails UserName=name "/users/{name}" Shows details about a single
	// user
	private static void showUserDetails(String name) {
		// TODO Auto-generated method stub

	}

	// UserList "/users" The list of users
	private static void showUserList() {
		// TODO Auto-generated method stub
	}

	private static void kill(String resource, String name,String id) throws UnirestException {
		HttpResponse<JsonNode> deliverResponse = Unirest
				.post("http://" + locationMap.get(name) + resource + id)
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken)
				.asJson();
		System.out.println(deliverResponse.getBody());
		ausgabe+=deliverResponse.getBody()+"\n";
		tokenMap.put(deliverResponse.getBody().getObject().get("token_name"),deliverResponse.getBody().getObject().get("token"));
	}


	// MapInfo MapName=name "/map/{name}" Information about a location on the
	// map
	private static void showMapInfo(String name) throws UnirestException {
		HttpResponse<JsonNode> locationResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/map/" + name).asJson();
		System.out.println(locationResponse.getBody().toString());
		String locationInfo = locationResponse.getBody().toString();
		System.out.println("Info of location: " + locationInfo);
		ausgabe+=locationInfo+"\n";
	}

	// Map "/map" Your friendly map, telling you where locations are found
	private static void showMap() throws UnirestException {
		locationMap = new HashMap<>();
		HttpResponse<JsonNode> locationResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/map").asJson();
		String locationString = locationResponse.getBody().toString();
		System.out.println(locationString);


		JSONArray jarr = new JSONArray();
		jarr = (JSONArray) locationResponse.getBody().getObject().get("objects");
		System.out.println(jarr);

		for (Object o:jarr){
			if(o instanceof JSONObject){
				locationMap.put(((JSONObject) o).get("name").toString(),((JSONObject) o).get("host").toString());
			}
		}
		ausgabe+=Arrays.asList(locationMap)+"\n";
		System.out.println(Arrays.asList(locationMap));
	}

	// TaskDetails detailsTaskID=id"/blackboard/tasks/{id} " Details about a
	// single task
	private static void showTaskDetails(String id) throws UnirestException {
		HttpResponse<JsonNode> taskResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/tasks/" + id)
				.header("Authorization", "Token " + authenticationToken).asJson();
		System.out.println(taskResponse.getBody());



		ausgabe+= taskResponse.getBody()+"\n";
	}

	// QuestTaskList tasklistQuestID=id "/blackboard/quests/{id}/tasks" Lists
	// the tasks to be fulfilled to solve the quest
	private static void showQuestTaskList(String id) throws UnirestException {
		HttpResponse<JsonNode> questResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + id + "/tasks")
				.asJson();

		JSONArray jarr = new JSONArray();
		jarr = (JSONArray) questResponse.getBody().getObject().get("tasks");
		System.out.println(jarr);

		ausgabe+=Arrays.asList(jarr)+"\n";
		System.out.println(Arrays.asList(jarr));
	}

	// detailsQuest detailsQuestID=id "/blackboard/quests/{id}" Shows details
	// about the quest
	private static void showQuestDetails(String id) {
		Body questResponse = Unirest.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + id)
				.getBody();
		System.out.println(questResponse.toString());
		String questInfo = questResponse.toString();
		System.out.println("Info of Quest [" + id + "]: " + questInfo);
	}

	// QuestsList "/blackboard/quests" Lists the quests available
	private static void showQuestsList() throws UnirestException {
		HttpResponse<JsonNode> questResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests").asJson();

		JSONArray jarr = new JSONArray();
		jarr = (JSONArray) questResponse.getBody().getObject().get("objects");
		System.out.println(jarr);

		for (Object o:jarr){
			if(o instanceof JSONObject){
				questList.put(((JSONObject) o).get("id").toString(),((JSONObject) o).get("name").toString());
			}
		}

		System.out.println(questList);
		ausgabe+=questList+"\n";

	}

	// DetailsDelivery DeliveryID=id"/blackboard/deliveries/{id}" Details about
	// a single delivery
	private static void showDeliveryDetails(String id) {
		// TODO Auto-generated method stub

	}

	// DeliveriesList "/blackboard/deliveries" Lists the deliveries
	private static void showDeliveriesList() {
		// TODO Auto-generated method stub

	}

	// DetailsDeliverable DeliverablesID=id "/blackboard/deliverables/{id}"
	// Details about a single deliverable
	private static void showDeliverableDetails(String id) {
		// TODO Auto-generated method stub
	}

	// Deliver DeliverablesID=id "/blackboard/quests/{id}/deliveries"
	// Details about a single deliverable
	private static void deliver(String tokenname, String id) throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("tokens", new JSONObject().put("/blackboard/tasks/"+id, tokenMap.get(tokenname)));
		System.out.println(jo);
		HttpResponse<JsonNode> deliverResponse = Unirest
				.post("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + id + "/deliveries")
				.header("Content-Type", "application/json").header("Authorization", "Token " + authenticationToken)
				.body(jo).asJson();
		System.out.println(deliverResponse.getBody());
		ausgabe+=deliverResponse.getBody()+"\n";
	}

	// Deliver DeliverablesID=id "/blackboard/quests/{id}/deliveries"
	// Details about a single deliverable
	private static void deliverRatsInDungeon(String name) throws UnirestException {
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put(tokenMap.get("Token:Rat Tail"));
		ja.put(tokenMap.get("Token:Rat Eye"));
		ja.put(tokenMap.get("Token:Rat Leg"));
		jo.put("tokens", ja);
		//jo.put("tokens", "["+tokenMap.get("Token:Rat Tail")+","+tokenMap.get("Token:Rat Eye")+","+tokenMap.get("Token:Rat Leg")+"]"); //vorher erstellen? und raus parsen
		System.out.println(jo);
		HttpResponse<JsonNode> deliverResponse = Unirest
				.post("http://" + locationMap.get(name) + "/floor_u1/rats")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken)
				.body(jo).asJson();
		System.out.println(deliverResponse.getBody());

		tokenMap.put(deliverResponse.getBody().getObject().get("token_name"),deliverResponse.getBody().getObject().get("token"));

		ausgabe+=deliverResponse.getBody()+"\n";
	}
}