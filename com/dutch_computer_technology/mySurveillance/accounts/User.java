package com.dutch_computer_technology.mySurveillance.accounts;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.exceptions.AccountParse;

public class User {
	
	private String username;
	private boolean isAdmin;
	private String hash;
	private List<Token> tokens = new ArrayList<Token>();
	
	public User(String username, boolean isAdmin, String hash) {
		
		this.username = username;
		this.isAdmin = isAdmin;
		this.hash = hash;
		
	};
	
	public List<Token> getTokens() {
		return tokens;
	};
	public Token getToken(String time) {
		for (Token token : tokens) {
			if (token.getTime().equals(time)) return token;
		};
		return null;
	};
	public void addToken(Token token) {
		tokens.add(token);
	};
	public void remToken(String time) {
		int i = 0;
		for (Token token : tokens) {
			if (!token.getTime().equals(time)) {
				i++;
				continue;
			};
			tokens.remove(i);
			break;
		};
	};
	public void clearTokens() {
		tokens.clear();
	};
	
	public User(JSONObject json) throws AccountParse {
		
		if (!json.containsKey("username")) throw new AccountParse("Can't load user, missing username");
		if (!json.containsKey("hash")) throw new AccountParse("Can't load user, missing hash");
		
		this.username = (String) json.get("username");
		if (json.containsKey("isAdmin")) this.isAdmin = (Boolean) json.get("isAdmin");
		this.hash = (String) json.get("hash");
		
		if (json.containsKey("tokens")) {
			JSONObject tokens = (JSONObject) json.get("tokens");
			for (Object oTime : tokens.keySet()) {
				if (!(oTime instanceof String)) continue;
				String time = (String)oTime;
				Object oKey = tokens.get(time);
				if (!(oKey instanceof String)) continue;
				Token token = new Token(time, (String)oKey);
				addToken(token);
			};
		};
		
	};
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		json.put("username", username);
		json.put("isAdmin", isAdmin);
		json.put("hash", hash);
		
		List<Token> tokens = getTokens();
		JSONObject tokensJSON = new JSONObject();
		for (Token token : tokens) {
			tokensJSON.put(token.getTime(), token.getKey());
		};
		json.put("tokens", tokensJSON);
		
		return json;
		
	};
	
	public String getUsername() {
		return username;
	};
	public void setUsername(String username) {
		this.username = username;
	};
	
	public String isAdmin() {
		return username;
	};
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	};
	
	public String getHash() {
		return hash;
	};
	public void setHash(String hash) {
		this.hash = hash;
	};
	
};