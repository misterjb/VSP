package com.haw.vsp;

import static org.junit.Assert.assertEquals;
import static spark.Spark.get;
import static spark.Spark.post;

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

public class App3 {
	private static String ausgabe = "";
	private static String blackboard_IP = "", blackboard_Port = "";
	private static String authenticationToken = "";
	private static HashMap<String, String> locationMap = new HashMap<>();
	private static String loginFehlerAusgabe = "";
	public static String my_IP;
	public static String halfofmy_IP;
	public static int my_PORT = 4569;
	private static ArrayList<Message> message_List = new ArrayList<>();
	private static ArrayList<Hiring> hiring_List = new ArrayList<>();
	private static ArrayList<Assignment> assignment_List = new ArrayList<>();
	private static ArrayList<Assignment> assignmentcallback_List = new ArrayList<>();
	private static HashMap<String, String> questList = new HashMap<>();
	private static HashMap<Object, Object> tokenMap = new HashMap<>();
	private static boolean local;
	private static boolean gotanswer;
	private static boolean cordinator;
	private static ArrayList<String> grouplist = new ArrayList<>();
	private static ArrayList<String> userlist = new ArrayList<>();
	private static HashMap<String, String> electionMap = new HashMap<>();
	private static boolean got_reply;
	private static String mutex_state = "released";
	private static int logical_time = 0;

