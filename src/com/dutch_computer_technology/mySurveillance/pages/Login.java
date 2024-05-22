package com.dutch_computer_technology.mySurveillance.pages;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.Token;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.exceptions.JSONParse;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
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
			
			JSONObject json = new JSONObject(request.data());
			
			String username = json.getString("username");
			String password = json.getString("password");
			
			if (username == null || password == null) {
				Page page = new Page();
				page.setStatus(400);
				page.setData(ms.lang.get("data.incomplete"));
				return page;
			};
			
			User u = ms.accounts.getUser(username);
			if (u == null) {
				Main.print(this, printType.INFO, "Failed login for user " + username + " on IP: " + request.ip());
				Page page = new Page();
				page.setStatus(401);
				page.setData(ms.lang.get("credentials.incorrect"));
				return page;
			};
			
			if (!(ms.accounts.matsHash(u.getHash(), password, false))) {
				Main.print(this, printType.INFO, "Failed login for user " + username + " on IP: " + request.ip());
				Page page = new Page();
				page.setStatus(401);
				page.setData(ms.lang.get("credentials.incorrect"));
				return page;
			};
			
			Token token = new Token();
			u.addToken(token);
			
			ms.accounts.save();
			
			Main.print(this, printType.INFO, "User " + username + " logged in, on IP: " + request.ip());
			
			Page page = new Page();
			page.setData(ms.accounts.createStringToken(u.getUsername(), token));
			return page;
			
		} catch (JSONParse e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData(ms.lang.get("data.parse"));
			return page;
		}
		
	};
	
};