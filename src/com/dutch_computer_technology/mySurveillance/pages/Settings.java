package com.dutch_computer_technology.mySurveillance.pages;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.exceptions.JSONParse;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.languages.Lang.Language;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentType;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Settings implements PageHandler {
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.HTML, ms.lang.get("unauthorized.privileges"));
		
		Page page = new Page();
		page.setStatus(200);
		page.setContentType(ContentType.HTML);
		
		String html = new String(ms.website.loadFile(FileType.HTML, "settings"));
		
		html = ms.website.loadHTML(html);
		
		//Languages
		String languages = "";
		for (Language lang : Language.values()) {
			languages += "\"" + lang.toString() + "\",";
		};
		languages = languages.substring(0, languages.length() - 1);
		
		html = ms.website.replaceHTML(html, "<language>", ms.lang.getLanguage().toString());
		html = ms.website.replaceHTML(html, "<languageDisplayName>", ms.lang.getLanguage().displayName());
		html = ms.website.replaceHTML(html, "<languages>", languages);
		
		//Colors
		String primaryColor = Main.primaryColor;
		String secondaryColor = Main.secondaryColor;
		String tertiaryColor = Main.tertiaryColor;
		String inputColor = Main.inputColor;
		String inputBorderColor = Main.inputBorderColor;
		String bannerColor = Main.bannerColor;
		
		JSONObject color = ms.config.getJSONObject("color");
		if (color != null) {
			if (color.contains("primary")) primaryColor = color.getString("primary");
			if (color.contains("secondary")) secondaryColor = color.getString("secondary");
			if (color.contains("tertiary")) tertiaryColor = color.getString("tertiary");
			if (color.contains("input")) inputColor = color.getString("input");
			if (color.contains("inputBorder")) inputBorderColor = color.getString("inputBorder");
			if (color.contains("banner")) bannerColor = color.getString("banner");
		};
		
		html = ms.website.replaceHTML(html, "<primaryColor>", primaryColor);
		html = ms.website.replaceHTML(html, "<secondaryColor>", secondaryColor);
		html = ms.website.replaceHTML(html, "<tertiaryColor>", tertiaryColor);
		html = ms.website.replaceHTML(html, "<inputColor>", inputColor);
		html = ms.website.replaceHTML(html, "<inputBorderColor>", inputBorderColor);
		html = ms.website.replaceHTML(html, "<bannerColor>", bannerColor);
		
		page.setData(html);
		return page;
		
	};
	
	public Page post(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.TEXT, ms.lang.get("unauthorized.privileges"));
		
		try {
			
			JSONObject json = new JSONObject(request.data());
			
			String cmd = json.getString("cmd");
			if (cmd == null) {
				Page page = new Page();
				page.setStatus(400);
				page.setData(ms.lang.get("data.incomplete"));
				return page;
			};
			
			if (cmd.equals("saveLanguage")) {
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				Language lang = null;
				try {
					lang = Language.valueOf(name);
				} catch(IllegalArgumentException ignore) {};
				
				if (lang == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("settings.languageNotFound"));
					return page;
				};
				
				ms.lang.setLanguage(lang);
				ms.save();
				return new Page();
				
			};
			
			if (cmd.equals("resetLanguage")) {
				
				ms.lang.setLanguage(Main.defaultLanguage);
				ms.save();
				return new Page();
				
			};
			
			if (cmd.equals("setColor")) {
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				if (!(name.equals("primary") || name.equals("secondary") || name.equals("tertiary") || name.equals("input") || name.equals("inputBorder") || name.equals("banner"))) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("settings.colorNotFound"));
					return page;
				};
				
				String hex = json.getString("hex");
				if (hex != null) {
					if (!hex.startsWith("#")) hex = "#" + hex;
					if (hex.matches("^#([0-9A-Fa-f]{3}){1,2}$")) {
						JSONObject color = ms.config.getJSONObject("color");
						if (color == null) color = new JSONObject();
						color.put(name, hex);
						ms.config.put("color", color);
						ms.save();
						return new Page();
					};
					
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("settings.invalidHex"));
					return page;
					
				};
				
				Page page = new Page();
				page.setStatus(400);
				page.setData(ms.lang.get("data.incomplete"));
				return page;
				
			};
			
			if (cmd.equals("resetColor")) {
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				if (!(name.equals("primary") || name.equals("secondary") || name.equals("tertiary") || name.equals("input") || name.equals("inputBorder") || name.equals("banner"))) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("settings.colorNotFound"));
					return page;
				};
				
				JSONObject color = ms.config.getJSONObject("color");
				if (color == null) color = new JSONObject();
				color.delete(name);
				ms.config.put("color", color);
				ms.save();
				return new Page();
				
			};
			
			Main.print(this, printType.INFO, "Admin " + user.getUsername() + " on IP: " + request.ip() + ", used the following command: " + cmd);
			
		} catch (JSONParse e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData(ms.lang.get("data.parse"));
			return page;
		};
		
		return null;
		
	};
	
};