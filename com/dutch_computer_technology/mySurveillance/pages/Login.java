package com.dutch_computer_technology.mySurveillance.pages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.Token;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentType;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Login implements PageHandler {
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		Page page = new Page();
		page.setStatus(200);
		page.setContentType(ContentType.HTML);
		
		String html = new String(ms.website.loadFile(FileType.HTML, "login"));
		html = ms.website.loadHTML(html);
		page.setData(html);
		return page;
		
	};
	
	public Page post(MySurveillance ms, Request request, User user) {
		
		try {
			
			JSONObject json = (JSONObject) new JSONParser().parse(request.data());
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
				Main.print(this, printType.INFO, "Failed login for user " + username + " on IP: " + request.ip());
				Page page = new Page();
				page.setStatus(401);
				page.setData("Incorrect Username/Password");
				return page;
			};
			
			if (!(ms.accounts.matsHash(u.getHash(), password, false))) {
				Main.print(this, printType.INFO, "Failed login for user " + username + " on IP: " + request.ip());
				Page page = new Page();
				page.setStatus(401);
				page.setData("Incorrect Username/Password");
				return page;
			};
			
			Token token = new Token();
			u.addToken(token);
			
			ms.accounts.save();
			
			Main.print(this, printType.INFO, "User " + username + " logged in, on IP: " + request.ip());
			
			Page page = new Page();
			page.setData(ms.accounts.createStringToken(u.getUsername(), token));
			return page;
			
		} catch (ParseException e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData("Can't read data");
			return page;
		}
		
	};
	
};