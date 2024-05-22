package com.dutch_computer_technology.mySurveillance.website;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.website.data.Cookie;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class OnRequest extends Thread {
	
	private MySurveillance ms;
	private Socket socket;
	
	private int bytesPerWrite = 4096;
	
	public OnRequest(MySurveillance ms, Socket socket) {
		
		this.ms = ms;
		this.socket = socket;
		
	};
	
	public void run() {
		
		Request request = new Request(socket);
		
		try {
			
			InputStream is = socket.getInputStream();
			
			byte[] raw = new byte[0];
			do {
				raw = ByteManager.append(raw, (byte)is.read());
			} while (is.available() > 0);
			
			String data = new String(raw);
			String[] dataLines = data.split("\n");
			
			if (dataLines.length > 0) {
				
				String[] head = dataLines[0].split(" ");
				if (head.length > 0) {
					String method = head[0];
					if (method.length() == 0) method = "GET";
					request.method(method);
				};
				if (head.length > 1) {
					String url = head[1];
					if (url.length() == 0) url = "/";
					request.url(url);
				};
				
				int offset = 0;
				for (int i = 1; i < dataLines.length; i++) {
					String[] parts = dataLines[i].split(" ");
					if (parts.length > 1) {
						if (parts[0].equals("Cookie:")) {
							String[] cooksRaw = dataLines[i].split("Cookie: ");
							if (cooksRaw.length == 1) continue;
							String cooks = cooksRaw[1];
							String[] keys = cooks.split(";");
							for (String key : keys) {
								String[] cs = key.split("=");
								if (cs.length == 1) continue;
								String val = "";
								for (int o = 1; o < cs.length; o++) {
									val += cs[o];
									if (o < cs.length-1) val += "=";
								};
								int last = (int)val.charAt(val.length()-1);
								if (last == 13) val = val.substring(0, val.length()-1);
								
								Cookie cookie = new Cookie(cs[0], val);
								request.cookies().addCookie(cookie);
							};
						};
					};
					if (parts.length == 1) {
						offset = i;
						break;
					};
				};
				
				if (offset > 0) {
					offset++;
					String inputData = "";
					for (int i = offset; i < dataLines.length; i++) {
						inputData += dataLines[i].replaceAll("[\r\f]", "");
						if (i != dataLines.length-1) inputData += "\n";
					};
					request.data(inputData);
				};
			};
			
			OutputStream os = socket.getOutputStream();
			
			User user = ms.accounts.getCookieUser(request);
			
			if (!request.query().containsKey("camera")) { //Gets called a lot when viewing cams, this will spam the logfile with requests
				Main.print(this, printType.REQUEST, "User: " + (user != null ? user.getUsername() : "") + ",\nIP: " + request.ip() + ",\nMethod: " + request.method() + ",\nUrl: " + request.url());
			};
			
			Page page;
			if (user == null && !request.url().equals("/login")) {
				page = new Page();
				page.setStatus(307);
				page.setContentType(ContentType.NONE);
				page.setHeader("Location", "/login");
			} else if (user != null && request.url().equals("/login")) {
				page = new Page();
				page.setStatus(307);
				page.setContentType(ContentType.NONE);
				page.setHeader("Location", "/");
			} else if (request.url().endsWith(".ico") || request.url().endsWith(".png")) {
				FileType ft = FileType.PNG;
				ContentType ct = ContentType.PNG;
				if (request.url().endsWith(".ico")) {
					ft = FileType.ICO;
					ct = ContentType.ICO;
				} else if (request.url().endsWith(".png")) {
					ft = FileType.PNG;
					ct = ContentType.PNG;
				};
				
				int index = request.url().lastIndexOf("/");
				byte[] image = new byte[0];
				int status = 404;
				if (index != -1) {
					String name = request.url().substring(index+1, request.url().length() - 4);
					image = ms.website.loadFile(ft, name);
					if (image.length > 0) status = 200;
				};
				byte[] s = new byte[0];
				s = ByteManager.append(s, "HTTP/1.1 " + Integer.toString(status) + "\nServer: MySurveillance");
				s = ByteManager.append(s, "\nContent-Type: " + ct.toString());
				s = ByteManager.append(s, "\n\n");
				if (image.length > 0) s = ByteManager.append(s, image);
				page = new Page();
				page.setRawData(s);
			} else {
				page = ms.website.getPage(request, user);
			};
			
			byte[] s = ms.website.stream(page);
			for (int o = 0; o < s.length; o += bytesPerWrite) {
				if (o + bytesPerWrite > s.length) {
					os.write(s, o, s.length-o);
				} else {
					os.write(s, o, bytesPerWrite);
				};
			};
			
			socket.close();
			
		} catch(SocketException e) {
			Main.print(this, printType.WARNING, "SockedException");
		} catch(IOException e) {
			Main.print(this, "Request error", e);
		};
		
	};
	
};