package com.dutch_computer_technology.mySurveillance.pages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentType;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Settings implements PageHandler {
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.HTML, "Admin privileges needed");
		
		Page page = new Page();
		page.setStatus(200);
		page.setContentType(ContentType.HTML);
		
		String html = new String(ms.website.loadFile(FileType.HTML, "settings"));
		
		html = ms.website.loadHTML(html);
		
		page.setData(html);
		return page;
		
	};
	
	public Page post(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.TEXT, "Admin privileges needed");
		
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
			
			Main.print(this, printType.INFO, "Admin " + user.getUsername() + " on IP: " + request.ip() + ", used the following command: " + cmd);
			
		} catch (ParseException e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData("Can't read data");
			return page;
		};
		
		return null;
		
	};
	
};