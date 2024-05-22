package com.dutch_computer_technology.mySurveillance.cameras.reasons;

import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.FileManager.ReasonType;

public class Reason {
	
	private MySurveillance ms;
	private ReasonType type;
	private Camera cam;
	
	public Reason(MySurveillance ms, ReasonType type, Camera cam) {
		this.ms = ms;
		this.type = type;
		this.cam = cam;
	};
	
	public Reason(MySurveillance ms, ReasonType type, Camera cam, JSONObject json) {
		this.ms = ms;
		this.type = type;
		this.cam = cam;
	};
	
	public MySurveillance getMS() {
		return ms;
	};
	
	public ReasonType getType() {
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
	
	public void delete() {
		
	};
	
};