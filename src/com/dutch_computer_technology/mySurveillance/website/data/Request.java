package com.dutch_computer_technology.mySurveillance.website.data;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Request {
	
	private String method;
	private String url;
	private Cookies cookies;
	private String data;
	private String ip;
	private Socket socket;
	
	public Request(Socket socket) {
		
		this.method = "GET";
		this.url = "/";
		this.cookies = new Cookies();
		this.data = "";
		this.socket = socket;
		
		InetAddress ia = socket.getInetAddress();
		if (ia != null) this.ip = ia.getHostAddress();
		
	};
	
	public Socket socket() {
		return socket;
	};
	public String ip() {
		if (ip == null) return "NO IP FOUND!";
		return ip;
	};
	
	public void method(String method) {
		this.method = method;
	};
	public String method() {
		return method;
	};
	
	public void url(String url) {
		this.url = url;
	};
	public String url() {
		return url;
	};
	
	public String path() {
		return url.split("[?]")[0];
	};
	
	public Map<String, String> query() {
		String[] urls = url.split("[?]");
		Map<String, String> data = new HashMap<String, String>();
		if (urls.length > 1) {
			String[] queries = urls[1].split("&");
			for (String query : queries) {
				String[] queryData = query.split("=");
				if (queryData.length != 2) continue;
				data.put(queryData[0], queryData[1]);
			};
		};
		return data;
	};
	
	public void cookies(Cookies cookies) {
		this.cookies = cookies;
	};
	public Cookies cookies() {
		return cookies;
	};
	
	public void data(String data) {
		this.data = data;
	};
	public String data() {
		return data;
	};
	
};