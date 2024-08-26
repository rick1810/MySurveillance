package com.dutch_computer_technology.mySurveillance.accounts;

import java.util.ArrayList;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.exceptions.AccountParse;
import com.dutch_computer_technology.JSONManager.data.JSONObject;

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
		
		if (json == null) throw new AccountParse("Can't load user, null");
		if (!json.contains("username")) throw new AccountParse("Can't load user, missing username");
		if (!json.contains("hash")) throw new AccountParse("Can't load user, missing hash");
		
		this.username = (String) json.get("username");
		if (json.contains("isAdmin")) this.isAdmin = json.getBoolean("isAdmin");
		this.hash = (String) json.get("hash");
		
		if (json.contains("tokens")) {
			JSONObject tokens = json.getJSONObject("tokens");
			if (tokens != null) {
				for (String time : tokens.keySet()) {
					String key = tokens.getString(time);
					Token token = new Token(time, key);
					addToken(token);
				};
			};
		};
		
	};
	
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
	
	public boolean isAdmin() {
		return isAdmin;
	};
	public void isAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	};
	
	public String getHash() {
		return hash;
	};
	public void setHash(String hash) {
		this.hash = hash;
	};
	
};