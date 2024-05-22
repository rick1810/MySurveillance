package com.dutch_computer_technology.mySurveillance.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dutch_computer_technology.mySurveillance.exceptions.JSONParse;

public class JSONObject {
	
	private Map<String, Object> data;
	
	public JSONObject() {
		data = new HashMap<String, Object>();
	};
	
	public JSONObject(String str) throws JSONParse {
		data = new HashMap<String, Object>();
		parse(str);
	};
	
	public JSONObject(JSONObject json) {
		data = new HashMap<String, Object>(json.data);
	};
	
	public void parse(String str) throws JSONParse {
		if (str == null) throw new JSONParse("Null");
		if (!(str.startsWith("{") && str.endsWith("}"))) throw new JSONParse("Not a JSONObject");
		if (str.length() == 2) return;
		str = str.substring(1, str.length()-1);
		List<String> objs = new ArrayList<String>();
		String open = "";
		int o = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '[' || c == '{') {
				open += c;
				continue;
			};
			if (c == ']' || c == '}') {
				if (open.length() == 0) throw new JSONParse("Illegal bracket at " + (i+2));
				char b = open.charAt(open.length()-1);
				if (!((c == ']' && b == '[') || (c == '}' && b == '{'))) throw new JSONParse("Illegal bracket at " + (i+2));
				open = open.substring(0, open.length()-1);
			};
			if (c == '"') {
				if (open.length() > 0 && open.charAt(open.length()-1) == '"') {
					if (open.length() == 0) continue;
					open = open.substring(0, open.length()-1);
				} else {
					open += '"';
				};
			};
			if (open.length() > 0) continue;
			if (c == ',') {
				String buf = str.substring(str.charAt(o) == ',' ? o+1 : o, i);
				objs.add(buf);
				o = i;
				continue;
			};
			if (i == str.length()-1) {
				String buf = str.substring(str.charAt(o) == ',' ? o+1 : o);
				objs.add(buf);
			};
		};
		o = 1; //Offset for { of init
		for (String obj : objs) {
			int closeKey = obj.indexOf("\"", 1);
			if (closeKey == -1) throw new JSONParse("Key is not a String at " + o);
			String key = obj.substring(obj.startsWith("\"") ? 1 : 0, closeKey);
			if (key.length() == 0) throw new JSONParse("Key is empty at " + o); //Key should always have a value
			int separatorIndex = obj.indexOf(":", closeKey);
			if (separatorIndex == -1) throw new JSONParse("No Separator at " + o);
			String oValue = obj.substring(separatorIndex+1, obj.length());
			if (oValue.startsWith("\"") && oValue.endsWith("\"") && oValue.length() > 1) {
				if (oValue.length() == 2) {
					data.put(key, "");
				} else {
					data.put(key, JSONEscape.unescape(oValue.substring(1, oValue.length()-1)));
				};
				continue;
			};
			if (oValue.startsWith("{") && oValue.endsWith("}")) {
				data.put(key, new JSONObject(oValue));
				o += key.length()+oValue.length()+5;
				continue;
			};
			if (oValue.startsWith("[") && oValue.endsWith("]")) {
				data.put(key, new JSONArray(oValue));
				o += key.length()+oValue.length()+5;
				continue;
			};
			if (oValue.equals("true")) {
				data.put(key, true);
				o += key.length()+oValue.length();
				continue;
			};
			if (oValue.equals("false")) {
				data.put(key, false);
				o += key.length()+oValue.length();
				continue;
			};
			if (oValue.equals("null")) {
				data.put(key, null);
				o += key.length()+oValue.length();
				continue;
			};
			if (oValue.matches("\\d+L")) {
				try {
					data.put(key, Long.parseLong(oValue.substring(0, oValue.length()-1)));
					o += key.length()+oValue.length();
					continue;
				} catch(NumberFormatException e) {
					throw new JSONParse("NumberFormatException at " + o);
				}
			};
			if (oValue.matches("\\d+")) {
				try {
					data.put(key, Integer.parseInt(oValue));
					o += key.length()+oValue.length();
					continue;
				} catch(NumberFormatException e) {
					throw new JSONParse("NumberFormatException at " + o);
				}
			};
			throw new JSONParse("Unexpected Object at " + o);
		};
	};
	
	@Override
	public String toString() {
		String str = "{";
		Object[] keys = data.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			str += "\"" + ((String) keys[i]) + "\":";
			Object oValue = data.get(((String) keys[i]));
			if (oValue instanceof JSONObject) {
				str += ((JSONObject) oValue).toString();
			} else if (oValue instanceof JSONArray) {
				str += ((JSONArray) oValue).toString();
			} else if (oValue instanceof String) {
				str += "\"" + JSONEscape.escape((String) oValue) + "\"";
			} else if (oValue instanceof Long) {
				str += Long.toString((long) oValue) + "L";
			} else if (oValue instanceof Integer) {
				str += Integer.toString((int) oValue);
			} else if (oValue instanceof Boolean) {
				if ((boolean) oValue) {
					str += "true";
				} else {
					str += "false";
				};
			} else {
				if (oValue == null) {
					str += "null";
				} else {
					str += "\"" + JSONEscape.escape(oValue.toString()) + "\"";
				};
			};
			if (i < data.size()-1) str += ",";
		};
		return str + "}";
	};
	
	public boolean contains(String key) {
		return data.containsKey(key);
	};
	
	public Set<String> keySet() {
		return data.keySet();
	};
	
	public void put(String key, Object value) {
		if (key == null) return;
		data.put(key, value);
	};
	
	public void delete(String key) {
		if (data.containsKey(key)) data.remove(key);
	};
	
	public Object get(String key) {
		if (key == null) return null;
		return data.get(key);
	};
	
	public String getString(String key) {
		if (key == null) return null;
		Object oValue = data.get(key);
		if (oValue == null) return null;
		if (oValue instanceof String) return (String) oValue;
		return null;
	};
	
	public int getInt(String key) {
		if (key == null) return 0;
		Object oValue = data.get(key);
		if (oValue == null) return 0;
		if (oValue instanceof Integer) return (int) oValue;
		return 0;
	};
	
	public long getLong(String key) {
		if (key == null) return 0;
		Object oValue = data.get(key);
		if (oValue == null) return 0;
		if (oValue instanceof Long) return (long) oValue;
		return 0;
	};
	
	public boolean getBoolean(String key) {
		if (key == null) return false;
		Object oValue = data.get(key);
		if (oValue == null) return false;
		if (oValue instanceof Boolean) return (boolean) oValue;
		return false;
	};
	
	
	public JSONObject getJSONObject(String key) {
		if (key == null) return null;
		Object oValue = data.get(key);
		if (oValue == null) return null;
		if (oValue instanceof JSONObject) return (JSONObject) oValue;
		return null;
	};
	public JSONArray getJSONArray(String key) {
		if (key == null) return null;
		Object oValue = data.get(key);
		if (oValue == null) return null;
		if (oValue instanceof JSONArray) return (JSONArray) oValue;
		return null;
	};
	
};