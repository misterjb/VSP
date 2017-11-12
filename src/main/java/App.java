import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;

public class App {
	private static String test;
	private static HashMap model;

	public static void main(String[] args) {
		get("/", (request, response) -> {
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
			test = request.queryParams("txt_username") + request.queryParams("txt_password") + LoginOderRegisterrequest;
			response.redirect("/index");
			return null;
		});
		get("/index", (request, response) -> {
			Map model = new HashMap<>();
			model.put("Ausgabe", test);
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));
		});
		post("/index", (request, response) -> {
			if (request.queryParams("Quest").equals("DetailsDeliverable")) {
				DetailsDeliverable(request.queryParams("DeliverablesID"));
			} else if (request.queryParams("Quest").equals("DeliveriesList")) {
				DeliveriesList();
			} else if (request.queryParams("Quest").equals("DetailsDelivery")) {
				DetailsDelivery(request.queryParams("DeliveryID"));
			} else if (request.queryParams("Quest").equals("QuestsList")) {
				QuestsList();
			} else if (request.queryParams("Quest").equals("detailsQuest")) {
				detailsQuest(request.queryParams("detailsQuestID"));
			} else if (request.queryParams("Quest").equals("QuestTaskList")) {
				QuestTaskList(request.queryParams("tasklistQuestID"));
			} else if (request.queryParams("Quest").equals("TaskDetails")) {
				TaskDetails(request.queryParams("detailsTaskID"));
			} else if (request.queryParams("Quest").equals("Map")) {
				Map();
			} else if (request.queryParams("Quest").equals("MapInfo")) {
				MapInfo(request.queryParams("MapName"));
			} else if (request.queryParams("Quest").equals("UserList")) {
				UserList();
			} else if (request.queryParams("Quest").equals("UserDetails")) {
				UserDetails(request.queryParams("UserName"));
			} else if (request.queryParams("Quest").equals("gotoLocation")) {
				gotoLocation(request.queryParams("LocationName"));
			}
			model = new HashMap<>();
			test = request.queryParams("Quest");
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
	private static void UserDetails(String name) {
		// TODO Auto-generated method stub

	}

	// UserList "/users" The list of users
	private static void UserList() {
		// TODO Auto-generated method stub

	}

	// MapInfo MapName=name "/map/{name}" Information about a location on the
	// map
	private static void MapInfo(String name) {
		// TODO Auto-generated method stub

	}

	// Map "/map" Your friendly map, telling you where locations are found
	private static void Map() {
		// TODO Auto-generated method stub

	}

	// TaskDetails detailsTaskID=id"/blackboard/tasks/{id} " Details about a
	// single task
	private static void TaskDetails(String id) {
		// TODO Auto-generated method stub

	}

	// QuestTaskList tasklistQuestID=id "/blackboard/quests/{id}/tasks" Lists
	// the tasks to be fulfilled to solve the quest
	private static void QuestTaskList(String id) {
		// TODO Auto-generated method stub

	}

	// detailsQuest detailsQuestID=id "/blackboard/quests/{id}" Shows details
	// about the quest
	private static void detailsQuest(String id) {
		// TODO Auto-generated method stub

	}

	// QuestsList "/blackboard/quests" Lists the quests available
	private static void QuestsList() {
		// TODO Auto-generated method stub

	}

	// DetailsDelivery DeliveryID=id"/blackboard/deliveries/{id}" Details about
	// a single delivery
	private static void DetailsDelivery(String id) {
		// TODO Auto-generated method stub

	}

	// DeliveriesList "/blackboard/deliveries" Lists the deliveries
	private static void DeliveriesList() {
		// TODO Auto-generated method stub

	}

	// DetailsDeliverable DeliverablesID=id "/blackboard/deliverables/{id}"
	// Details about a single deliverable
	private static void DetailsDeliverable(String id) {
		// TODO Auto-generated method stub
	}
}