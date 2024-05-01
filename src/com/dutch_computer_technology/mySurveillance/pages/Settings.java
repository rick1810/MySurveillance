package com.dutch_computer_technology.mySurveillance.pages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentTypes;
import com.dutch_computer_technology.mySurveillance.website.Page;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Settings implements PageHandler {
	
	public Page request(MySurveillance ms, String method, String url, JSONObject cookies, User user, String data) {
		
		if (method.equals("GET")) {
			
			Page page = new Page();
			page.setStatus(200);
			page.setContentType(ContentTypes.HTML);
			
			String html = new String(ms.website.loadFile(FileType.HTML, "settings"));
			
			html = ms.website.replaceHTML(html, "<path>", ms.savePath);
			html = ms.website.loadHTML(html);
			
			page.setData(html);
			return page;
			
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
				if (cmd.equals("setPath")) {
					
					Object oPath = json.get("path");
					if (!(oPath instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					String path = (String) oPath;
					ms.savePath = path;
					ms.save();
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
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
	
};