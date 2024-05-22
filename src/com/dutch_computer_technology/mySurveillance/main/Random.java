package com.dutch_computer_technology.mySurveillance.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Random {
	
	List<String> characters = new ArrayList<String>();
	
	public Random() {
		
		for (int i = 33; i < 127; i++) {
			characters.add(String.valueOf((char)i));
		};
		
	};
	
	public String idDate(int size) {
		
		Date date = new Date();
		return Long.toString(date.getTime()) + id(size);
		
	};
	
	public String id(int size) {
		
		if (size < 1) size = 16;
		String id = "";
		for (int i = 0; i < size; i++) {
			id += characters.get((int) Math.floor(Math.random() * characters.size()));
		};
		return id;
		
	};
	
	public int randomInt(int max) {
		
		return randomInt(0, max);
		
	};
	public int randomInt(int min, int max) {
		
		return min + ((int) Math.floor(Math.random() * (max - min + 1)));
		
	};
	
	public boolean action(int dif) {
		
		if (dif < 2) dif = 2;
		return (Math.floor(Math.random() * dif) == 0);
		
	};
	
	public Object choice(List<?> list) {
		
		if (list == null) return null;
		return list.get((int) Math.floor(Math.random() * list.size()));
		
	};
	
};