package com.dutch_computer_technology.mySurveillance.website;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class OnRequest extends Thread {
	
	private MySurveillance ms;
	private Socket socket;
	
	public OnRequest(MySurveillance ms, Socket socket) {
		
		this.ms = ms;
		this.socket = socket;
		
	};
	
	@SuppressWarnings("unchecked")
	public void run() {
		
		String method = "GET";
		String url = "/";
		JSONObject cookies = new JSONObject();
		String inputData = "";
		
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
					method = head[0];
					if (method.length() == 0) method = "GET";
				};
				if (head.length > 1) {
					url = head[1];
					if (url.length() == 0) url = "/";
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
								cookies.put(cs[0], val);
							};
						};
					};
					if (parts.length == 1) {
						offset = i;
						break;
					};
				};
				
				if (offset > 0) {
					for (int i = offset; i < dataLines.length; i++) {
						inputData += dataLines[i];
						if (i < dataLines.length-1) inputData += "\n";
					};
				};
			};
			
			OutputStream os = socket.getOutputStream();
			
			User user = ms.accounts.getCookieUser(cookies);
			Page page;
			if (user == null && !url.equals("/login")) {
				page = new Page();
				page.setStatus(307);
				page.setContentType(ContentTypes.NONE);
				page.setHeader("Location", "/login");
			} else if (user != null && url.equals("/login")) {
				page = new Page();
				page.setStatus(307);
				page.setContentType(ContentTypes.NONE);
				page.setHeader("Location", "/");
			} else if (url.endsWith(".ico") || url.endsWith(".png")) {
				FileType ft = FileType.PNG;
				ContentTypes ct = ContentTypes.PNG;
				if (url.endsWith(".ico")) {
					ft = FileType.ICO;
					ct = ContentTypes.ICO;
				} else if (url.endsWith(".png")) {
					ft = FileType.PNG;
					ct = ContentTypes.PNG;
				};
				
				int index = url.lastIndexOf("/");
				byte[] image = new byte[0];
				int status = 404;
				if (index != -1) {
					String name = url.substring(index+1, url.length() - 4);
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
				page = ms.website.getPage(method, url, cookies, user, inputData);
			};
			
			byte[] s = ms.website.stream(page);
			for (int o = 0; o < s.length; o += 4096) {
				if (o + 4096 > s.length) {
					os.write(s, o, s.length-o);
				} else {
					os.write(s, o, 4096);
				};
			};
			
			socket.close();
			
		} catch(SocketException ignore) {
		} catch(IOException e) {
			e.printStackTrace();
		};
		
	};
	
};