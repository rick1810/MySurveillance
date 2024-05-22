package com.dutch_computer_technology.mySurveillance.website.data;

public class Cookie {
	
	private String key;
	private String value;
	
	public Cookie(String key, String value) {
		this.key = key;
		this.value = value;
	};
	
	public String key() {
		return key;
	};
	public String value() {
		return value;
	};
	
};