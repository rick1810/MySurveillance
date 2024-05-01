package com.dutch_computer_technology.mySurveillance.pages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dutch_computer_technology.mySurveillance.accounts.Token;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.Page;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;

public class Accounts implements PageHandler {
	
	public Page request(MySurveillance ms, String method, String url, JSONObject cookies, User user, String data) {
		
		if (method.equals("GET")) {
			
			
			
		} else if (method.equals("POST")) {
			
			try {
				
				JSONObject json = (JSONObject) new JSONParser().parse(data);
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
					return logout(ms, cookies, user, json);
				};
				
			} catch (ParseException e) {
				e.printStackTrace();
				Page page = new Page();
				page.setStatus(400);
				page.setData("Can't read data");
				return page;
			};
			
		};
		
		return null;
		
	};
	
	private Page logout(MySurveillance ms, JSONObject cookies, User user, JSONObject json) {
		
		Token token = ms.accounts.getToken(cookies);
		user.remToken(token.getTime());
		ms.accounts.save();
		
		Page page = new Page();
		page.setStatus(200);
		page.setData("Bye!");
		return page;
		
	};
	
};