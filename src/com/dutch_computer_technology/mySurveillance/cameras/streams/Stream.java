package com.dutch_computer_technology.mySurveillance.cameras.streams;

import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager.StreamType;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Stream {
	
	private MySurveillance ms;
	private StreamType type;
	private Camera cam;
	
	public Stream(MySurveillance ms, StreamType type, Camera cam) {
		this.ms = ms;
		this.type = type;
		this.cam = cam;
	};
	
	public Stream(MySurveillance ms, StreamType type, Camera cam, JSONObject json) {
		this.ms = ms;
		this.type = type;
		this.cam = cam;
	};
	
	public MySurveillance getMS() {
		return ms;
	};
	
	public StreamType getType() {
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
		return json;
	};
	
	public String html() {
		return "<p>Raw Type</p>";
	};
	
	public boolean test() {
		return false;
	};
	
	public boolean running() {
		return false;
	};
	
	public void start() {};
	
	public void stop() {};
	
};