package com.dutch_computer_technology.mySurveillance.pages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dutch_computer_technology.mySurveillance.accounts.Token;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentTypes;
import com.dutch_computer_technology.mySurveillance.website.Page;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Login implements PageHandler {
	
	public Page request(MySurveillance ms, String method, String url, JSONObject cookies, User user, String data) {
		
		if (method.equals("GET")) {
			
			Page page = new Page();
			page.setStatus(200);
			page.setContentType(ContentTypes.HTML);
			
			String html = new String(ms.website.loadFile(FileType.HTML, "login"));
			html = ms.website.loadHTML(html);
			page.setData(html);
			return page;
			
		} else if (method.equals("POST")) {
			
			try {
				
				JSONObject json = (JSONObject) new JSONParser().parse(data);
				if (!(json.containsKey("username") && json.containsKey("password"))) {
					Page page = new Page();
					page.setStatus(400);
					page.setData("Data incomplete");
					return page;
				};
				
				Object oUsername = json.get("username");
				Object oPassword = json.get("password");
				if (!(oUsername instanceof String && oPassword instanceof String)) {
					Page page = new Page();
					page.setStatus(400);
					page.setData("Data incomplete");
					return page;
				};
				
				String username = (String) oUsername;
				String password = (String) oPassword;
				
				User u = ms.accounts.getUser(username);
				if (u == null) {
					Page page = new Page();
					page.setStatus(401);
					page.setData("Incorrect Username/Password");
					return page;
				};
				
				if (!(ms.accounts.matsHash(u.getHash(), password, false))) {
					Page page = new Page();
					page.setStatus(401);
					page.setData("Incorrect Username/Password");
					return page;
				};
				
				Token token = new Token();
				u.addToken(token);
				
				ms.accounts.save();
				
				Page page = new Page();
				page.setStatus(200);
				page.setData(ms.accounts.createStringToken(u.getUsername(), token));
				return page;
				
			} catch (ParseException e) {
				e.printStackTrace();
				Page page = new Page();
				page.setStatus(400);
				page.setData("Can't read data");
				return page;
			}
			
		};
		
		return null;
		
	};
	
};