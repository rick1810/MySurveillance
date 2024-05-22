package com.dutch_computer_technology.mySurveillance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import com.dutch_computer_technology.mySurveillance.languages.Lang.Language;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Main {
	
	public static final String version = "1.4";
	public static final int defaultPort = 8090;
	public static final String defaultSaveLocation = "storage";
	public static final String defaultUser = "Admin";
	public static final String defaultPassword = "0000";
	public static final Language defaultLanguage = Language.en;
	public static final long tokenExpire = 1000L*60*60*24*31;
	
	public static final String primaryColor = "#404040";
	public static final String secondaryColor = "#494949";
	public static final String tertiaryColor = "#1a1a1a";
	public static final String inputColor = "#535353";
	public static final String inputBorderColor = "#7a7a7a";
	public static final String bannerColor = "#985b2c";
	
	public static String slash() {
		String rep = null;
		try {
			rep = System.getProperty("file.separator");
		} catch(Exception ignore) {};
		if (rep == null) rep = "/";
		return rep;
	};
	public static String path(String path) {
		String slash = slash();
		if (slash.equals("\\")) slash += "\\";
		return path.replaceAll("[\\\\/]", slash);
	};
	public static String[] files(String path) {
		return path.split("[\\\\/]");
	};
	
	public static void main(String[] args) throws URISyntaxException {
		
		print(null, printType.NOTIME, "");
		try {
			new MySurveillance();
		} catch(Exception e) {
			print(null, "MySurveillance crashed!", e);
		};
		
	};
	
	private static void logFile(String name, byte[] data) { //Can't use FileManager, incase FileManager has a exception
		
		String[] names = files(name);
		if (names.length > 1) {
			String path = "";
			for (int i = 0; i < names.length-1; i++) {
				path += names[i] + "/";
				File file = new File(path);
				if (!file.exists()) file.mkdir();
			};
		};
		
		try {
			File file = new File(name + ".txt");
			OutputStream out = new FileOutputStream(file, true);
			out.write(data);
			out.write("\r\n".getBytes());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		};
		
	};
	
	public static enum printType {
		NOLOG,
		NOTIME,
		REQUEST,
		INFO,
		WARNING,
		ERROR
	};
	
	public static void print(Object obj, String info) {
		print(obj, printType.INFO, info, null);
	};
	
	public static void print(Object obj, printType type, String info) {
		print(obj, type, info, null);
	};
	
	public static void print(Object obj, String info, Exception e) {
		print(obj, printType.ERROR, info, e);
	};
	
	public static void print(Object obj, Exception e) {
		print(obj, printType.ERROR, null, e);
	};
	
	public static void print(Object obj, printType type, String info, Exception e) {
		String str = "";
		if (obj != null) str += ("[" + obj.getClass().getSimpleName() + "] ");
		switch (type) {
			case WARNING:
				str += "[WARNING] ";
				break;
			case ERROR:
				str += "[ERROR] ";
				break;
			default:
				break;
		};
		if (info != null) str += info;
		if (!type.equals(printType.REQUEST)) System.out.println(str);
		if (e != null) e.printStackTrace();
		
		if (type.equals(printType.NOLOG)) return;
		
		LocalDateTime now = LocalDateTime.now();
		
		String line = "";
		if (!type.equals(printType.NOTIME)) {
			String sHour = Integer.toString(now.getHour());
			if (sHour.length() < 2) sHour = "0" + sHour;
			String sMin = Integer.toString(now.getMinute());
			if (sMin.length() < 2) sMin = "0" + sMin;
			String sSec = Integer.toString(now.getSecond());
			if (sSec.length() < 2) sSec = "0" + sSec;
			line = "[" + sHour + ":" + sMin + ":" + sSec + "] ";
		};
		
		line += str;
		if (e != null) line += "\r\n" + e.toString() + "\r\n";
		
		String sMon = Integer.toString(now.getMonthValue());
		if (sMon.length() < 2) sMon = "0" + sMon;
		String sDay = Integer.toString(now.getDayOfMonth());
		if (sDay.length() < 2) sDay = "0" + sDay;
		String name = "logs/" + sDay + "-" + sMon + "-" + now.getYear();
		logFile(name, line.getBytes());
		
	};
	
};