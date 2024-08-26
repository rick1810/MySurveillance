package com.dutch_computer_technology.mySurveillance.accounts;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.website.data.Cookie;
import com.dutch_computer_technology.mySurveillance.website.data.Cookies;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.exceptions.AccountParse;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Accounts {
	
	private MySurveillance ms;
	private List<User> users = new ArrayList<User>();
	
	public Accounts(MySurveillance ms) {
		
		this.ms = ms;
		
		Main.print(this, "Loading Accounts");
		JSONObject json = ms.getJSON("accounts.json");
		if (json != null) {
			
			for (String key : json.keySet()) {
			
				try {
					User user = new User(json.getJSONObject(key));
					addUser(user);
				} catch(AccountParse e) {
					Main.print(this, "Could not load user", e);
				};
				
			};
			Main.print(this, "Loaded Accounts");
			
		} else {
			
			Main.print(this, printType.WARNING, "No accounts, creating default.\nUsername: " + Main.defaultUser + "\nPassword: " + Main.defaultPassword);
			User user = new User(Main.defaultUser, true, createHash(Main.defaultPassword));
			addUser(user);
			
		};
		
	};
	
	public void save() {
		
		JSONObject json = new JSONObject();
		for (User user : users) {
			json.put(user.getUsername(), user.toJSON());
		};
		
		ms.saveJSON("accounts.json", json);
		
	};
	
	public List<String> getUsers() {
		List<String> names = new ArrayList<String>();
		for (User user : users) {
			names.add(user.getUsername());
		};
		return names;
	};
	
	public int totalUsers() {
		return users.size();
	};
	
	public User getUser(String username) {
		for (User user : users) {
			if (user.getUsername().equals(username)) return user;
		};
		return null;
	};
	
	public void addUser(User user) {
		setUser(user);
	};
	public void setUser(User user) {
		User oldUser = getUser(user.getUsername());
		if (oldUser != null) remUser(user.getUsername());
		users.add(user);
	};
	
	public void remUser(String username) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(username)) {
				users.remove(i);
				break;
			};
		};
	};
	
	public Token getToken(Cookies cookies) {
		
		Cookie cookie = cookies.getCookie("token");
		if (cookie == null) return null;
		
		String[] baseTokens = cookie.value().split("[.]");
		
		if (baseTokens.length != 3) return null;
		
		String time = baseTokens[1];
		String key = baseTokens[2];
		return new Token(time, key);
		
	};
	
	public User getCookieUser(Request request) {
		
		Cookie cookie = request.cookies().getCookie("token");
		if (cookie == null) return null;
		
		String[] baseTokens = cookie.value().split("[.]");
		
		if (baseTokens.length != 3) return null;
		
		String username = new String(Base64.getDecoder().decode(baseTokens[0]));
		
		User user = getUser(username);
		if (user == null) return null;
		
		String time = baseTokens[1];
		String key = baseTokens[2];
		
		Token userToken = user.getToken(time);
		if (userToken == null) return null;
		
		Token token = new Token(time, key);
		
		if (token.equals(userToken)) {
			if (userToken.expired(Main.tokenExpire)) {
				Main.print(this, printType.INFO, "Token expired for user " + username);
				user.remToken(time);
				return null;
			};
			return user;
		};
		
		return null;
		
	};
	
	public String createStringToken(String username, Token token) {
		
		String baseUsername = new String(Base64.getEncoder().encode(username.getBytes()));
		return baseUsername + "." + token.getTime() + "." + token.getKey();
		
	};
	
	public boolean matsHash(String hash, String password, boolean isHashed) {
		
		if (hash == null) return false;
		if (password == null) return false;
		if (isHashed) {
			return hash.equals(password);
		};
		return hash.equals(createHash(password));
		
	};
	
	public String createHash(String password) {
		
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			return new String(Base64.getEncoder().encode(digest.digest(password.getBytes(StandardCharsets.UTF_8))));
		} catch (NoSuchAlgorithmException e) {
			Main.print(this, "Could not create Hash", e);
		};
		return null;
		
	};
	
};