package com.dutch_computer_technology.mySurveillance.json;

import java.util.ArrayList;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.exceptions.JSONParse;

public class JSONArray {
	
	private List<Object> data;
	
	public JSONArray() {
		data = new ArrayList<Object>();
	};
	
	public JSONArray(String str) throws JSONParse {
		data = new ArrayList<Object>();
		parse(str);
	};
	
	public JSONArray(JSONArray json) {
		data = new ArrayList<Object>(json.data);
	};
	
	public void parse(String str) throws JSONParse {
		if (str == null) throw new JSONParse("Null");
		if (!(str.startsWith("[") && str.endsWith("]"))) throw new JSONParse("Not a JSONArray");
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
				if (open.length() == 0) continue;
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
		for (String oValue : objs) {
			if (oValue.startsWith("\"") && oValue.endsWith("\"") && oValue.length() > 1) {
				if (oValue.length() == 2) {
					data.add("");
				} else {
					data.add(JSONEscape.unescape(oValue.substring(1, oValue.length()-1)));
				};
				continue;
			};
			if (oValue.startsWith("{") && oValue.endsWith("}")) {
				JSONObject json = new JSONObject();
				json.parse(oValue);
				data.add(json);
				continue;
			};
			if (oValue.startsWith("[") && oValue.endsWith("]")) {
				JSONArray json = new JSONArray();
				json.parse(oValue);
				data.add(json);
				continue;
			};
			if (oValue.equals("true")) {
				data.add(true);
				continue;
			};
			if (oValue.equals("false")) {
				data.add(false);
				continue;
			};
			if (oValue.equals("null")) {
				data.add(null);
				continue;
			};
			if (oValue.matches("\\d+L")) {
				try {
					data.add(Long.parseLong(oValue.substring(0, oValue.length()-1)));
					continue;
				} catch(NumberFormatException e) {
					throw new JSONParse("NumberFormatException");
				}
			};
			if (oValue.matches("\\d+")) {
				try {
					data.add(Integer.parseInt(oValue));
					continue;
				} catch(NumberFormatException e) {
					throw new JSONParse("NumberFormatException");
				}
			};
			throw new JSONParse("Unexpected Object");
		};
	};
	
	@Override
	public String toString() {
		String str = "[";
		for (int i = 0; i < data.size(); i++) {
			Object oValue = data.get(i);
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
					str += null;
				} else {
					str += "\"" + JSONEscape.escape(oValue.toString()) + "\"";
				};
			};
			if (i < data.size()-1) str += ",";
		};
		return str + "]";
	};
	
	public boolean contains(Object key) {
		return data.contains(key);
	};
	
	public void add(Object value) {
		add(value, -1);
	};
	
	public void add(Object value, int i) {
		if (i > -1) {
			if (i > data.size()) i = data.size();
			data.add(i, value);
			return;
		};
		data.add(value);
	};

	public void delete(int i) {
		if (i < 0 || i > data.size() || data.size() == 0) return;
		data.remove(i);
	};
	
	public void delete(Object value) {
		data.remove(value);
	};
	
	public int size() {
		return data.size();
	};
	
	public List<Object> objs() {
		return new ArrayList<Object>(data);
	};
	
	public Object get(int i) {
		if (i < 0 || i > data.size()) return null;
		return data.get(i);
	};
	
	public String getString(int i) {
		if (i < 0 || i > data.size()) return null;
		Object oValue = data.get(i);
		if (oValue == null) return null;
		if (oValue instanceof String) return (String) oValue;
		return null;
	};
	
	public int getInt(int i) {
		if (i < 0 || i > data.size()) return 0;
		Object oValue = data.get(i);
		if (oValue == null) return 0;
		if (oValue instanceof Integer) return (int) oValue;
		return 0;
	};
	
	public long getLong(int i) {
		if (i < 0 || i > data.size()) return 0;
		Object oValue = data.get(i);
		if (oValue == null) return 0;
		if (oValue instanceof Long) return (long) oValue;
		return 0;
	};
	
	public boolean getBoolean(int i) {
		if (i < 0 || i > data.size()) return false;
		Object oValue = data.get(i);
		if (oValue == null) return false;
		if (oValue instanceof Long) return (Boolean) oValue;
		return false;
	};
	
	
	public JSONObject getJSONObject(int i) {
		if (i < 0 || i > data.size()) return null;
		Object oValue = data.get(i);
		if (oValue == null) return null;
		if (oValue instanceof JSONObject) return (JSONObject) oValue;
		return null;
	};
	public JSONArray getJSONArray(int i) {
		if (i < 0 || i > data.size()) return null;
		Object oValue = data.get(i);
		if (oValue == null) return null;
		if (oValue instanceof JSONArray) return (JSONArray) oValue;
		return null;
	};
	
};