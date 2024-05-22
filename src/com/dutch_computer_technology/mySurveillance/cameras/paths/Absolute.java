package com.dutch_computer_technology.mySurveillance.cameras.paths;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Absolute extends Path {
	
	public Absolute(MySurveillance ms) {
		super(ms, PathType.Absolute);
		setPath(ms.path + Main.path(Main.defaultSaveLocation));
	};
	
	public Absolute(MySurveillance ms, JSONObject json) {
		super(ms, PathType.Absolute, json);
	};
	
	@Override
	public String html() {
		return "<div><p>Path:</p><input key=\"path\" type=\"text\" value=\"" + getPath() + "\"/></div>";
	};
	
};