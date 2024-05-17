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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.Accounts;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.cameras.Cameras;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager;
import com.dutch_computer_technology.mySurveillance.website.Website;

public class MySurveillance {
	
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
	
	private JSONObject config;
	
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
			
			return (JSONObject) new JSONParser().parse(new String(data));
			
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
			writer.write(json.toJSONString());
			writer.close();
		} catch (Exception e) {
			Main.print(this, "Could not save JSON", e);
		};
		
	};
	
	@SuppressWarnings("unchecked")
	public void save() {
		
		JSONArray screensJ = new JSONArray();
		for (String screen : screens) {
			screensJ.add(screen);
		};
		config.put("screens", screensJ);
		saveJSON("config.json", config);
		
	};
	
	@SuppressWarnings("unchecked")
	public MySurveillance() throws URISyntaxException {
		
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
			config.put("save", Main.defaultSaveLocation);
			screens.add("");
			save();
			return;
		};
		Main.print(this, "Got config");
		
		this.port = config.containsKey("port") ? ((Long)config.get("port")).intValue() : Main.defaultPort;
		Main.print(this, "Port: " + port);
		
		if (config.containsKey("screens")) {
			Object screensO = config.get("screens");
			if (screensO instanceof JSONArray) {
				JSONArray screensA = (JSONArray) screensO;
				for (Object screenO : screensA) {
					if (!(screenO instanceof String)) continue;
					screens.add((String)screenO);
				};
			};
		} else {
			screens.add("");
		};
		
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
					Main.print(this, "FileManager crashed!", ((e instanceof Exception) ? (Exception)e : null));
				};
			});
		} catch(Exception e) {
			Main.print(this, "FileManager crashed!", e);
		};
		Main.print(this, "Created FileManager");
		
		//Cameras
		Main.print(this, "Creating Cameras");
		try {
			cameras = new Cameras(this);
		} catch(Exception e) {
			Main.print(this, "Cameras crashed!", e);
		};
		Main.print(this, "Created Cameras");
		
		fileManager.start();
		
		//StreamManager
		Main.print(this, "Creating StreamManager");
		try {
			streamManager = new StreamManager(this, cameras);
			streamManager.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Main.print(this, "StreamManager crashed!", ((e instanceof Exception) ? (Exception)e : null));
				};
			});
			streamManager.start();
		} catch(Exception e) {
			Main.print(this, "StreamManager crashed!", e);
		};
		Main.print(this, "Created StreamManager");
		
		//Website
		Main.print(this, "Creating Website");
		try {
			website = new Website(this, port);
			website.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Main.print(this, "Website crashed!", ((e instanceof Exception) ? (Exception)e : null));
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
					Main.print(this, "GuiManager crashed!", ((e instanceof Exception) ? (Exception)e : null));
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