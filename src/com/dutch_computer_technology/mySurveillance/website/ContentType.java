package com.dutch_computer_technology.mySurveillance.website;

public enum ContentType {
	
	TEXT("text/plain"),
	HTML("text/html"),
	JFIF("multipart/x-mixed-replace;boundary=--myboundary"),
	JPEG("image/jpeg"),
	JSON("application/json"),
	ICO("image/ico"),
	PNG("image/png"),
	NONE("");
	
	public final String text;
	private ContentType(String str) {
		this.text = str;
	};
	@Override
	public String toString() {
		return this.text;
	};
	
};