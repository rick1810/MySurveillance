package com.dutch_computer_technology.mySurveillance.website;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public interface PageHandler {
	
	public Page request(MySurveillance ms, String method, String url, JSONObject cookies, User user, String data);
	
};