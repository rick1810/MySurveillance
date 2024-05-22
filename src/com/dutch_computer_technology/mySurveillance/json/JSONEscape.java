package com.dutch_computer_technology.mySurveillance.json;

public class JSONEscape {
	
	public static String escape(String str) {
		str = str.replace("\\", "\\\\");
		str = str.replace("/", "\\/");
		str = str.replace("\"", "\\\"");
		str = str.replace("\n", "\\\n");
		str = str.replace("\b", "\\\b");
		str = str.replace("\f", "\\\f");
		str = str.replace("\r", "\\\r");
		str = str.replace("\t", "\\\t");
		return str;
	};
	
	public static String unescape(String str) {
		str = str.replace("\\\\", "\\");
		str = str.replace("\\/", "/");
		str = str.replace("\\\"", "\"");
		str = str.replace("\\\n", "\n");
		str = str.replace("\\\b", "\b");
		str = str.replace("\\\f", "\f");
		str = str.replace("\\\r", "\r");
		str = str.replace("\\\t", "\t");
		return str;
	};
	
};