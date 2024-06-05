package com.dutch_computer_technology.mySurveillance.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.Accounts;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.cameras.Cameras;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager;
import com.dutch_computer_technology.mySurveillance.json.JSONArray;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.languages.Lang;
import com.dutch_computer_technology.mySurveillance.website.Website;

public class MySurveillance {
	
	public Lang lang;
	public Accounts accounts;
	public Website website;
	public Cameras cameras;
	public FileManager fileManager;
	public StreamManager streamManager;
	public GuiManager guiManager;
	public List<String> screens = new ArrayList<String>();
	public boolean run = true;
	public int port;
	public String path;
	
	public JSONObject config;
	
	private MySurveillance ms;
	
	public JSONObject getJSON(String name) {
		
		if (path == null) return null;
		if (name == null) return null;
		
		try {
			
			File file = new File(path + name);
			if (!file.exists()) return null;
			
			InputStream is = new FileInputStream(file);
			
			byte[] data = new byte[0];
			do {
				data = ByteManager.append(data, (byte)is.read());
			} while (is.available() > 0);
			is.close();
			
			return new JSONObject(new String(data));
			
		} catch(Exception e) {
			Main.print(this, "Could not load JSON", e);
		};
		
		return null;
		
	};
	
	public void saveJSON(String name, JSONObject json) {
		
		if (path == null) return;
		if (name == null) return;
		
		try {
			File file = new File(path + name);
			FileWriter writer = new FileWriter(file);
			writer.write(json.toString());
			writer.close();
		} catch (Exception e) {
			Main.print(this, "Could not save JSON", e);
		};
		
	};
	
	public void save() {
		
		JSONArray screensJ = new JSONArray();
		for (String screen : screens) {
			screensJ.add(screen);
		};
		config.put("screens", screensJ);
		config.put("language", lang.getLanguage().toString());
		saveJSON("config.json", config);
		
	};
	
	public MySurveillance() throws URISyntaxException {
		
		this.ms = this;
		
		Main.print(this, "MySurveillance");
		Main.print(this, "Version: " + Main.version);
		
		this.path = "";
		try {
			String p = System.getProperty("user.dir");
			if (p != null) this.path = Main.path(p) + Main.slash();
		} catch(Exception ignore) {};
		Main.print(this, "Path: " + path);
		
		Main.print(this, "Getting config");
		config = getJSON("config.json");
		if (config == null) {
			Main.print(this, printType.WARNING, "No config, creating default");
			config = new JSONObject();
			config.put("port", Main.defaultPort);
			JSONObject color = new JSONObject();
			color.put("primary", Main.primaryColor);
			color.put("secondary", Main.secondaryColor);
			color.put("tertiary", Main.tertiaryColor);
			color.put("banner", Main.bannerColor);
			config.put("color", color);
			config.put("language", Main.defaultLanguage.toString());
			screens.add("");
			save();
			return;
		};
		Main.print(this, "Got config");
		
		this.port = config.contains("port") ? config.getInt("port") : Main.defaultPort;
		Main.print(this, "Port: " + port);
		
		if (config.contains("screens")) {
			JSONArray screensA = config.getJSONArray("screens");
			if (screensA != null) {
				for (Object screenO : screensA.objs()) {
					if (!(screenO instanceof String)) continue;
					screens.add((String)screenO);
				};
			};
		} else {
			screens.add("");
		};
		
		//Lang
		Main.print(this, "Creating Lang");
		try {
			lang = new Lang(config.getString("language"));
		} catch(Exception e) {
			Main.print(this, "Lang crashed!", e);
		};
		Main.print(this, "Created Lang");
		
		//Accounts
		Main.print(this, "Creating Accounts");
		try {
			accounts = new Accounts(this);
		} catch(Exception e) {
			Main.print(this, "Accounts crashed!", e);
		};
		Main.print(this, "Created Accounts");
		
		//FileManager
		Main.print(this, "Creating FileManager");
		try {
			fileManager = new FileManager(this);
			fileManager.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Main.print(ms, "FileManager crashed!", new Exception(e));
				};
			});
		} catch(Exception e) {
			Main.print(this, "FileManager crashed!", e);
		};
		Main.print(this, "Created FileManager");
		
		//StreamManager
		Main.print(this, "Creating StreamManager");
		try {
			streamManager = new StreamManager(this);
			streamManager.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Main.print(ms, "StreamManager crashed!", new Exception(e));
				};
			});
			streamManager.start();
		} catch(Exception e) {
			Main.print(this, "StreamManager crashed!", e);
		};
		Main.print(this, "Created StreamManager");
		
		//Cameras
		Main.print(this, "Creating Cameras");
		try {
			cameras = new Cameras(this);
		} catch(Exception e) {
			Main.print(this, "Cameras crashed!", e);
		};
		Main.print(this, "Created Cameras");
		
		fileManager.start();
		
		//Website
		Main.print(this, "Creating Website");
		try {
			website = new Website(this, port);
			website.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Main.print(ms, "Website crashed!", new Exception(e));
				};
			});
			website.start();
		} catch(IOException e) {
			Main.print(this, "Could not create Website", e);
		} catch(Exception e) {
			Main.print(this, "Website crashed!", e);
		};
		Main.print(this, "Created Website");
		
		//GuiManager
		Main.print(this, "Creating GuiManager");
		try {
			guiManager = new GuiManager(this);
			guiManager.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Main.print(ms, "GuiManager crashed!", new Exception(e));
				};
			});
			guiManager.start();
		} catch(Exception e) {
			Main.print(this, "GuiManager crashed!", e);
		};
		Main.print(this, "Created GuiManager");
		
		//Console
		while (run) {
			
			try {
				
				InputStream is = System.in;
				if (is.available() == 0) continue;
				byte[] raw = new byte[0];
				do {
					raw = ByteManager.append(raw, (byte)is.read());
				} while (is.available() > 0);
				
				String data = new String(raw);
				if (data.length() > 2) data = data.substring(0, data.length() - 2);
				
				if (data.equals("save") || data.equals("stop")) {
					Main.print(this, "Saving");
					accounts.save();
					cameras.save();
					save();
					if (data.equals("stop")) {
						Main.print(this, "Stopping");
						run = false;
					};
				} else if (data.equals("gui")) {
					if (guiManager != null) guiManager.kill();
					guiManager = new GuiManager(this);
					guiManager.start();
					Main.print(this, "Restarted guiManager");
				} else if (data.equals("radmin")) {
					User user = new User(Main.defaultUser, true, accounts.createHash(Main.defaultPassword));
					accounts.setUser(user);
					Main.print(this, "Recreated user " + Main.defaultUser + ".\nUsername: " + Main.defaultUser + "\nPassword: " + Main.defaultPassword);
				};
				
			} catch(IOException e) {
				Main.print(this, "Console Input/Output not working?", e);
			};
			
		};
		
		long time = new Date().getTime();
		long now = time;
		long timeout = (15)*1000;
		while (now-time > timeout) {};
		System.exit(0);
		
	};
	
};