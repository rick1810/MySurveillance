package com.dutch_computer_technology.mySurveillance.cameras.paths;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Absolute extends Path {
	
	public Absolute(MySurveillance ms, Camera cam) {
		super(ms, PathType.Absolute, cam);
		setPath(ms.path + Main.path(Main.defaultSaveLocation));
	};
	
	public Absolute(MySurveillance ms, Camera cam, JSONObject json) {
		super(ms, PathType.Absolute, cam, json);
	};
	
	@Override
	public String html() {
		return "<div><p>Path:</p><input key=\"path\" type=\"text\" value=\"" + getPath() + "\"/></div>";
	};
	
};