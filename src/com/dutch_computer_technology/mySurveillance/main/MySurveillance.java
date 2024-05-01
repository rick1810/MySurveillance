package com.dutch_computer_technology.mySurveillance.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.accounts.Accounts;
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
	public String savePath;
	
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
			e.printStackTrace();
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
			e.printStackTrace();
		};
		
	};
	
	@SuppressWarnings("unchecked")
	public void save() {
		
		JSONArray screensJ = new JSONArray();
		for (String screen : screens) {
			screensJ.add(screen);
		};
		config.put("screens", screensJ);
		config.put("save", savePath);
		saveJSON("config.json", config);
		
	};
	
	@SuppressWarnings("unchecked")
	public MySurveillance() throws URISyntaxException {
		
		System.out.println("MySurveillance");
		System.out.println("Version: " + Main.version);
		this.path = "";
		System.out.println("Path: " + path);
		
		System.out.println("Getting config");
		config = getJSON("config.json");
		if (config == null) {
			System.out.println("No config, creating default");
			config = new JSONObject();
			config.put("port", 8090);
			config.put("save", "storage");
			screens.add("");
			save();
			return;
		};
		System.out.println("Got config");
		
		this.savePath = config.containsKey("save") ? ((String)config.get("save")) : "storage";
		System.out.println("Save Path: " + path + savePath);
		
		this.port = config.containsKey("port") ? ((Long)config.get("port")).intValue() : 8090;
		System.out.println("Port: " + port);
		
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
		System.out.println("Create Accounts");
		accounts = new Accounts(this);
		System.out.println("Created Accounts");
		
		//Cameras
		System.out.println("Create Cameras");
		cameras = new Cameras(this);
		System.out.println("Created Cameras");
		
		//FileManager
		System.out.println("Creating FileManager");
		fileManager = new FileManager(this);
		fileManager.start();
		System.out.println("Created FileManager");
		
		//StreamManager
		System.out.println("Creating StreamManager");
		streamManager = new StreamManager(this, cameras);
		streamManager.start();
		System.out.println("Created StreamManager");
		
		//Website
		System.out.println("Create Website");
		try {
			website = new Website(this, port);
			website.start();
		} catch (IOException e) {
			e.printStackTrace();
		};
		System.out.println("Created Website");
		
		//GuiManager
		System.out.println("Creating GuiManager");
		guiManager = new GuiManager(this);
		guiManager.start();
		System.out.println("Created GuiManager");
		
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
					System.out.println("Saving");
					accounts.save();
					cameras.save();
					save();
					if (data.equals("stop")) {
						System.out.println("Stopping");
						run = false;
					};
				} else if (data.equals("gui")) {
					if (guiManager != null) guiManager.kill();
					guiManager = new GuiManager(this);
					guiManager.start();
				};
				
			} catch(IOException e) {
				e.printStackTrace();
			};
			
		};
		
		long time = new Date().getTime();
		long now = time;
		long timeout = (15)*1000;
		while (now-time > timeout) {};
		System.exit(0);
		
	};
	
};