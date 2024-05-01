package com.dutch_computer_technology.mySurveillance.website;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.simple.*;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Website extends Thread {
	
	private Map<String, PageHandler> pages = new HashMap<String, PageHandler>();
	private ServerSocket serverSocket;
	
	private MySurveillance ms;
	
	public Website(MySurveillance ms, int port) throws IOException {
		
		this.ms = ms;
		
		pages.put("/login", new com.dutch_computer_technology.mySurveillance.pages.Login());
		pages.put("/", new com.dutch_computer_technology.mySurveillance.pages.Home());
		pages.put("/accounts", new com.dutch_computer_technology.mySurveillance.pages.Accounts());
		pages.put("/cameras", new com.dutch_computer_technology.mySurveillance.pages.Cameras());
		pages.put("/settings", new com.dutch_computer_technology.mySurveillance.pages.Settings());
		
		serverSocket = new ServerSocket(port);
		
	};
	
	public void run() {
		
		while (ms.run) {
			if (serverSocket == null) return;
			try {
				OnRequest request = new OnRequest(ms, serverSocket.accept());
				request.start();
			} catch (IOException e) {
				e.printStackTrace();
			};
		};
		
	};
	
	public enum FileType {
		HTML("pages/html", ".html"),
		JS("pages/js", ".js"),
		CSS("pages/css", ".css"),
		ICO("images", ".ico"),
		PNG("images", ".png");
		private String path;
		private String suffix;
		FileType(String path, String suffix) {
			this.path = path;
			this.suffix = suffix;
		};
		@Override
		public String toString() {
			return this.suffix;
		};
	};
	
	public byte[] loadFile(FileType fileType, String name) {
		
		byte[] data = new byte[0];
		
		name = name.replaceAll("/", "_");
		InputStream is = Main.class.getResourceAsStream(fileType.path + "/" + name + fileType);
		if (is == null) return data;
		
		try {
			while (is.available() > 0) {
				data = ByteManager.append(data, (byte)is.read());
			};
			is.close();
		} catch(IOException e) {
			e.printStackTrace();
		};
		
		return data;
		
	};
	
	private String getPathFromTag(String tag) {
		return tag.replaceAll("<.*?>|<\\/.*?>", "");
	};
	public String replaceHTML(String html, String tag, String replacer) {
		return Pattern.compile(tag).matcher(html).replaceAll(match -> replacer);
	};
	public String loadHTML(String html) {
		
		html = Pattern.compile("<script>.*?<\\/script>").matcher(html).replaceAll(match -> "<script>" + new String(loadFile(FileType.JS, getPathFromTag(match.group()))) + "</script>");
		html = Pattern.compile("<style>.*?<\\/style>").matcher(html).replaceAll(match -> "<style>" + new String(loadFile(FileType.CSS, getPathFromTag(match.group()))) + "</style>");
		return html;
		
	};
	
	public byte[] stream(Page page) {
		
		byte[] s = new byte[0];
		if (page == null) {
			page = new Page();
			page.setStatus(400);
			page.setContentType(ContentTypes.TEXT);
			page.setData("Page not found!");
		};
		if (page.isRaw()) return ByteManager.append(s, page.getRawData());
		
		s = ByteManager.append(s, "HTTP/1.1 " + Integer.toString(page.getStatus()) + "\nServer: MySurveillance");
		ContentTypes type = page.getContentType();
		if (!type.equals(ContentTypes.NONE)) {
			s = ByteManager.append(s, "\nContent-Type: " + type.toString());
		};
		Map<String, String> headers = page.getHeaders();
		for (String header : headers.keySet()) {
			s = ByteManager.append(s, "\n" + header + ": " + headers.get(header));
		};
		s = ByteManager.append(s, "\n\n");
		String data = page.getData();
		if (data != null) s = ByteManager.append(s, data);
		return s;
		
	};
	
	public String getPath(String url) {
		return url.split("[?]")[0];
	};
	public Map<String, String> getQuery(String url) {
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
	
	public Page getPage(String method, String url, JSONObject cookies, User user, String data) {
		
		String path = getPath(url);
		if (!pages.containsKey(path)) return null;
		PageHandler ph = pages.get(path);
		if (ph == null) return null;
		return ph.request(ms, method, url, cookies, user, data);
		
	};
	
};