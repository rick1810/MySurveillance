package com.dutch_computer_technology.mySurveillance.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

public class FileManager extends Thread {
	
	private MySurveillance ms;
	private Map<String, byte[]> buffers = new HashMap<String, byte[]>();
	private Map<String, Map<String, Long>> files = new HashMap<String, Map<String, Long>>();
	
	public FileManager(MySurveillance ms) {
		
		this.ms = ms;
		
		System.out.println("Loading saved Files");
		JSONObject json = ms.getJSON("files.json");
		if (json == null) return;
		for (Object oCName : json.keySet()) {
			if (!(oCName instanceof String)) continue;
			String cName = (String) oCName;
			
			Object oJsonsCam = json.get(cName);
			if (!(oJsonsCam instanceof JSONObject)) continue;
			JSONObject jsonsCam = (JSONObject) oJsonsCam;
			for (Object oName : jsonsCam.keySet()) {
				if (!(oName instanceof String)) continue;
				String name = (String) oName;
				Object oTime = jsonsCam.get(name);
				long time = (Long) oTime;
				addFile(cName, name, time);
			};
			
		};
		System.out.println("Loaded saved Files");
		
	};
	
	@SuppressWarnings("unchecked")
	public void save() {
		
		JSONObject json = new JSONObject();
		for (String cName : files.keySet()) {
			Map<String, Long> cFiles = files.get(cName);
			JSONObject jsons = new JSONObject();
			for (String name : cFiles.keySet()) {
				jsons.put(name, cFiles.get(name));
			};
			json.put(cName, jsons);
		};
		ms.saveJSON("files.json", json);
		
	};
	
	private void addFile(String cName, String name) {
		
		synchronized(this) {
			Map<String, Long> cFiles = files.containsKey(cName) ? files.get(cName) : new HashMap<String, Long>();
			if (cFiles.containsKey(name)) return;
			
			long now = new Date().getTime();
			addFile(cName, name, now);
		};
		
	};
		
	private void addFile(String cName, String name, long time) {

		synchronized(this) {
			Map<String, Long> cFiles = files.containsKey(cName) ? files.get(cName) : new HashMap<String, Long>();
			cFiles.put(name, time);
			files.put(cName, cFiles);
		};
		
	};
	
	private long remFile(String cName, String name) {
		
		synchronized(this) {
			Map<String, Long> cFiles = files.containsKey(cName) ? files.get(cName) : new HashMap<String, Long>();
			cFiles.remove(name);
			if (cFiles.size() == 0) files.remove(cName);
			try {
				File file = new File(name);
				long size = 0;
				if (file.exists()) {
					size = file.length();
					file.delete();
				};
				return size;
			} catch (Exception e) {
				e.printStackTrace();
			};
			return 0;
		}
		
	};
	
	public void addData(String cName, String name, byte[] data) {
		
		synchronized(this) {
			addFile(cName, name);
			byte[] buffer = new byte[0];
			if (buffers.containsKey(name)) buffer = buffers.get(name);
			buffer = ByteManager.append(buffer, data);
			buffers.put(name, buffer);
		};
		
	};
	
	public int totalBuffers() {
		
		synchronized(this) {
			return buffers.size();
		}
		
	};
	
	private void write(String name, byte[] data, boolean append) {
		
		name = ms.savePath + "/" + name;
		String[] names = name.split("[\\\\/]");
		if (names.length > 1) {
			String path = "";
			for (int i = 0; i < names.length-1; i++) {
				path += names[i] + "/";
				File file = new File(path);
				if (!file.exists()) file.mkdir();
			};
		};
		
		try {
			File file = new File(name);
			OutputStream out = new FileOutputStream(file, append);
			if (append) out.write("\r\n".getBytes());
			out.write(data);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		};
		
	};
	
	private static int maxBytes = 2 * (1024 * 1024);
	private static int maxSize = 120; //In GB
	
	private long getSize(File dir) {
		
		long size = 0;
		File[] files = dir.listFiles();
		if (files == null) return size;
		
		for (File file : files) {
			
			if (file.isFile()) size += file.length();
			if (file.isDirectory()) size += getSize(file);
			
		};
		
		return size;
		
	};
	
	private long mapSize(String cName) { //In GB
		
		long size = 0;
		try {
			File file = new File(ms.savePath + "/" + cName);
			if (!file.exists()) return size;
			if (file.isDirectory()) size = getSize(file);
		} catch (Exception e) {
			e.printStackTrace();
		};
		return size;
		
	};
	
	public long toBytes(int gb) {
		
		return (long) gb * (1024 * 1024 * 1024);
		
	};
	
	public int toGB(long bytes) {
		
		return (int) bytes / (1024 * 1024 * 1024);
		
	};
	
	public void run() {
		
		while (ms.run) {
			
			synchronized(this) {
				
				List<String> bufferNames = new ArrayList<>(buffers.keySet());
				for (String name : bufferNames) {
					
					if (!buffers.containsKey(name)) continue;
					
					byte[] buffer = buffers.get(name);
					if (buffer.length == 0) {
						buffers.remove(name);
						continue;
					};
					
					byte[] data = ByteManager.between(buffer, 0, maxBytes);
					if (buffer.length > maxBytes) {
						buffer = ByteManager.between(buffer, data.length);
						buffers.put(name, buffer);
					} else {
						buffers.remove(name);
					};
					
					write(name, data, true);
					
				};
				
				long lMaxSize = toBytes(maxSize);
				
				for (String cName : files.keySet()) {
					
					long size = mapSize(cName);
					if (!(size > lMaxSize)) continue;
					
					Map<String, Long> cFiles = files.get(cName);
					List<Long> arr = new ArrayList<Long>();
					for (String name : cFiles.keySet()) {
						arr.add(cFiles.get(name));
					};
					
					Collections.sort(arr);
					
					int dels = 0;
					while (size > lMaxSize && dels < 1000 && arr.size() > 0) { //Don't delete too much in a single loop, everything has to wait for this!
						String name = null;
						long time = arr.get(0);
						for (String n : cFiles.keySet()) { //Not good, loop needed just to find the name.
							if (cFiles.get(n) == time) {
								name = n;
								break;
							};
						};
						arr.remove(0);
						if (name == null) continue;
						
						size -= remFile(cName, name);
					};
					
				};
				
			};
			
			try {
				
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			};
			
		};
		
	};
	
};