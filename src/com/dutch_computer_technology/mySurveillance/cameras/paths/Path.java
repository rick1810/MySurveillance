package com.dutch_computer_technology.mySurveillance.cameras.paths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;

public class Path {

	private MySurveillance ms;
	private PathType type;
	private Camera cam;
	private String path;
	
	public Path(MySurveillance ms, PathType type, Camera cam) {
		this.ms = ms;
		this.type = type;
		this.cam = cam;
	};
	
	public Path(MySurveillance ms, PathType type, Camera cam, JSONObject json) {
		this.ms = ms;
		this.type = type;
		this.cam = cam;
		this.path = json.getString("path");
	};
	
	public MySurveillance getMS() {
		return ms;
	};
	
	public PathType getType() {
		return type;
	};
	
	public Camera getCam() {
		return cam;
	};
	public void setCam(Camera cam) {
		this.cam = cam;
	};
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("type", type.toString());
		json.put("path", path);
		return json;
	};
	
	public String getPath() {
		return path;
	};
	public void setPath(String path) {
		this.path = path;
	};
	
	public String html() {
		return "<p>Raw Type</p>";
	};
	
	@Override
	public String toString() {
		return path;
	};
	
	public void write(String name, byte[] data, boolean append) { //Write to file at path
		
		String fullPath = path + Main.slash() + cam.getName() + Main.slash() + name;
		
		String[] paths = Main.files(fullPath);
		if (paths.length > 1) {
			String curPath = "";
			for (int i = 0; i < paths.length - 1; i++) {
				curPath += paths[i] + Main.slash();
				File file = new File(curPath);
				if (!file.exists()) file.mkdir();
			};
		};
		
		try {
			File file = new File(fullPath);
			OutputStream out = new FileOutputStream(file, append);
			if (append && file.exists()) out.write("\r\n".getBytes());
			out.write(data);
			out.close();
		} catch(Exception e) {
			Main.print(this, e);
		};
		
	};
	
	public File read(String name) {
		
		try {
			File file = new File(path + Main.slash() + cam.getName() + Main.slash() + name);
			if (!file.exists()) return null;
			return file;
		} catch(Exception e) {
			Main.print(this, e);
		};
		return null;
		
	};
	
	public List<File> files() { //Get files at path
		
		try {
			File dir = new File(path + Main.slash() + cam.getName());
			if (!dir.exists()) return new ArrayList<File>();
			return Arrays.asList(dir.listFiles());
		} catch(Exception e) {
			Main.print(this, e);
		};
		return new ArrayList<File>();
		
	};
	
	public boolean delete(String name) { //Remove file at path
		
		try {
			File file = new File(path + Main.slash() + cam.getName() + Main.slash() + name);
			if (!file.exists()) return true;
			return file.delete();
		} catch(Exception e) {
			Main.print(this, e);
		};
		return false;
		
	};
	
};