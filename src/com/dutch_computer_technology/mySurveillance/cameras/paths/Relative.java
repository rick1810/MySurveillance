package com.dutch_computer_technology.mySurveillance.cameras.paths;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;

public class Relative extends Path {
	
	public Relative(MySurveillance ms, Camera cam) {
		super(ms, PathType.Relative, cam);
		setPath(Main.path(Main.defaultSaveLocation));
	};
	
	public Relative(MySurveillance ms, Camera cam, JSONObject json) {
		super(ms, PathType.Relative, cam, json);
	};
	
	@Override
	public String html() {
		return "<div><p>Path:</p><p class=\"gray\">MySurveillance" + Main.slash() + "</p><input key=\"path\" type=\"text\" value=\"" + getPath() + "\"/></div>";
	};
	
	@Override
	public String toString() {
		return getMS().path + getPath();
	};
	
};