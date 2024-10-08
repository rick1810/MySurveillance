package com.dutch_computer_technology.mySurveillance.pages;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.Token;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.JSONManager.exception.JSONParseException;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentType;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Accounts implements PageHandler {
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.HTML, ms.lang.get("unauthorized.privileges"));
		
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
			
			JSONObject json = new JSONObject(request.data());
			
			String cmd = json.getString("cmd");
			if (cmd == null) {
				Page page = new Page();
				page.setStatus(400);
				page.setData(ms.lang.get("data.incomplete"));
				return page;
			};
			
			if (cmd.equals("logout")) {
				Main.print(this, printType.INFO, "User " + user.getUsername() + " logged out, on IP: " + request.ip());
				return logout(ms, request, user);
			};
			
			if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.TEXT, ms.lang.get("unauthorized.privileges"));
			
			Main.print(this, printType.INFO, "Admin " + user.getUsername() + " on IP: " + request.ip() + ", used the following command: " + cmd);
			
			if (cmd.equals("remove")) {
				
				String username = json.getString("username");
				if (username == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				Main.print(this, printType.INFO, "Removed user " + username);
				ms.accounts.remUser(username);
				ms.accounts.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("save")) {
				
				String username = json.getString("username");
				if (username == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				User us = ms.accounts.getUser(username);
				if (us == null) {
					Main.print(this, printType.INFO, "Created user " + username);
					us = new User(username, false, null);
				};
				
				String userUsername = user.getUsername();
				
				String newUsername = json.getString("newUsername");
				if (newUsername != null) {
					if (ms.accounts.getUser(newUsername) != null) {
						Page page = new Page();
						page.setStatus(400);
						page.setData(ms.lang.get("accounts.nameUsed"));
						return page;
					};
					Main.print(this, printType.INFO, "Renamed user " + username + " to " + newUsername);
					us.setUsername(newUsername);
				};
				
				String password = json.getString("password");
				if (password != null) {
					Main.print(this, printType.INFO, "Changed password for user " + username);
					us.setHash(ms.accounts.createHash(password));
				};
				
				if (json.contains("isAdmin")) {
					boolean isAdmin = json.getBoolean("isAdmin");
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
			
		} catch (JSONParseException e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData(ms.lang.get("data.parse"));
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