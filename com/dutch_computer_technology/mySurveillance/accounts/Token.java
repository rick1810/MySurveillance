package com.dutch_computer_technology.mySurveillance.accounts;

import java.util.Base64;
import java.util.Date;

import com.dutch_computer_technology.mySurveillance.main.Random;

public class Token {
	
	private String time;
	private String key;
	
	public Token() {
		Random ran = new Random();
		long now = new Date().getTime();
		String time = Long.toString(now);
		this.time = new String(Base64.getEncoder().encode(time.getBytes()));;
		this.key = new String(Base64.getEncoder().encode(ran.id(24).getBytes()));
	};
	
	public Token(String time, String key) {
		this.time = time;
		this.key = key;
	};
	
	public boolean equals(Token token) {
		if (token == null) return false;
		if (!this.time.equals(token.time)) return false;
		if (!this.key.equals(token.key)) return false;
		return true;
	};
	
	public boolean expired(long max) {
		
		long now = new Date().getTime();
		
		String timeD = new String(Base64.getDecoder().decode(time));
		long timeL;
		try {
			timeL = Long.valueOf(timeD);
		} catch(NumberFormatException e) {
			return false;
		};
		
		return (now-timeL > max);
		
	};
	
	public String getTime() {
		return time;
	};
	
	public String getKey() {
		return key;
	};
	
};