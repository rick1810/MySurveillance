package com.dutch_computer_technology.mySurveillance.cameras;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.exceptions.CameraParse;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Cameras {
	
	private MySurveillance ms;
	private List<Camera> cameras = new ArrayList<Camera>();
	
	public Cameras(MySurveillance ms) {
		
		this.ms = ms;
		
		System.out.println("Loading Cameras");
		JSONObject jsons = ms.getJSON("cameras.json");
		if (jsons == null) {
			System.out.println("No Cameras");
			return;
		};
		for (Object oJson : jsons.values()) {
			try {
				if (!(oJson instanceof JSONObject)) continue;
				JSONObject json = (JSONObject) oJson;
				Camera cam = new Camera(json);
				addCamera(cam);
			} catch(CameraParse e) {
				e.printStackTrace();
			};
		};
		System.out.println("Loaded Cameras");
		
	};
	
	@SuppressWarnings("unchecked")
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
		for (int i = 0; i < cameras.size(); i++) {
			if (cameras.get(i).getName().equals(name)) {
				cameras.remove(i);
				break;
			};
		};
	};
	
};