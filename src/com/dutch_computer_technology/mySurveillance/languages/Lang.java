package com.dutch_computer_technology.mySurveillance.languages;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;

public class Lang {
	
	Map<String, String> text;
	private Language language;
	
	public enum Language {
		en("English"),
		nl("Nederlands");
		public final String name;
		private Language(String name) {
			this.name = name;
		};
		public String displayName() {
			return this.name;
		};
	};
	
	public Lang(String languageStr) {
		text = new HashMap<String, String>();
		Language language = Main.defaultLanguage;
		if (languageStr != null) {
			try {
				language = Language.valueOf(languageStr);
			} catch(IllegalArgumentException ignore) {};
		};
		this.language = language;
		load(Main.defaultLanguage);
		if (Main.defaultLanguage.equals(language)) return;
		load(language);
	};
	
	public Language getLanguage() {
		return language;
	};
	public void setLanguage(Language language) {
		this.language = language;
		load(language);
	};
	
	public void load(Language language) {
		
		InputStream is = Lang.class.getResourceAsStream(language.toString());
		if (is == null) {
			Main.print(this, "Could not load language");
			return;
		};
		
		byte[] data = new byte[0];
		try {
			while (is.available() > 0) {
				data = ByteManager.append(data, (byte)is.read());
			};
			is.close();
		} catch(IOException e) {
			Main.print(this, "Could not load language", e);
			return;
		};
		
		String[] datas = new String(data).split("\n");
		for (String str : datas) {
			str = str.replace("\r", "");
			String[] strs = str.split("=");
			if (strs.length < 2) continue;
			String key = strs[0];
			String val = "";
			for (int i = 1; i < strs.length; i++) val += strs[i];
			text.put(key, val);
		};
		
	};
	
	public String get(String key) {
		
		if (key == null) return "";
		if (text.containsKey(key)) return text.get(key);
		return key;
		
	};
	
};