import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class App {
	private static String ausgabe = "";
	private static String blackboard_IP = "", blackboard_Port = "";
	private static String locationName = "";
	private static String locationHost = "";
	private static String authenticationToken = "";
	private static String my_IP = "172.19.0.24";
	private static String my_Group;
	private static ArrayList<String> questList = new ArrayList<>();
	private static String loginFehlerAusgabe = "";

	public static void main(String[] args) throws UnirestException {
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

				System.out.println("showQuestsList");
				System.out
						.println("-----------------------------------------------------------------------------------");
				showQuestsList();
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("showTaskDetails");
				System.out
						.println("-----------------------------------------------------------------------------------");
				// showTaskDetails("1");
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("showMap");
				System.out
						.println("-----------------------------------------------------------------------------------");
				showMap();
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("showMapInfo");
				System.out
						.println("-----------------------------------------------------------------------------------");
				showMapInfo("Throneroom");
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("gotoLocation");
				System.out
						.println("-----------------------------------------------------------------------------------");
				gotoLocation("Throneroom");
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("deliver");
				System.out
						.println("-----------------------------------------------------------------------------------");
				deliver("1");

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
				loginFehlerAusgabe="";
				System.out
						.println("-----------------------------------------------------------------------------------");
				System.out.println("login");
				System.out
						.println("-----------------------------------------------------------------------------------");
				login(request.queryParams("txt_username"), request.queryParams("txt_password"));
				System.out
						.println("-----------------------------------------------------------------------------------");
				ausgabe += "Erfolgreich eingeloggt";
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
			switch (request.queryParams("Quest")) {
			case "deliverableDetails":
				showDeliverableDetails(request.queryParams("deliverablesID"));
				break;
			case "deliveriesList":
				showDeliveriesList();
				break;
			case "deliveryDetails":
				showDeliveryDetails(request.queryParams("deliveryID"));
				break;
			case "questsList":
				showQuestsList();
				break;
			case "questDetails":
				showQuestDetails(request.queryParams("detailsQuestID"));
				break;
			case "questTaskList":
				showQuestTaskList(request.queryParams("tasklistQuestID"));
				break;
			case "taskDetails":
				showTaskDetails(request.queryParams("detailsTaskID"));
				break;
			case "map":
				showMap();
				break;
			case "mapInfo":
				showMapInfo(request.queryParams("MapName"));
				break;
			case "userList":
				showUserList();
				break;
			case "userDetails":
				showUserDetails(request.queryParams("UserName"));
				break;
			case "gotoLocation":
				gotoLocation(request.queryParams("locationName"));
				break;
			case "allinOne":
				completeQuestOne();
				break;
			}
			ausgabe = request.queryParams("Quest");
			Map model = new HashMap<>();
			model.put("Ausgabe", ausgabe);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
		// TODO Aufgabe 2
		post("/taverna", (request, response) -> {
			Gson gson = new Gson();
			Registration reg = gson.fromJson(request.body(), Registration.class);
			response.status(201);
			// TODO
			request.headers("Authorization:Token " + authenticationToken);
			return response;
		});
		get("/taverna", (request, response) -> {
			// TODO logintoken um richtigen user wiederzugeben
			String jsonString = new JSONObject().put("user", my_IP).put("idle", "false").put("group", my_Group)
					.put("hirings", "/hirings").put("assignments", "/assignments").put("messages", "/messages")
					.toString();
			response.type("application/json");
			response.status(200);
			return jsonString;
		});
		post("/taverna/hirings", (request, response) -> {
			Gson gson = new Gson();
			Groups reg = gson.fromJson(request.body(), Groups.class);
			// TODO
			request.headers("Authorization:Token" + authenticationToken);
			response.status(201);
			return response;
		});
		post("/taverna/assignments", (request, response) -> {
			Gson gson = new Gson();
			Assignments asmnt = gson.fromJson(request.body(), Assignments.class);
			response.status(201);
			// TODO
			request.headers("Authorization:Token " + authenticationToken);
			return response;
		});
	}

	/**
	 * geht wohl noch nicht!
	 * 
	 * @throws UnirestException
	 */
	private static void registerUser(String username, String password) throws UnirestException {
		JsonNode jsonResponse = Unirest.post("http://" + blackboard_IP + ":" + blackboard_Port + "/users")
				.field("name", username).field("password", password).asJson().getBody();
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

	private static void completeQuestOne() {
		// TODO Auto-generated method stub

	}

	// gotoLocation LocationName=name Visit Location host/visits
	private static void gotoLocation(String name) throws UnirestException {
		// search for name and take the host of it
		Unirest.get("http://" + "172.19.0.32:5000" + "/visits") // fixed host
																// for
																// throneroom
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken);

		System.out.println("ICH RASTE AUS!!");

		HttpResponse<JsonNode> questResponse = Unirest.post("http://" + "172.19.0.32:5000" + "/visits")
				.header("Accept", "application/json").header("Authorization", "Token " + authenticationToken).asJson();
		System.out.println(questResponse.getBody().toString());
		String questInfo = questResponse.getBody().toString();
		System.out.println("Info of location: " + questInfo);

		/*
		 * MultipartBody jsonResponse = Unirest.post("http://" + blackboard_IP +
		 * ":" + blackboard_Port + "/visits/" + name) .header("Accept",
		 * "application/json") .field("Authorization: Token ",
		 * authenticationToken) ;
		 */
		/*
		 * Unirest.post("http://" + blackboard_IP + ":" + blackboard_Port +
		 * "/visits/" + name) .header("Authorization: Token ",
		 * authenticationToken) .asJson();
		 */
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
	}

	// Map "/map" Your friendly map, telling you where locations are found
	private static void showMap() throws UnirestException {
		HttpResponse<JsonNode> locationResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/map").asJson();
		String locationString = locationResponse.getBody().toString();
		System.out.println(locationString);
		// shows first location

		locationHost = locationString.substring(locationString.indexOf("\"host\":\"") + 8,
				locationString.indexOf("\"", locationString.indexOf("\"host\":\"") + 8));
		System.out.println("Host of location: " + locationHost); // to " from
																	// "name":"
		locationName = locationString.substring(locationString.indexOf("\"name\":\"") + 8,
				locationString.indexOf("\"", locationString.indexOf("\"name\":\"") + 8));
		System.out.println("Name of location: " + locationName);
	}

	// TaskDetails detailsTaskID=id"/blackboard/tasks/{id} " Details about a
	// single task
	private static void showTaskDetails(String id) throws UnirestException {
		Body taskResponse = Unirest.get("http://" + blackboard_IP + ":" + blackboard_Port + "/tasks/" + id)
				.basicAuth("Authorization: Token ", authenticationToken).getBody();
		System.out.println(taskResponse.toString());
		String taskInfo = taskResponse.toString();
		System.out.println("Info of Task [" + id + "]: " + taskInfo);

		/*
		 * Unirest.post("http://" + blackboard_IP + ":" + blackboard_Port +
		 * "/blackboard/quests/" + id + "/deliveries")
		 * .header("Authorization: Token ", authenticationToken) .asJson();
		 */
	}

	// QuestTaskList tasklistQuestID=id "/blackboard/quests/{id}/tasks" Lists
	// the tasks to be fulfilled to solve the quest
	private static void showQuestTaskList(String id) {
		Body questResponse = Unirest
				.get("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests" + id + "/tasks")
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
		Unirest.post("http://" + blackboard_IP + ":" + blackboard_Port + "/blackboard/quests/" + id + "/deliveries")
				.header("Authorization: Token ", authenticationToken).asJson();
	}

	class Registration {
		String heroclass;
		String[] capabilities;
		String url;
	}

	class Message {
		String status;
		String type;
		String message;
	}

	class Groups {
		String group;
		String quest;
		Message message;

	}

	class Assignments {
		String id;
		String task;
		String resource;
		String method;
		String data;
		String callback;
		Message message;
	}

	class Election {
		String algorithm;
		String payload;
		String user;
		String job;
		Message message;
	}
}