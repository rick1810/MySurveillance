package com.dutch_computer_technology.mySurveillance.website.data;

import java.util.HashMap;
import java.util.Map;

import com.dutch_computer_technology.mySurveillance.website.ContentType;

public class Page {
	
	private int status;
	private ContentType contentType;
	private String data;
	private byte[] rawData;
	private Map<String, String> headers;
	
	public Page() {
		this.status = 200;
		this.contentType = ContentType.TEXT;
		this.data = "";
		this.headers = new HashMap<String, String>();
	};
	
	public int getStatus() {
		return status;
	};
	public void setStatus(int status) {
		this.status = status;
	};
	
	public ContentType getContentType() {
		return contentType;
	};
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	};
	
	public String getData() {
		return data;
	};
	public void setData(String data) {
		this.data = data;
	};
	
	public boolean isRaw() {
		if (rawData != null) {
			if (rawData.length > 0) return true;
		};
		return false;
	};
	public byte[] getRawData() {
		return rawData;
	};
	public void setRawData(byte[] rawData) {
		this.rawData = rawData;
	};
	
	public Map<String, String> getHeaders() {
		return headers;
	};
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	};
	public String getHeader(String name) {
		if (headers.containsKey(name)) return (String) headers.get(name);
		return null;
	};
	public void setHeader(String name, String value) {
		headers.put(name, value);
	};
	public void remHeader(String name) {
		if (headers.containsKey(name)) headers.remove(name);
	};
	
};