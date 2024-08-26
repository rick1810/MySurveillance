package com.dutch_computer_technology.mySurveillance.cameras;

import java.util.ArrayList;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.exceptions.CameraParse;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Cameras {
	
	private MySurveillance ms;
	private List<Camera> cameras = new ArrayList<Camera>();
	
	public Cameras(MySurveillance ms) {
		
		this.ms = ms;
		
		Main.print(this, "Loading Cameras");
		JSONObject json = ms.getJSON("cameras.json");
		if (json == null) {
			Main.print(this, printType.WARNING, "No Cameras");
			return;
		};
		for (String key : json.keySet()) {
			try {
				Camera cam = new Camera(ms, json.getJSONObject(key));
				addCamera(cam);
			} catch(CameraParse e) {
				Main.print(this, "Could not load camera", e);
			};
		};
		Main.print(this, "Loaded Cameras");
		
	};
	
	public enum CameraType {
		IPCamera
	};
	
	public void save() {
		
		JSONObject json = new JSONObject();
		for (Camera cam : cameras) {
			json.put(cam.getName(), cam.toJSON());
		};
		
		ms.saveJSON("cameras.json", json);
		
	};
	
	public List<String> getCameras() {
		List<String> names = new ArrayList<String>();
		for (Camera cam : cameras) {
			names.add(cam.getName());
		};
		return names;
	};
	
	public int totalCameras() {
		return cameras.size();
	};
	
	public int totalCamerasOnline() {
		int ammo = 0;
		for (Camera cam : cameras) {
			if (cam.isOnline()) ammo++;
		};
		return ammo;
	};
	
	public int totalCamerasRunning() {
		int ammo = 0;
		for (Camera cam : cameras) {
			if (cam.isRunning()) ammo++;
		};
		return ammo;
	};
	
	public Camera getCamera(String name) {
		if (name == null) return null;
		for (Camera cam : cameras) {
			if (cam.getName().equals(name)) return cam;
		};
		return null;
	};
	
	public void addCamera(Camera cam) {
		setCamera(cam);
	};
	public void setCamera(Camera cam) {
		Camera oldCam = getCamera(cam.getName());
		if (oldCam != null) remCamera(cam.getName());
		cameras.add(cam);
	};
	
	public void remCamera(String name) {
		if (name == null) return;
		for (int i = 0; i < cameras.size(); i++) {
			if (cameras.get(i).getName().equals(name)) {
				cameras.remove(i);
				break;
			};
		};
	};
	
};