	@SuppressWarnings("unused")
	public static void main(String[] args) throws UnirestException {
		Spark.threadPool(20);
		Spark.port(my_PORT);
		try {
			my_IP = InetAddress.getLocalHost().getHostAddress();
			halfofmy_IP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		if (!my_IP.contains("172")) {
			my_IP = "http://localhost:" + my_PORT;
			local = true;
			String IP_1 = "http://localhost:4567";
			String IP_2 = "http://localhost:4568";
			String IP_3 = "http://localhost:4569";
			grouplist.add(IP_1);
			grouplist.add(IP_2);
			grouplist.add(IP_3);
			userlist.add(IP_1);
			userlist.add(IP_2);
			userlist.add(IP_3);
			
		} else {
			my_IP = "http://" + my_IP + ":" + my_PORT;
			login("jannikb", "jannikb");
			grouplist = getUrlFromOurGroup();
		}
		System.out.println(my_IP);
		/*
		 * { "user":"<link to the registered user account>", "idle":<boolean,if
		 * you have no assignment currently>,
		 * "group":"<url to the group you are in>",
		 * "hirings":"<uri to which one may post to hire you for a group>",
		 * "assignments":"<uri to which one may post an assignment",
		 * "messages":"<uri to which one may post messages>",
		 * "election":"<uri to which one sends election messages to>" }
		 */
		get("/tavern", (request, response) -> {
			JSONObject json = new JSONObject();
			json.put("user", "/users/jannikb");
			json.put("idle", "false");
			json.put("group", "/taverna/groups/1461/members");
			json.put("hirings", "/hirings");
			json.put("assignments", "/assignments");
			json.put("messages", "/messages");
			json.put("election", "/election");
			json.put("mutex", "/mutex");
			json.put("mutexstate", "/mutexstate");
			response.type("application/json");
			response.status(200);
			response.body(json.toString());
			return response.body();
		});
		// def get_reply():
		// content = request.get_json()
		// msg = content['msg']
		// clock_reply = content['time']
		// if clock_reply > logical_time:
		// logical_time = clock_reply + 1
		// else:
		// logical_time += 1
		// if msg == 'reply-ok':
		// got_reply = True
		// return HTTP_200_OK
		post("/reply", (request, response) -> {
			JSONObject jo = new JSONObject(request.body());
			String msg = jo.get("msg").toString();
			int clock_reply =Integer.parseInt(jo.get("msg").toString());
			if(clock_reply>logical_time){
				logical_time= clock_reply+1;
			}else{
				logical_time+=1;
			}
			if(msg.equals("reply-ok")){
				got_reply = true;
			}
			response.status(200);
			return response;
		});
		post("/mutex", (request, response) -> {
			JSONObject jo = new JSONObject(request.body());
			String reply = jo.get("reply").toString();
			String usertmp = jo.get("user").toString();
			int user = Integer.parseInt(usertmp.substring(usertmp.lastIndexOf('.') + 1, usertmp.length()));
			int myipint = Integer.parseInt(halfofmy_IP.substring(my_IP.lastIndexOf('.') + 1, my_IP.length()));
			int time = Integer.parseInt(jo.get("time").toString());
			JSONObject jsonreply_ok = new JSONObject();
			jsonreply_ok.put("msg", "reply-ok");
			jsonreply_ok.put("time", logical_time);
			jsonreply_ok.put("reply", my_IP + "/reply");
			jsonreply_ok.put("user", my_IP);

			JSONObject jsonrequest = new JSONObject();
			jsonrequest.put("msg", "request");
			jsonrequest.put("time", logical_time);
			jsonrequest.put("reply", my_IP + "/reply");
			jsonrequest.put("user", my_IP);

			if (mutex_state.equals("held") || (mutex_state.equals("wanting")
					&& (logical_time > time || (logical_time == time && myipint >= user)))) {
				if (mutex_state.equals("wanting")) {
					get_all_user_names();
					for (int i = 0; i < userlist.size(); i++) {
						String tempurl = userlist.get(i);
						HttpResponse<JsonNode> request_msg = Unirest.post(tempurl + "/mutex").body(jsonrequest)
								.asJson();
						HttpResponse<JsonNode> reply_ok_msg = Unirest.post(reply).body(jsonreply_ok).asJson();
						logical_time += 1;
					}
				} else if (mutex_state.equals("held")) {
					HttpResponse<JsonNode> reply_ok_msg = Unirest.post(reply).body(jsonreply_ok).asJson();
					logical_time += 1;
				}
			} else if (mutex_state.equals("released")) {
				HttpResponse<JsonNode> reply_ok_msg = Unirest.post(reply).body(jsonreply_ok).asJson();
				logical_time += 1;
			}
			response.status(200);
			return response;
		});
		get("/mutexstate", (request, response) -> {
			JSONObject json = new JSONObject();
			json.put("status", my_IP);
			json.put("time",logical_time);
			response.type("application/json");
			response.status(200);
			response.body(json.toString());
			return response.body();
		});
		post("/hirings", (request, response) -> {
			Hiring hring = new Gson().fromJson(request.body(), Hiring.class);
			hiring_List.add(hring);
			response.status(201);
			response.body(new Gson().toJson(hring));
			return response.body();
		});
		get("/hirings", (request, response) -> {
			JSONObject json = new JSONObject();
			for (int i = 0; i < hiring_List.size(); i++) {
				json.put("" + i, new Gson().toJson(hiring_List.get(i)));
			}
			hiring_List.clear();
			response.status(201);
			response.body(json.toString());
			return response.body();
		});
		post("/assignments", (request, response) -> {
			Assignment asmnt = new Gson().fromJson(request.body(), Assignment.class);
			assignment_List.add(asmnt);
			response.status(201);
			response.body(new Gson().toJson(asmnt));
			return response.body();
		});
		post("/assignments/callback", (request, response) -> {
			Assignment asgmtcallback = new Gson().fromJson(request.body(), Assignment.class);
			assignmentcallback_List.add(asgmtcallback);
			response.status(201);
			response.body(request.body());
			return response.body();
		});
		get("/assignments", (request, response) -> {
			JSONObject json = new JSONObject();
			for (int i = 0; i < assignment_List.size(); i++) {
				json.put("" + i, new Gson().toJson(assignment_List.get(i)));
			}
			hiring_List.clear();
			response.status(200);
			response.body(new Gson().toJson(json));
			return response.body();
		});
		post("/messages", (request, response) -> {
			System.out.println(request.ip());
			System.out.println(request.port());
			Message msg = new Gson().fromJson(request.body(), Message.class);
			message_List.add(msg);
			response.status(201);
			response.body(new Gson().toJson(msg));
			return response.body();
		});
		get("/messages", (request, response) -> {
			JSONObject json = new JSONObject();
			for (int i = 0; i < message_List.size(); i++) {
				json.put("" + i, new Gson().toJson(message_List.get(i).getMessage()));
			}
			message_List.clear();
			response.status(201);
			response.body(json.toString());
			return response.body();
		});
		post("/election", (request, response) -> {
			if (cordinator || gotanswer) {
				response.body(request.body());
				response.status(200);
				return response.body();
			}

			Election elec = new Gson().fromJson(request.body(), Election.class);
			String ip = elec.getUser();
			String callback = elec.getJob().getCallback();
			elec.setPayload("ok");
			elec.getJob().setCallback("/callback");
			if (local) {
				elec.setUser(my_IP);
			} else {
				elec.setUser(halfofmy_IP);
			}
			// answer: confirm the entrance of an election message
			System.out.println("confirm the entrance from " + ip);
			HttpResponse<JsonNode> jsonResponse1 = Unirest.post(ip + callback).body(new Gson().toJson(elec)).asJson();

			// election
			Thread.sleep(5000);
			if (cordinator) {
				response.body(request.body());
				response.status(200);
				return response.body();
			}
			int userurl;
			if (local) {
				userurl = Integer.parseInt(my_IP.substring(my_IP.lastIndexOf(':') + 1, my_IP.length()));
				for (int i = 0; i < grouplist.size(); i++) {
					String grpusr = grouplist.get(i);
					if (!grouplist.get(i).equals(my_IP)) {
						int grpusrurl = Integer.parseInt(grouplist.get(i)
								.substring(grouplist.get(i).lastIndexOf(':') + 1, grouplist.get(i).length()));
						if (grpusrurl > userurl) {
							elec.setPayload("election");
							System.out.println("meine url: " + userurl + " election geschickt an: " + grpusr);
							try {
								electionMap.put(grpusr, "notok");
								HttpResponse<JsonNode> jsonResponse2 = Unirest.post(grpusr + "/election")
										.body(new Gson().toJson(elec)).asJson();
							} catch (Exception e) {
								System.out.println(grpusr + "NOT FOUND");
							}

						}
					}
				}

			} else {
				userurl = Integer.parseInt(my_IP.substring(my_IP.lastIndexOf('.') + 1, my_IP.length()));
				for (int i = 0; i < grouplist.size(); i++) {
					String grpusr = grouplist.get(i);
					if (!grouplist.get(i).equals(halfofmy_IP)) {
						int grpusrurl = Integer.parseInt(grouplist.get(i)
								.substring(grouplist.get(i).lastIndexOf('.') + 1, grouplist.get(i).length()));
						if (grpusrurl > userurl) {
							elec.setPayload("election");
							System.out.println("meine url: " + userurl + " election geschickt an: " + grpusr);
							try {
								electionMap.put(grpusr, "notok");
								HttpResponse<JsonNode> jsonResponse2 = Unirest.post(grpusr + "/election")
										.body(new Gson().toJson(elec)).asJson();
							} catch (Exception e) {
								System.out.println(grpusr + "NOT FOUND");
							}

						}
					}
				}
			}
			Thread.sleep(5000);
			if (cordinator || gotanswer) {
				response.body(request.body());
				response.status(200);
				return response.body();
			}

			for (Map.Entry<String, String> entry : electionMap.entrySet()) {
				if (entry.getValue().equals("ok")) {
					System.out.println("got answer from anyone");
					gotanswer = true;
				}
			}
			if (local && !gotanswer && electionMap.size() != 0) {
				for (String grpusr : grouplist) {
					int grpusrurl = Integer.parseInt(grpusr.substring(grpusr.lastIndexOf(':') + 1, grpusr.length()));
					if (!grpusr.equals(my_IP)) {
						if (grpusrurl < userurl) {
							System.out.println("juhu bin neuer cordinator");
							elec.setPayload("cordinator");
							HttpResponse<JsonNode> jsonResponse2 = Unirest.post(grpusr + "/callback")
									.body(new Gson().toJson(elec)).asJson();
							cordinator = true;
						}
					}
				}
			} else if (!gotanswer && electionMap.size() != 0) {
				for (String grpusr : grouplist) {
					int grpusrurl = Integer.parseInt(grpusr.substring(grpusr.lastIndexOf('.') + 1, grpusr.length()));
					if (!grpusr.equals(halfofmy_IP)) {
						if (grpusrurl < userurl) {
							System.out.println("juhu bin neuer cordinator");
							elec.setPayload("cordinator");
							HttpResponse<JsonNode> jsonResponse2 = Unirest.post(grpusr + "/callback")
									.body(new Gson().toJson(elec)).asJson();
							cordinator = true;
						}
					}
				}
			}
			response.body(request.body());
			response.status(200);
			return response.body();
		});

		post("/callback", (request, response) -> {
			Election elec = new Gson().fromJson(request.body(), Election.class);
			String ip = elec.getUser();
			JSONObject jo = new JSONObject(request.body());
			System.out.println(ip + " callback wird überprüft");
			if (jo.get("payload").equals("ok")) {
				System.out.println(ip + " callback OK");
				for (Map.Entry<String, String> entry : electionMap.entrySet()) {
					if (ip.equals(entry.getKey())) {
						entry.setValue("ok");
					}
				}
				response.body(request.body());
				response.status(200);
				return response.body();
			} else if (jo.get("payload").equals("cordinator")) {
				System.out.println(ip + " callback cordinator");
				cordinator = true;
				response.body(request.body());
				response.status(200);
				return response.body();
			} else {
				System.out.println(ip + " callback NOT");
				response.body(request.body());
				response.status(400);
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
			System.out.println(blackboard_IP);
			System.out.println(blackboard_Port);
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
			String param = "";
			if (request.queryParams("Input").contains(",")) {
				Inputs = request.queryParams("Input").split(",");
			} else {
				param = request.queryParams("Input");
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
				gotoLocation(Inputs[0], Inputs[1]);
				break;
			case "allinOne":
				completeQuestOne();
				break;
			case "allinOne2":
				completeQuestTwo();
				break;
			case "allinOne3":
				completeQuestThree();
				break;
			case "posttestmessages":
				posttestmessages();
				break;
			case "postHirings":
				postHirings(Inputs[0], Inputs[1], Inputs[2], Inputs[3]);
				break;
			case "gettestmessages":
				gettestmessages();
				break;
			case "getHirings":
				gethirings();
				break;
			case "createGroup":
				createGroup();
				break;
			case "notjoinGroup":
				notjoinGroup(Inputs[0], Inputs[1]);
				break;
			case "joinGroup":
				joinGroup(Inputs[0]);
				break;
			case "getGroup":
				getGroup(Inputs[0]);
				break;
			case "postAssignments":
				postAssignments(Inputs[0], Inputs[1], Inputs[2], Inputs[3], Inputs[4], Inputs[5], Inputs[6]);
				break;
			case "postmessage":
				postmessage(Inputs[0], Inputs[1], Inputs[2], Inputs[3]);
				break;
			case "Question":
				showquestions();
				break;
			case "getAssignments":
				getAssignments();
				break;
			case "postElection":
				if (Inputs.length == 9) {
					postElection(Inputs[0], Inputs[1], Inputs[2], Inputs[3], Inputs[4], Inputs[5], Inputs[6], Inputs[7],
							Inputs[8]);
				} else {
					postElection(Inputs[0], Inputs[1], Inputs[2], Inputs[3], Inputs[4], Inputs[5], Inputs[6], Inputs[7],
							"");
				}
				break;
			}
			System.out.println("Questparameter:" + request.queryParams("Quest"));
			System.out.println("Inputparameter:" + request.queryParams("Input"));
			Map model = new HashMap<>();
			model.put("Ausgabe", ausgabe);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
	}

	private static void get_all_user_names() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse1 = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/taverna/adventurers")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		JSONObject jsonNode1 = jsonResponse1.getBody().getObject();
		for (int i = 0; i < jsonNode1.getJSONArray("objects").length(); i++) {
			if(jsonNode1.getJSONArray("objects").getJSONObject(i).get("capabilities").toString().contains("mutex")){
				userlist.add(jsonNode1.getJSONArray("objects").getJSONObject(i).get("url").toString());
			}
		}
	}

	private static void postElection(String payload, String id, String task, String resource, String method,
			String data, String message1, String message2, String IP) throws UnirestException {
		if (IP.equals("")) {
			IP = my_IP;
		}
		JSONObject jo = new JSONObject();
		jo.put("algorithm", "bully algo");
		jo.put("payload", payload);
		jo.put("user", my_IP);
		System.out.println(my_IP);
		jo.put("job", new JSONObject().put("id", id).put("task", task).put("resource", resource).put("method", method)
				.put("data", data).put("callback", "/callback").put("message", message1));
		jo.put("message", message2);
		HttpResponse<JsonNode> jsonResponse = Unirest.post(IP + "/election").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############postElection#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe += "############postElection#############\n";
		ausgabe += jsonObj + "\n";
		ausgabe += "########################################\n";

	}

	private static void getAssignments() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse1 = Unirest.get(my_IP + "/assignments").asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		assertEquals(201, jsonResponse1.getStatus());
		System.out.println("############getAssignments#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe += "############getAssignments#############\n";
		ausgabe += jsonObj1 + "\n";
		ausgabe += "########################################\n";
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

	private static void postmessage(String status, String type, String message, String IP) throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("status", status);
		jo.put("type", type);
		jo.put("message", message);
		HttpResponse<JsonNode> jsonResponse = Unirest.post(IP + "/messages").body(jo).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############postmessage#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe += "############postmessage#############\n";
		ausgabe += jsonObj + "\n";
		ausgabe += "########################################\n";
	}

	private static void getGroup(String groupnr) throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/taverna/groups/" + groupnr + "/members")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############getGroup#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe += "############getGroup#############\n";
		ausgabe += jsonObj + "\n";
		ausgabe += "########################################\n";
	}

	private static ArrayList<String> getUrlFromOurGroup() throws UnirestException {
		ArrayList<String> groupUrls = new ArrayList<>();
		HttpResponse<JsonNode> jsonResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/taverna/groups/1461/members")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		for (int i = 0; i < jsonObj.getJSONArray("objects").length(); i++) {
			groupUrls.add(jsonObj.getJSONArray("objects").getJSONObject(i).get("url").toString());
		}
		return groupUrls;
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
		HttpResponse<JsonNode> jsonResponse = Unirest.get(my_IP + "/messages").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############gettestmessages#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe += "############gettestmessages#############\n";
		ausgabe += jsonObj + "\n";
		ausgabe += "########################################\n";
	}

	private static void gethirings() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.get(my_IP + "/hirings").asJson();
		JSONObject jsonObj = jsonResponse.getBody().getObject();
		System.out.println("############gethirings#############\n");
		System.out.println(jsonObj + "\n");
		System.out.println("####################################\n");
		ausgabe += "############gethirings#############\n";
		ausgabe += jsonObj + "\n";
		ausgabe += "########################################\n";
	}

