import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;

public class App {
	private static String test="";
	private static HashMap model;
	private static String BlackboardIP,BlackboardPort;
	
	public static void main(String[] args) {	
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
			      test += "IP: "+packet.getAddress().toString().substring(1)+"\n";
			      msg = msg.substring(msg.lastIndexOf(':') + 1,msg.length()-1);
			      test +="Port: "+msg+"\n";
			      
			      BlackboardPort= msg;
			      BlackboardIP= packet.getAddress().toString().substring(1);
			      
			      // Reset the length of the packet before reusing it.
			      packet.setLength(buffer.length);			        
			    } catch (Exception e) {
			      System.err.println(e);
			    }
			Map<String, Object> model = new HashMap<>();
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/login.vtl"));
		});
		post("/login", (request, response) -> {
			// curl -d '{"name":"jannikb", "password":"jannikb"}' -H
			// "Content-Type: application/json" -X POST 172.19.0.3:5000/users
			// curl --user jannikb:jannikb 172.19.0.3:5000/login
			// curl 172.19.0.3:5000/whoami -H "Accept: application/json" -H
			// 'Authorization: Token <tokenvalue>'
			String LoginOderRegisterrequest = request.queryParams("btn");
			test += request.queryParams("txt_username") + request.queryParams("txt_password") + LoginOderRegisterrequest;
			response.redirect("/index");
			return null;
		});
		get("/index", (request, response) -> {
			Map model = new HashMap<>();
			model.put("Ausgabe", test);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
		post("/index", (request, response) -> {
			if (request.queryParams("Quest").equals("deliverableDetails")) {
				showDeliverableDetails(request.queryParams("deliverablesID"));
			} else if (request.queryParams("Quest").equals("deliveriesList")) {
				showDeliveriesList();
			} else if (request.queryParams("Quest").equals("deliveryDetails")) {
				showDeliveryDetails(request.queryParams("deliveryID"));
			} else if (request.queryParams("Quest").equals("questsList")) {
				showQuestsList();
			} else if (request.queryParams("Quest").equals("questDetails")) {
				showQuestDetails(request.queryParams("detailsQuestID"));
			} else if (request.queryParams("Quest").equals("questTaskList")) {
				showQuestTaskList(request.queryParams("tasklistQuestID"));
			} else if (request.queryParams("Quest").equals("taskDetails")) {
				showTaskDetails(request.queryParams("detailsTaskID"));
			} else if (request.queryParams("Quest").equals("map")) {
				showMap();
			} else if (request.queryParams("Quest").equals("mapInfo")) {
				showMapInfo(request.queryParams("MapName"));
			} else if (request.queryParams("Quest").equals("userList")) {
				showUserList();
			} else if (request.queryParams("Quest").equals("userDetails")) {
				showUserDetails(request.queryParams("UserName"));
			} else if (request.queryParams("Quest").equals("gotoLocation")) {
				gotoLocation(request.queryParams("locationName"));
			}
			model = new HashMap<>();
			test += request.queryParams("Quest");
			model.put("Ausgabe", test);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
	}

	// gotoLocation LocationName=name Visit Location host/visits
	private static void gotoLocation(String name) {
		// TODO Auto-generated method stub

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
	private static void showMapInfo(String name) {
		// TODO Auto-generated method stub

	}

	// Map "/map" Your friendly map, telling you where locations are found
	private static void showMap() {
		// TODO Auto-generated method stub

	}

	// TaskDetails detailsTaskID=id"/blackboard/tasks/{id} " Details about a
	// single task
	private static void showTaskDetails(String id) {
		// TODO Auto-generated method stub

	}

	// QuestTaskList tasklistQuestID=id "/blackboard/quests/{id}/tasks" Lists
	// the tasks to be fulfilled to solve the quest
	private static void showQuestTaskList(String id) {
		// TODO Auto-generated method stub

	}

	// detailsQuest detailsQuestID=id "/blackboard/quests/{id}" Shows details
	// about the quest
	private static void showQuestDetails(String id) {
		// TODO Auto-generated method stub

	}

	// QuestsList "/blackboard/quests" Lists the quests available
	private static void showQuestsList() {
		// TODO Auto-generated method stub

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
}