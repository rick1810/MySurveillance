package com.dutch_computer_technology.mySurveillance.cameras.reasons;

import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.FileManager.ReasonType;

public class Disabled extends Reason {
	
	public Disabled(MySurveillance ms, Camera cam) {
		super(ms, ReasonType.Disabled, cam);
	};
	
	public Disabled(MySurveillance ms, Camera cam, JSONObject json) {
		super(ms, ReasonType.Disabled, cam, json);
	};
	
	@Override
	public String html() {
		return "<div><p>Will not delete files</p></div>";
	};
	
};