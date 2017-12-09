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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static String locationName = "";
	private static String locationHost = "";
	private static String authenticationToken = "";
	private static HashMap<String, String> locationMap = new HashMap<>();
	private static ArrayList<String> questList = new ArrayList<>();
	private static String loginFehlerAusgabe = "";
	private static Object locationToken;
	public static String my_IP;
	private static int my_PORT = 4567;
	private static String my_Group = "11";
	private static ArrayList<Registration> registration_List = new ArrayList<>();
	private static ArrayList<Message> message_List = new ArrayList<>();
	private static int groupanzahl = 1;
	private static ArrayList<Group> group_List = new ArrayList<>();
	private static ArrayList<Hiring> hiring_List = new ArrayList<>();

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
		 * • heroclass: describe your trade – you may be creative here •
		 * capabilities: you have not earned any yet, so this is an empty
		 * string. But it will become a comma separated list of capabilities you
		 * have earned through solving assignments. • url: a fully qualified url
		 * to reach the player service!
		 */
		post("/taverna/user", (request, response) -> {
			Registration reg = new Gson().fromJson(request.body(), Registration.class);
			reg.setId(request.ip());
			registration_List.add(reg);
			response.type("application/json");
			response.status(201);
			response.body(new Gson().toJson(reg));
			return response.body();
		});
		put("/taverna/user", (request, response) -> {
			Registration regtemp;
			for (int i = 0; i < registration_List.size(); i++) {
				if (registration_List.get(i).getId().equals(request.ip())) {
					Registration reg = new Gson().fromJson(request.body(), Registration.class);
					registration_List.get(i).setCapabilities(reg.getCapabilities());
					response.body(new Gson().toJson(registration_List.get(i)));
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
		get("/taverna", (request, response) -> {
			JSONObject json = new JSONObject();
			json.put("user", my_IP);
			json.put("idle", "false");
			json.put("group", my_IP + "/taverna/groups/" + my_Group);
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
				for (String string : group_List.get(i).getMitglieder_List()) {
					if (string.equals(request.ip())) {
						group_List.get(i).setAssignment(asmnt);
					}
				}
			}
			response.status(201);
			response.body(new Gson().toJson(asmnt));
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
		post("/taverna/groups", (request, response) -> {
			group_List.add(new Group("" + groupanzahl));
			JSONObject json = new JSONObject();
			json.put("msg", "Gruppe wurde erstellt");
			json.put("groupnr", "" + groupanzahl);
			response.status(201);
			response.body(json.toString());
			groupanzahl++;
			return response.body();
		});
		get("/taverna/groups/:id", (request, response) -> {
			for (int i = 0; i < group_List.size(); i++) {
				if(group_List.get(i).getId()==request.params(":id")){
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
		post("/taverna/groups/:id", (request, response) -> {
			if (request.body().length() != 0) {
				response.status(406);
				response.body(request.body());
				return response.body();
			} else {
				int grpnr = Integer.parseInt(request.params(":id"));
				Group grp = group_List.get(grpnr - 1);
				JSONObject json = new JSONObject();
				json.put("msg", "Gruppe begetreten");
				json.put("groupnr", grp.getId());
				grp.addMitglieder(request.ip());
				response.status(202);
				response.body(json.toString());
				return response.body();
			}
		});
		post("/assignments/callback", (request, response) -> {
			Assignment asgmt = new Gson().fromJson(request.body(), Assignment.class);
			for (int i = 0; i < group_List.size(); i++) {
				if (group_List.get(i).getAssignment().getId() == asgmt.getId()) {
					group_List.get(i).getAssignment().updateAssigment(asgmt.getMethod(), asgmt.getData(),
							asgmt.getUser(), asgmt.getMessage());
				}
			}
			response.status(201);
			// TODO results & token zurückgeben
			response.body(request.body());
			return response.body();
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
						for (String string : group_List.get(i).getMitglieder_List()) {
							if (!string.equals(user)) {
								elec.setPayload("election");
								jsonResponseList.add(Unirest.post(string + "/election").body(new Gson().toJson(elec)).asJson());
								System.out.println("election an"+string +"geschickt");
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
					for (String string : tempgrp.getMitglieder_List()) {
						if (!string.equals(user)) {
							elec.setPayload("cordinator");
							HttpResponse<JsonNode> jsonResponse2 = Unirest.post(string + "/callback").body(new Gson().toJson(elec))
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
				gotoLocation(param);
				break;
			case "allinOne":
				completeQuestOne();
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
			case "postTaverna":
				if(Inputs.length==3){
					postTaverna(Inputs[0],Inputs[1],Inputs[2]);
				}else{
					postTaverna(Inputs[0],Inputs[1],"");
				}
				break;
			case "putTaverna":
				if(Inputs.length==2){
					putTaverna(Inputs[0],Inputs[1]);
				}else{
					putTaverna(param,"");
				}
				break;
			case "getTaverna":
				if(!param.equals("")){
					getTaverna(param);
				}else{
					getTaverna("");
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
			}
			System.out.println("Questparameter:"+request.queryParams("Quest"));
			System.out.println("Inputparameter:"+request.queryParams("Input"));
			Map model = new HashMap<>();
			model.put("Ausgabe", ausgabe);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
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

	private static void postTaverna(String heroclasse,String capabilities,String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject jo = new JSONObject();
		jo.put("heroclass", heroclasse);
		jo.put("capabilities", "");
		jo.put("url", App.my_IP);
		HttpResponse<JsonNode> jsonResponse = Unirest.post(IP + "/taverna/user").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############postTaverna#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############postTaverna#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}
	private static void putTaverna(String capabilities,String IP) throws UnirestException{
		if(IP.equals("")){
			IP = my_IP;
		}
		JSONObject jo1 = new JSONObject();
		jo1.put("heroclass", "heroclasse");
		jo1.put("capabilities",capabilities);
		jo1.put("url", App.my_IP);
		HttpResponse<JsonNode> jsonResponse2 = Unirest.put(IP + "/taverna/user").body(jo1).asJson();
		JSONObject jsonObj2 = jsonResponse2.getBody().getObject();
		System.out.println("############putTaverna#############\n");
		System.out.println(jsonObj2 + "\n");
		System.out.println("####################################\n");
		ausgabe +="############putTaverna#############\n";
		ausgabe +=jsonObj2+ "\n";
		ausgabe +="########################################\n";
	}

	private static void getTaverna(String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		HttpResponse<JsonNode> jsonResponse = Unirest.get(IP + "/taverna").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############getTaverna#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe +="############getTaverna#############\n";
		ausgabe +=jsonObj+ "\n";
		ausgabe +="########################################\n";
	}

	private static void createGroupwithIP(String IP) throws UnirestException {
		if(IP.equals("")){
			IP = my_IP;
		}
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP + "/taverna/groups").asJson();
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
		HttpResponse<JsonNode> jsonResponse3 = Unirest.post(IP + "/taverna/groups/"+groupnr).body(json).asJson();
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
		HttpResponse<JsonNode> jsonResponse4 = Unirest.post(IP + "/taverna/groups/"+groupnr).asJson();
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
		System.out.println("showQuestsList");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "showQuestsList\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		showQuestsList();
		System.out.println("-----------------------------------------------------------------------------------");
		System.out.println("showTaskDetails");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "showTaskDetails\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		showTaskDetails("1"); // geht noch nicht
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "showTaskDetails1\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		System.out.println("showMap");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "showMap\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		showMap(); // besseres parsen von name, host
		System.out.println("-----------------------------------------------------------------------------------");
		System.out.println("showMapInfo");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "showMapInfo\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		showMapInfo("Throneroom");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "showMapInfo Throneroom\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		System.out.println("gotoLocation");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "gotoLocation\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		gotoLocation("Throneroom");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "gotoThroneroom\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		System.out.println("deliver");
		System.out.println("-----------------------------------------------------------------------------------");
		ausgabe += "deliver\n";
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		deliver("1"); // geht noch nicht
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
	private static void gotoLocation(String name) throws UnirestException {
		// search for name and take the host of it
		if (locationMap.containsKey(name)) {
			HttpResponse<JsonNode> questResponse = Unirest.post("http://" + locationMap.get(name) + "/visits")
					.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken)
					.asJson();
			ausgabe += questResponse.getBody().toString() + "/n";
			System.out.println(questResponse.getBody().toString());
			locationToken = questResponse.getBody().getObject().get("token");
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

	// MapInfo MapName=name "/map/{name}" Information about a location on the
	// map
	private static void showMapInfo(String name) throws UnirestException {
		HttpResponse<JsonNode> locationResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/map/" + name).asJson();
		System.out.println(locationResponse.getBody().toString());
		String locationInfo = locationResponse.getBody().toString();
		System.out.println("Info of location: " + locationInfo);
		ausgabe += locationInfo + "/n";
	}

	// Map "/map" Your friendly map, telling you where locations are found
	private static void showMap() throws UnirestException {
		HttpResponse<JsonNode> locationResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/map").asJson();
		String locationString = locationResponse.getBody().toString();
		System.out.println(locationString);

		int i = 0;
		while (i < 3) { // nicht hinsehen ... wir benutzen keine magic
						// numbers!!!!!
			locationHost = locationString.substring(locationString.indexOf("\"host\":\"") + 8,
					locationString.indexOf("\"", locationString.indexOf("\"host\":\"") + 8));// to
																								// "
																								// from
			locationName = locationString.substring(locationString.indexOf("\"name\":\"") + 8,
					locationString.indexOf("\"", locationString.indexOf("\"name\":\"") + 8));

			locationMap.put(locationName, locationHost);

			int num = locationString.indexOf("\"", locationString.indexOf("\"name\":\"") + 8);
			String tmp = locationString.substring(num, locationString.length());
			locationString = tmp;
			i++;
		}
		ausgabe += Arrays.asList(locationMap) + "/n";
		System.out.println(Arrays.asList(locationMap));
	}

	// TaskDetails detailsTaskID=id"/blackboard/tasks/{id} " Details about a
	// single task
	private static void showTaskDetails(String id) throws UnirestException {
		HttpResponse<JsonNode> taskResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/tasks/" + id)
				.header("Authorization", "Token " + authenticationToken).asJson();
		System.out.println(taskResponse.getBody());
		ausgabe += taskResponse.getBody() + "/n";
	}

	// QuestTaskList tasklistQuestID=id "/blackboard/quests/{id}/tasks" Lists
	// the tasks to be fulfilled to solve the quest
	private static void showQuestTaskList(String id) {
		Body questResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + id + "/tasks")
				.getBody();
		String questtaskString = questResponse.toString();
		String tasks = questtaskString.substring(questtaskString.indexOf("\"tasks\": [") + 1,
				questtaskString.indexOf("]"));

		Pattern pattern = Pattern.compile("[/a-zA-Z0-9]+");
		Matcher matcher = pattern.matcher(tasks);
		ArrayList<String> questtaskList = new ArrayList<>();
		while (matcher.find()) {
			questtaskList.add(matcher.group());
		}
		int i = 1;
		for (String quest : questtaskList) {
			System.out.println("Quest " + i + " is: " + quest);
			i++;
		}

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
		String questString = questResponse.getBody().toString();
		System.out.println(questString);
		ausgabe += questString + "/n";
		String tasks = questString.substring(questString.indexOf("\"tasks\": [") + 1, questString.indexOf("]"));

		Pattern pattern = Pattern.compile("[/a-zA-Z0-9]+");
		Matcher matcher = pattern.matcher(tasks);
		while (matcher.find()) {
			questList.add(matcher.group());
		}
		int i = 1;
		for (String quest : questList) {
			System.out.println("Quest " + i + " is: " + quest);
			i++;
		}
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
	private static void deliver(String id) throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("tokens", new JSONObject().put("/blackboard/tasks/1", locationToken));
		System.out.println(jo);
		HttpResponse<JsonNode> deliverResponse = Unirest
				.post("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + id + "/deliveries")
				.header("Content-Type", "application/json").header("Authorization", "Token " + authenticationToken)
				.body(jo).asJson();
		System.out.println(deliverResponse.getBody());
		ausgabe += "-----------------------------------------------------------------------------------------\n";
		ausgabe += deliverResponse.getBody() + "/n";
		// ausgabe+= deliverResponse.getBody().getObject().
		// System.out.println(deliverResponse.getBody().getObject().get("message"));
		// System.out.println(deliverResponse.getBody().toString());
		// String deliveryInfo = deliverResponse.getBody().toString();
		// System.out.println("Info: " + deliveryInfo);
	}
}