package com.dutch_computer_technology.mySurveillance.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager extends Thread {
	
	private MySurveillance ms;
	private Map<String, List<byte[]>> buffers = new HashMap<String, List<byte[]>>();
	
	public FileManager(MySurveillance ms) {
		
		this.ms = ms;
		
	};
	
	public void addData(String name, byte[] data) {
		
		List<byte[]> buffer = new ArrayList<byte[]>();
		if (buffers.containsKey(name)) buffer = buffers.get(name);
		buffer.add(data);
		buffers.put(name, buffer);
		
	};
	
	public int totalBuffers() {
		
		int ammo = 0;
		for (String name : buffers.keySet()) {
			ammo += buffers.get(name).size();
		};
		return ammo;
		
	};
	
	private void write(String name, byte[] data, boolean append) {
		
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
	
	public void run() {
		
		while (ms.run) {
			
			for (String name : buffers.keySet()) {
				
				List<byte[]> buffer = buffers.get(name);
				if (buffer.size() == 0) continue;
				
				byte[] data = buffer.get(0).clone();
				buffer.remove(0);
				
				write(name, data, true);
				
			};
			
			try {
				
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			};
			
		};
		
	};
	
};