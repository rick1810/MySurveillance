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

public class Accounts implements PageHandler {
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.HTML, "Admin privileges needed");
		
		Page page = new Page();
		page.setStatus(200);
		page.setContentType(ContentType.HTML);
		
		String html = new String(ms.website.loadFile(FileType.HTML, "accounts"));
		
		String accounts = "";
		String usersJSON = "";
		for (String name : ms.accounts.getUsers()) {
			User us = ms.accounts.getUser(name);
			accounts += "<div class=\"user\"><p>" + name + "</p><div class=\"right\"><button class=\"edit\" onclick=\"openUser('" + name + "');\"><img src=\"settings.png\"/></button>" + (us.isAdmin() ? "<img src=\"admin.png\"/>" : "<img src=\"user.png\"/>") + "</div></div>";
			usersJSON += "\"" + name + "\":{\"isAdmin\":" + us.isAdmin() + "},";
		};
		usersJSON = usersJSON.substring(0, usersJSON.length() - 1);
		html = ms.website.replaceHTML(html, "<accounts>", accounts);
		
		html = ms.website.loadHTML(html);
		html = ms.website.replaceHTML(html, "<usersJSON>", usersJSON);
		
		page.setData(html);
		return page;
		
	};
	
	public Page post(MySurveillance ms, Request request, User user) {
		
		try {
			
			JSONObject json = (JSONObject) new JSONParser().parse(request.data());
			if (!json.containsKey("cmd")) {
				Page page = new Page();
				page.setStatus(400);
				page.setData("Data incomplete");
				return page;
			};
			
			Object oCmd = json.get("cmd");
			if (!(oCmd instanceof String)) {
				Page page = new Page();
				page.setStatus(400);
				page.setData("Data incomplete");
				return page;
			};
			
			String cmd = (String) oCmd;
			if (cmd.equals("logout")) {
				Main.print(this, printType.INFO, "User " + user.getUsername() + " logged out, on IP: " + request.ip());
				return logout(ms, request, user);
			};
			
			if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.TEXT, "Admin privileges needed");
			
			Main.print(this, printType.INFO, "Admin " + user.getUsername() + " on IP: " + request.ip() + ", used the following command: " + cmd);
			
			if (cmd.equals("remove")) {
				
				Object oUsername = json.get("username");
				if (!(oUsername instanceof String)) {
					Page page = new Page();
					page.setStatus(400);
					page.setData("Data incomplete");
					return page;
				};
				
				String username = (String)oUsername;
				
				Main.print(this, printType.INFO, "Removed user " + username);
				ms.accounts.remUser(username);
				ms.accounts.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("save")) {
				
				Object oUsername = json.get("username");
				if (!(oUsername instanceof String)) {
					Page page = new Page();
					page.setStatus(400);
					page.setData("Data incomplete");
					return page;
				};
				
				String username = (String) oUsername;
				User us = ms.accounts.getUser(username);
				if (us == null) {
					Main.print(this, printType.INFO, "Created user " + username);
					us = new User(username, false, null);
				};
				
				String userUsername = user.getUsername();
				
				Object oNUsername = json.get("newUsername");
				if (oNUsername instanceof String) {
					String newUsername = (String)oNUsername;
					if (ms.accounts.getUser(newUsername) != null) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Username already in use");
						return page;
					};
					Main.print(this, printType.INFO, "Renamed user " + username + " to " + newUsername);
					us.setUsername(newUsername);
				};
				
				Object oPassword = json.get("password");
				if (oPassword instanceof String) {
					Main.print(this, printType.INFO, "Changed password for user " + username);
					us.setHash(ms.accounts.createHash((String)oPassword));
				};
				
				Object oAdmin = json.get("isAdmin");
				if (oAdmin instanceof Boolean) {
					boolean isAdmin = (boolean)oAdmin;
					if (isAdmin) {
						Main.print(this, printType.INFO, "Made user " + username + " a admin");
					} else {
						Main.print(this, printType.INFO, "Removed admin from user " + username);
					};
					us.isAdmin(isAdmin);
				};
				
				us.clearTokens();
				ms.accounts.setUser(us);
				
				Page page = new Page();
				if (userUsername.equals(username)) {
					Token token = new Token();
					us.addToken(token);
					page.setData(ms.accounts.createStringToken(us.getUsername(), token));
				};
				
				ms.accounts.save();
				
				return page;
				
			};
			
		} catch (ParseException e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData("Can't read data");
			return page;
		};
		
		return null;
		
	};
	
	private Page logout(MySurveillance ms, Request request, User user) {
		
		Token token = ms.accounts.getToken(request.cookies());
		user.remToken(token.getTime());
		ms.accounts.save();
		
		return new Page();
		
	};
	
};