package com.dutch_computer_technology.mySurveillance.accounts;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.exceptions.AccountParse;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Accounts {
	
	private MySurveillance ms;
	private List<User> users = new ArrayList<User>();
	
	private final long tokenExpire = 60*60*24*31;
	
	public Accounts(MySurveillance ms) {
		
		this.ms = ms;
		
		System.out.println("Loading Accounts");
		JSONObject jsons = ms.getJSON("accounts.json");
		if (jsons != null) {
			
			for (Object oJson : jsons.values()) {
				try {
					if (!(oJson instanceof JSONObject)) continue;
					JSONObject json = (JSONObject) oJson;
					User user = new User(json);
					addUser(user);
				} catch(AccountParse e) {
					e.printStackTrace();
				};
			};
			System.out.println("Loaded Accounts");
			
		} else {
			
			System.out.println("No accounts, creating default.\nUsername: Admin\nPassword: 0000");
			User user = new User("Admin", true, createHash("0000"));
			addUser(user);
			
		};
		
	};
	
	@SuppressWarnings("unchecked")
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
	
	public Token getToken(JSONObject cookies) {
		
		if (!cookies.containsKey("token")) return null;
		Object objToken = cookies.get("token");
		
		if (!(objToken instanceof String)) return null;
		String[] baseTokens = ((String) objToken).split("[.]");
		
		if (baseTokens.length != 3) return null;
		
		String id = baseTokens[1];
		String key = baseTokens[2];
		return new Token(id, key);
		
	};
	
	public User getCookieUser(JSONObject cookies) {
		
		if (!cookies.containsKey("token")) return null;
		Object objToken = cookies.get("token");
		
		if (!(objToken instanceof String)) return null;
		String[] baseTokens = ((String) objToken).split("[.]");
		
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
			if (userToken.expired(tokenExpire)) {
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
			e.printStackTrace();
		};
		return null;
		
	};
	
};