	private static void createGroup() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse1 = Unirest
				.post("http://" + blackboard_IP + ":" + blackboard_Port + "/taverna/groups")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############createGroup#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe += "############createGroup#############\n";
		ausgabe += jsonObj1 + "\n";
		ausgabe += "########################################\n";
	}

	private static void notjoinGroup(String groupnr, String message) throws UnirestException {
		JSONObject json = new JSONObject();
		json.put("message", message);
		HttpResponse<JsonNode> jsonResponse3 = Unirest
				.post("http://" + blackboard_IP + ":" + blackboard_Port + "/taverna/groups/" + groupnr + "/members")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).body(json)
				.asJson();
		JSONObject jsonObj3 = jsonResponse3.getBody().getObject();
		System.out.println("############notjoinGroup#############\n");
		System.out.println(jsonObj3 + "\n");
		System.out.println("####################################\n");
		ausgabe += "############notjoinGroup#############\n";
		ausgabe += jsonObj3 + "\n";
		ausgabe += "########################################\n";
	}

	private static void joinGroup(String groupnr) throws UnirestException {
		HttpResponse<JsonNode> jsonResponse4 = Unirest
				.post("http://" + blackboard_IP + ":" + blackboard_Port + "/taverna/groups/" + groupnr + "/members")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		JSONObject jsonObj4 = jsonResponse4.getBody().getObject();
		System.out.println("############joinGroup#############\n");
		System.out.println(jsonObj4 + "\n");
		System.out.println("####################################\n");
		ausgabe += "############joinGroup#############\n";
		ausgabe += jsonObj4 + "\n";
		ausgabe += "########################################\n";
	}

	private static void postHirings(String groupuri, String questbeschreibung, String message, String IP)
			throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("group", groupuri);
		jo.put("quest", questbeschreibung);
		jo.put("message", message);
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP + "/hirings").body(jo).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############postHirings#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe += "############postHirings#############\n";
		ausgabe += jsonObj1 + "\n";
		ausgabe += "########################################\n";
	}

	private static void postAssignments(String task, String resource, String method, String data, String message,
			String callback, String IP) throws UnirestException {
		String uri = "";
		if (callback.equals("yes")) {
			uri = "/assignments/callback";
		} else {
			uri = "/assignments";
		}
		JSONObject jo = new JSONObject();
		jo.put("id", "jannikb");
		jo.put("task", task);
		jo.put("resource", resource);
		jo.put("method", method);
		jo.put("data", data);
		if (callback.equals("yes")) {
			jo.put("user", App.my_IP);
		} else {
			jo.put("callback", "/assignments/callback");
		}
		jo.put("message", message);
		HttpResponse<JsonNode> jsonResponse1 = Unirest.post(IP + uri).body(jo).asJson();
		JSONObject jsonObj1 = jsonResponse1.getBody().getObject();
		System.out.println("############post" + uri + "#############\n");
		System.out.println(jsonObj1 + "\n");
		System.out.println("####################################\n");
		ausgabe += "############post" + uri + "#############\n";
		ausgabe += jsonObj1 + "\n";
		ausgabe += "####################################\n";
	}

	private static void testalles() throws UnirestException {
		ausgabe = "";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("Login\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "Login\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		login("jannikb", "jannikb");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showQuestList\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showQuestList\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showQuestsList();

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showTaskDetails\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showTaskDetails\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showTaskDetails("1");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMap\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showMap\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showMap();

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMapInfo Throneroom\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showMapInfo Throneroom\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showMapInfo("Throneroom");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("gotoThroneroom\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "gotoThroneroom\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		gotoLocation("/visits", "Throneroom");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliver\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "deliver\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		deliver("", "1", "1");
	}

	private static void completeQuestTwo() throws UnirestException {
		testalles2();
	}

	private static void testalles2() throws UnirestException {
		ausgabe = "";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("Login\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "Login\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		login("jannikb", "jannikb");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showTaskDetails\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showTaskDetails\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showTaskDetails("3");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showMap\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMap\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		showMap();

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showQuests\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showQuestList\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showQuestsList();

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showMapInfo Dungeon\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMapInfo Dungeon\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		showMapInfo("Dungeon");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkDungeon\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkDungeon\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		checkLocation("/floor_u1", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkDungeonRats\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkDungeonRats\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		checkLocation("/floor_u1/rats", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkRat1\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkRat1\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		checkLocation("/floor_u1/rats/1", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("killRat1\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "killRat1\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		action("/floor_u1/rats/", "Dungeon", "1");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkRat2\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkRat2\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		checkLocation("/floor_u1/rats/2", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("killRat2\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "killRat2\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		action("/floor_u1/rats/", "Dungeon", "2");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkRat3\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkRat3\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		checkLocation("/floor_u1/rats/3", "Dungeon");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("killRat3\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "killRat3\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		action("/floor_u1/rats/", "Dungeon", "3");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliverRatsInDungeon\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "deliverRatsInDungeon\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put(tokenMap.get("Token:Rat Tail"));
		ja.put(tokenMap.get("Token:Rat Eye"));
		ja.put(tokenMap.get("Token:Rat Leg"));
		jo.put("tokens", ja);
		System.out.println(jo);

		deliverQuestInLocation("Dungeon", jo, "/floor_u1/rats");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliverRat\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "deliverRat\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		deliver("Token:Kill rats", "1", "1");
	}

	private static void completeQuestThree() throws UnirestException {
		testalles3();
	}

	// TODO
	private static void testalles3() throws UnirestException {
		ausgabe = "";

		User mage = new User("mage", "group", my_IP);
		User warrior = new User("warrior", "group", my_IP);

		// Group group = new Group("1");

		// group.addMitglied(mage);
		// group.addMitglied(warrior);

		Assignment ass1 = new Assignment("mage", "3", "/wounded", "/stretcher/handle/front", "", my_IP, "do the front");
		Assignment ass2 = new Assignment("warrior", "3", "/wounded", "/stretcher/handle/back", "", my_IP,
				"do the back");

		// group.addAssignment(ass1);
		// group.addAssignment(ass2);

		testWarrior(ass2);
		testMage(ass1);
	}

	private static void testWarrior(Assignment ass) throws UnirestException {
		System.out.println("LoginWarrior\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "LoginWarrior\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		login("LeeroyJenkins", "LeeroyJenkins");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showTaskDetails\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showTaskDetails\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showTaskDetails(ass.getTask());

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showMap\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showMap\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		showMap();

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("showQuests\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "showQuestList\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		showQuestsList();

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkWounded\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkWounded\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		checkLocation(ass.getResource(), "Throneroom");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("help\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "help\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		action(ass.getMethod(), "Throneroom", "");

		/*
		 * JSONObject jo = new JSONObject(); jo.put("id","warrior");
		 * jo.put("task",ass.getTask()); jo.put("resource",ass.getResource());
		 * jo.put("method","post on /wounded/stretcher/handle/back");
		 * jo.put("user",user.getUrl()); jo.put("message","his feet smell");
		 * HttpResponse<JsonNode> deliverResponse = Unirest .post("http://" +
		 * ass.getCallback() + ass.getResource()) .header("Content-Type",
		 * "application/json") .body(jo).asJson();
		 */
	}

	private static void testMage(Assignment ass) throws UnirestException {
		System.out.println("LoginMage\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "LoginMage\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		login("jannikb", "jannikb");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "checkWounded\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("checkWounded\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		checkLocation(ass.getResource(), "Throneroom");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("help\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "help\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		action(ass.getMethod(), "Throneroom", "");

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliverWounded\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "deliverWounded\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.put(tokenMap.get("Token:Bloody Handle front"));
		ja.put(tokenMap.get("Token:Bloody Handle back"));
		jo.put("tokens", ja);
		System.out.println(jo);

		deliverQuestInLocation("Throneroom", jo, ass.getResource());

		System.out.println("-----------------------------------------------------------------------------------\n");
		System.out.println("deliver\n");
		System.out.println("-----------------------------------------------------------------------------------\n");

		ausgabe += "-----------------------------------------------------------------------------------\n";
		ausgabe += "deliver\n";
		ausgabe += "-----------------------------------------------------------------------------------\n";

		deliver("Token:Carry a wounded person", "4", "3");
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
			HttpResponse<JsonNode> questResponse = Unirest.post("http://" + locationMap.get(name) + resource)
					.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken)
					.asJson();
			ausgabe += questResponse.getBody().toString() + "\n";
			System.out.println(questResponse.getBody().toString());
			tokenMap.put(questResponse.getBody().getObject().get("token_name"),
					questResponse.getBody().getObject().get("token"));
		} else {
			System.out.println("Map does not contain: " + name);
		}
	}

	// gotoLocation LocationName=name Visit Location host/visits
	private static void checkLocation(String resource, String name) throws UnirestException {
		// search for name and take the host of it
		if (locationMap.containsKey(name)) {
			HttpResponse<JsonNode> locationResponse = Unirest.get("http://" + locationMap.get(name) + resource)
					.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken)
					.asJson();
			String locationString = locationResponse.getBody().toString();
			System.out.println(locationString);
			ausgabe += locationResponse.getBody().toString() + "\n";
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

	private static void action(String resource, String name, String id) throws UnirestException {
		HttpResponse<JsonNode> deliverResponse = Unirest.post("http://" + locationMap.get(name) + resource + id)
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		System.out.println(deliverResponse.getBody());
		ausgabe += deliverResponse.getBody() + "\n";
		tokenMap.put(deliverResponse.getBody().getObject().get("token_name"),
				deliverResponse.getBody().getObject().get("token"));
	}

	// MapInfo MapName=name "/map/{name}" Information about a location on the
	// map
	private static void showMapInfo(String name) throws UnirestException {
		HttpResponse<JsonNode> locationResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/map/" + name).asJson();
		System.out.println(locationResponse.getBody().toString());
		String locationInfo = locationResponse.getBody().toString();
		System.out.println("Info of location: " + locationInfo);
		ausgabe += locationInfo + "\n";
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

		for (Object o : jarr) {
			if (o instanceof JSONObject) {
				locationMap.put(((JSONObject) o).get("name").toString(), ((JSONObject) o).get("host").toString());
			}
		}
		ausgabe += Arrays.asList(locationMap) + "\n";
		System.out.println(Arrays.asList(locationMap));
	}

	// TaskDetails detailsTaskID=id"/blackboard/tasks/{id} " Details about a
	// single task
	private static void showTaskDetails(String id) throws UnirestException {
		HttpResponse<JsonNode> taskResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/tasks/" + id)
				.header("Authorization", "Token " + authenticationToken).asJson();
		System.out.println(taskResponse.getBody());

		ausgabe += taskResponse.getBody() + "\n";
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

		ausgabe += Arrays.asList(jarr) + "\n";
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

		for (Object o : jarr) {
			if (o instanceof JSONObject) {
				questList.put(((JSONObject) o).get("id").toString(), ((JSONObject) o).get("name").toString());
			}
		}

		System.out.println(questList);
		ausgabe += questList + "\n";

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
	private static void deliver(String tokenname, String tasksId, String questId) throws UnirestException {
		JSONObject jo = new JSONObject();
		jo.put("tokens", new JSONObject().put("/blackboard/tasks/" + tasksId, tokenMap.get(tokenname)));
		System.out.println(jo);
		HttpResponse<JsonNode> deliverResponse = Unirest
				.post("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + questId
						+ "/deliveries")
				.header("Content-Type", "application/json").header("Authorization", "Token " + authenticationToken)
				.body(jo).asJson();
		System.out.println(deliverResponse.getBody());
		ausgabe += deliverResponse.getBody() + "\n";
	}

	// Deliver DeliverablesID=id "/blackboard/quests/{id}/deliveries"
	// Details about a single deliverable
	private static void deliverQuestInLocation(String name, JSONObject jo, String resource) throws UnirestException {
		HttpResponse<JsonNode> deliverResponse = Unirest.post("http://" + locationMap.get(name) + resource)
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).body(jo)
				.asJson();
		System.out.println(deliverResponse.getBody());

		tokenMap.put(deliverResponse.getBody().getObject().get("token_name"),
				deliverResponse.getBody().getObject().get("token"));
		ausgabe += deliverResponse.getBody() + "\n";
	}
}