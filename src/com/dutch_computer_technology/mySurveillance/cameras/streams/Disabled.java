package com.dutch_computer_technology.mySurveillance.cameras.streams;

import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager.StreamType;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Disabled extends Stream {
	
	public Disabled(MySurveillance ms, Camera cam) {
		super(ms, StreamType.Disabled, cam);
	};
	
	public Disabled(MySurveillance ms, Camera cam, JSONObject json) {
		super(ms, StreamType.Disabled, cam);
	};
	
	@Override
	public String html() {
		return "<div><p>Will not stream/record</p></div>";
	};
	
};