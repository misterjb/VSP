import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;

public class App {
	private static String test;

	public static void main(String[] args) {
		get("/", (request, response) -> {
			//curl -d '{"name":"jannikb", "password":"jannikb"}' -H "Content-Type: application/json" -X POST 172.19.0.3:5000/users
			//curl --user jannikb:jannikb 172.19.0.3:5000/login
			//curl 172.19.0.3:5000/whoami -H "Accept: application/json" -H 'Authorization: Token <tokenvalue>'
			//request.queryParams("txt_username");
			//request.queryParams("txt_password");
			Map<String, Object> model = new HashMap<>();			
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/login.vtl"));		
		});
		post("/login", (request, response) -> {
			//curl -d '{"name":"jannikb", "password":"jannikb"}' -H "Content-Type: application/json" -X POST 172.19.0.3:5000/users
			//curl --user jannikb:jannikb 172.19.0.3:5000/login
			//curl 172.19.0.3:5000/whoami -H "Accept: application/json" -H 'Authorization: Token <tokenvalue>'
			String LoginOderRegisterrequest = request.queryParams("btn");		
			test = request.queryParams("txt_username")+request.queryParams("txt_password")+LoginOderRegisterrequest;			
			response.redirect("/index");
			return null;				
		});	
		get("/index", (request, response) -> {
			//curl -d '{"name":"jannikb", "password":"jannikb"}' -H "Content-Type: application/json" -X POST 172.19.0.3:5000/users
			//curl --user jannikb:jannikb 172.19.0.3:5000/login
			//curl 172.19.0.3:5000/whoami -H "Accept: application/json" -H 'Authorization: Token <tokenvalue>'
			//request.queryParams("txt_username");
			//request.queryParams("txt_password");
			Map model = new HashMap<>();
			model.put("Ausgabe", test);			
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));		
		});
		post("/index", (request, response) -> {
			//curl -d '{"name":"jannikb", "password":"jannikb"}' -H "Content-Type: application/json" -X POST 172.19.0.3:5000/users
			//curl --user jannikb:jannikb 172.19.0.3:5000/login
			//curl 172.19.0.3:5000/whoami -H "Accept: application/json" -H 'Authorization: Token <tokenvalue>'
			//request.queryParams("txt_username");
			//request.queryParams("txt_password");
			Map model = new HashMap<>();
			test = request.queryParams("Quest");
			model.put("Ausgabe", test);			
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/index.vtl"));		
		});
	}
}