package com.dutch_computer_technology.mySurveillance.website;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
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
				Main.print(this, "Could not create request handler", e);
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
	
	public Page notFoundPage(ContentType contentType) {
		if (contentType == null) contentType = ContentType.TEXT;
		if (!(contentType.equals(ContentType.TEXT) || contentType.equals(ContentType.HTML))) {
			contentType = ContentType.TEXT;
		};
		
		Page page = new Page();
		page.setStatus(404);
		page.setContentType(contentType);
		
		if (contentType.equals(ContentType.TEXT)) {
			page.setData("404, Not found");
			return page;
		};
		
		String msg = "<div class=\"msg\"><h2>404 Not found</h2><pre>It seems this page does not exists!</pre></div>";
		String html = "<html><head><style>default</style><style>notFound</style><script>default</script><title>MySurveillance</title></head>"
				+ "<body><div class=\"header\"><h1>MySurveillance</h1><div class=\"pages\">"
				+ "<button onmousedown=\"goTo(event, '')\">Home</button><button onmousedown=\"goTo(event, 'accounts')\">Accounts</button>"
				+ "<button onmousedown=\"goTo(event, 'cameras')\">Cameras</button><button onmousedown=\"goTo(event, 'settings')\">Settings</button>"
				+ "</div><div class=\"options\"><button class=\"logout\" id=\"logoutButton\">Logout</button></div></div>" + msg + "</body></html>";
		
		html = ms.website.loadHTML(html);
		
		page.setData(html);
		
		return page;
		
	};
	
	public Page unauthorizedPage(ContentType contentType, String reason) {
		if (contentType == null) contentType = ContentType.TEXT;
		if (!(contentType.equals(ContentType.TEXT) || contentType.equals(ContentType.HTML))) {
			contentType = ContentType.TEXT;
		};
		
		Page page = new Page();
		page.setStatus(401);
		page.setContentType(contentType);
		
		if (contentType.equals(ContentType.TEXT)) {
			if (reason == null) {
				page.setData("Unauthorized");
				return page;
			};
			page.setData(reason);
			return page;
		};
		
		String msg = "<div class=\"msg\"><h2>Unauthorized</h2>";
		if (reason != null) msg += "<pre>" + reason + "</pre>";
		msg += "</div>";
		String html = "<html><head><style>default</style><style>unauthorized</style><script>default</script><title>MySurveillance</title></head>"
				+ "<body><div class=\"header\"><h1>MySurveillance</h1><div class=\"pages\">"
				+ "<button onmousedown=\"goTo(event, '')\">Home</button><button onmousedown=\"goTo(event, 'accounts')\">Accounts</button>"
				+ "<button onmousedown=\"goTo(event, 'cameras')\">Cameras</button><button onmousedown=\"goTo(event, 'settings')\">Settings</button>"
				+ "</div><div class=\"options\"><button class=\"logout\" id=\"logoutButton\">Logout</button></div></div>" + msg + "</body></html>";
		
		html = ms.website.loadHTML(html);
		
		page.setData(html);
		
		return page;
		
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
			Main.print(this, "Could not loadFile for request", e);
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
			page.setContentType(ContentType.TEXT);
			page.setData("Page not found!");
		};
		if (page.isRaw()) return ByteManager.append(s, page.getRawData());
		
		s = ByteManager.append(s, "HTTP/1.1 " + Integer.toString(page.getStatus()) + "\nServer: MySurveillance");
		ContentType type = page.getContentType();
		if (!type.equals(ContentType.NONE)) {
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
	
	public Page getPage(Request request, User user) {
		
		String path = request.path();
		if (!pages.containsKey(path)) return null;
		PageHandler ph = pages.get(path);
		if (ph == null) return null;
		if (request.method().equals("GET")) {
			return ph.get(ms, request, user);
		} else if (request.method().equals("POST")) {
			return ph.post(ms, request, user);
		};
		Page page = new Page();
		page.setContentType(ContentType.TEXT);
		page.setStatus(405);
		page.setData("Method " + request.method() + " not supported!\nTry GET or POST");
		return page;
		
	};
	
};