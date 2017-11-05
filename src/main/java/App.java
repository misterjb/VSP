import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;

public class App {
	public static void main(String[] args) {
		get("/", (request, response) -> {
			Map<String, Object> model = new HashMap<>();			
			return new VelocityTemplateEngine().render(new ModelAndView(model, "/hello.vtl"));		
		});
		
		get("/login", (request, response) -> {
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
			String test = request.queryParams("txt_username")+request.queryParams("txt_password");
			response.type("text/html");
			return "<html>"+
			"<header><title>This is title</title></header>"+
			"<body>"+
			"Hello"+test+ "world"+
			"</body>"+
			"</html>";
		});	
	}
}