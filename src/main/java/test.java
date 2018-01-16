import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class test {

	public static void main(String[] args) {
		JSONObject jo = new JSONObject();
		jo.put("capabilities", "bully algo");
		jo.put("group", "payload");
		jo.put("heroclass", "my_IP");
		jo.put("url", "456");
		jo.put("user", "my_IP");
		
		JSONObject jo1 = new JSONObject();
		jo1.put("capabilities", "bully algo");
		jo1.put("group", "payload");
		jo1.put("heroclass", "my_IP");
		jo1.put("url", "123");
		jo1.put("user", "my_IP");
		
		JSONArray ja = new JSONArray();
		ja.put(jo1);
		ja.put(jo);
		
		JSONObject jo2 = new JSONObject();
		jo2.put("objects",ja);
		jo2.put("status","success");
		System.out.println(jo2);
		
		System.out.println(jo2.getJSONArray("objects").getJSONObject(0).get("url"));
		System.out.println(jo2.getJSONArray("objects").getJSONObject(1).get("url"));

		JSONObject json = new JSONObject();
		json.put("msg", "msgwerwerwer");
		json.put("time", 1);
		json.put("reply", "reply");
		json.put("user", "user");
		System.out.println(json.get("time").toString());
		
		String ip ="http://172.19.0.71:4567";
		int myipint = Integer.parseInt(ip.substring(ip.lastIndexOf('.') + 1, ip.lastIndexOf(':')));
		System.out.println(myipint);		
	}

}