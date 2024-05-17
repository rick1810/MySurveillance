package com.dutch_computer_technology.mySurveillance.main;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Path;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Absolute;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Relative;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.Disabled;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.MaxSize;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.Reason;

public class FileManager extends Thread {
	
	private MySurveillance ms;
	private List<Buffer> buffers = new ArrayList<Buffer>();
	
	class Buffer {
		public Camera cam;
		public String name;
		public byte[] data;
		public Buffer(Camera cam, String name, byte[] data) {
			this.cam = cam;
			this.name = name;
			this.data = data;
		};
		public boolean equals(Buffer buffer) {
			if (!buffer.cam.equals(cam)) return false;
			return buffer.name.equals(name);
		};
	};
	
	public FileManager(MySurveillance ms) {
		
		this.ms = ms;
		
	};
	
	public enum PathType {
		Relative,
		Absolute
	};
	
	public Path createPath(PathType type) {
		if (type == null) return null;
		switch(type) {
			case Absolute:
				return new Absolute(ms);
			case Relative:
				return new Relative(ms);
			default:
				break;
		};
		return null;
	};
	
	public Path createPath(PathType type, JSONObject json) {
		if (type == null) return null;
		switch(type) {
			case Absolute:
				return new Absolute(ms, json);
			case Relative:
				return new Relative(ms, json);
			default:
				break;
		};
		return null;
	};
	
	public enum ReasonType {
		MaxSize,
		Disabled
	};
	
	public Reason createReason(ReasonType type, Camera cam) {
		if (type == null) return null;
		switch(type) {
			case MaxSize:
				return new MaxSize(ms, cam);
			case Disabled:
				return new Disabled(ms, cam);
			default:
				break;
		};
		return null;
	};
	
	public Reason createReason(ReasonType type, Camera cam, JSONObject json) {
		if (type == null) return null;
		switch(type) {
			case MaxSize:
				return new MaxSize(ms, cam, json);
			case Disabled:
				return new Disabled(ms, cam, json);
			default:
				break;
		};
		return null;
	};
	
	public Path getPath(Camera cam, PathType type) {
		if (cam == null) {
			Path path = new Relative(ms); 
			path.setPath(Main.defaultSaveLocation);
			return path;
		};
		Path path = cam.getPath();
		if (path != null) {
			if (path.getType().equals(type)) {
				return path;
			};
		};
		return createPath(type);
	};
	
	public Reason getReason(Camera cam, ReasonType type) {
		if (cam == null) return new MaxSize(ms, null);
		Reason reason = cam.getReason();
		if (reason != null) {
			if (reason.getType().equals(type)) {
				return reason;
			};
		};
		return createReason(type, cam);
	};
	
	public void addData(Camera cam, String name, byte[] data) {
		
		synchronized(this) {
			for (Buffer buffer : buffers) {
				if (buffer.cam.equals(cam) && buffer.name.equals(name)) {
					byte[] bufData = new byte[0];
					if (buffer.data != null) bufData = buffer.data; //Can be null?
					buffer.data = ByteManager.append(bufData, data);
					return;
				};
			};
			buffers.add(new Buffer(cam, name, data));
		};
		
	};
	
	public int totalBuffers() {
		
		synchronized(this) {
			return buffers.size();
		}
		
	};
	
	private static int maxBytes = 2 * (1024 * 1024); //2MB
	
	public void run() {
		
		while (ms.run) {
			
			synchronized(this) {
				
				List<Buffer> removeBuffers = new ArrayList<Buffer>();
				for (Buffer buffer : buffers) {
					
					if (buffer.data == null || buffer.data.length == 0) {
						removeBuffers.add(buffer);
						continue;
					};
					
					byte[] data = ByteManager.between(buffer.data, 0, maxBytes);
					if (buffer.data.length > maxBytes) {
						buffer.data = ByteManager.between(buffer.data, data.length);
					} else {
						buffer.data = null;
					};
					
					Path path = buffer.cam.getPath();
					path.write(buffer.name, data, true);
					
				};
				for (Buffer buffer : removeBuffers) {
					for (int i = 0; i < buffers.size(); i++) {
						if (buffers.get(i).equals(buffer)) {
							buffers.remove(i);
							break;
						};
					};
				};
				
				for (String name : ms.cameras.getCameras()) {
					
					Camera cam = ms.cameras.getCamera(name);
					if (cam == null) continue;
					
					Reason reason = cam.getReason();
					if (reason == null) continue;
					
					if (reason.test()) reason.delete();
					
				};
				
			};
			
			try {
				
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Main.print(this, e);
			};
			
		};
		
	};
	
};