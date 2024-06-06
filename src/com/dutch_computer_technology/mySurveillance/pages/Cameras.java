package com.dutch_computer_technology.mySurveillance.pages;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.Main.printType;
import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.cameras.Cameras.CameraType;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager.StreamType;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Path;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.Reason;
import com.dutch_computer_technology.mySurveillance.cameras.streams.Stream;
import com.dutch_computer_technology.mySurveillance.exceptions.JSONParse;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;
import com.dutch_computer_technology.mySurveillance.main.FileManager.ReasonType;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.Random;
import com.dutch_computer_technology.mySurveillance.website.ContentType;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Cameras implements PageHandler {
	
	private String getDate(LocalDateTime now) {
		
		//Date
		String year = Integer.toString(now.getYear());
		String month = Integer.toString(now.getMonthValue());
		if (month.length() < 2) month = "0" + month;
		String day = Integer.toString(now.getDayOfMonth());
		if (day.length() < 2) day = "0" + day;
		//Time
		String hour = Integer.toString(now.getHour());
		if (hour.length() < 2) hour = "0" + hour;
		String min = Integer.toString(now.getMinute());
		if (min.length() < 2) min = "0" + min;
		String sec = Integer.toString(now.getSecond());
		if (sec.length() < 2) sec = "0" + sec;
		
		return year + "/" + month + "/" + day + " " + hour + ":" + min + ":" + sec;
		
	};
	
	private final int width = 320;
	private final int height = 176;
	
	private byte[] getDefaultStream(MySurveillance ms) {
		
		LocalDateTime now = LocalDateTime.now();
		BufferedImage defImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = defImage.createGraphics();
		g.setFont(new Font("Serif", Font.PLAIN, 11));
		g.setColor(Color.white);
		g.drawString(getDate(now), 3, 11);
		g.setFont(new Font("Serif", Font.PLAIN, 32));
		if (now.getSecond() % 2 == 0) g.setColor(Color.red);
		
		String text = ms.lang.get("cameras.noCam");
		int x = (int) (text.length() * 8.75);
		g.drawString(text, (width/2)-x, (height/2)+6);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(defImage, "jpg", baos);
		} catch (IOException e) {
			Main.print(this, "Could not create default stream", e);
		};
		return baos.toByteArray();
		
	};
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		Map<String, String> query = request.query();
		if (query.containsKey("camera")) {
			
			String name = query.get("camera");
			Camera cam = ms.cameras.getCamera(name);
			
			Page page = new Page();
			byte[] s = new byte[0];
			if (cam == null) {
				s = ByteManager.append(s, "HTTP/1.1 404 Not Found\r\nServer: MySurveillance");
				s = ByteManager.append(s, "\r\nContent-Type: " + ContentType.JPEG.toString());
				s = ByteManager.append(s, "\r\nConnection: Close\r\nCache-Control: no-cache, no-store\r\n\r\n");
				s = ByteManager.append(s, getDefaultStream(ms));
			} else {
				s = ByteManager.append(s, "HTTP/1.1 200 OK\r\nServer: MySurveillance");
				s = ByteManager.append(s, "\r\nContent-Type: " + ContentType.JPEG.toString());
				s = ByteManager.append(s, "\r\nConnection: Close\r\nCache-Control: no-cache, no-store\r\n\r\n");
				byte[] stream = cam.getStreamData();
				if (stream == null || !cam.isRunning()) {
					s = ByteManager.append(s, getDefaultStream(ms));
				} else {
					if (cam.isGlitch()) {
						Random ran = new Random();
						if (ran.action(20)) {
							int index = ran.randomInt(stream.length);
							int size = ran.randomInt(1, 4096);
							byte[] a = ByteManager.between(stream, 0, index);
							byte[] b = ByteManager.between(stream, index+size, stream.length);
							stream = new byte[0];
							Random r = new Random();
							stream = ByteManager.append(stream, a);
							stream = ByteManager.append(stream, r.id(size).getBytes());
							stream = ByteManager.append(stream, b);
						};
					};
					s = ByteManager.append(s, stream);
				};
			};
			page.setRawData(s);
			return page;
			
		};
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.HTML, ms.lang.get("unauthorized.privileges"));
		
		Page page = new Page();
		page.setStatus(200);
		page.setContentType(ContentType.HTML);
		
		String html = new String(ms.website.loadFile(FileType.HTML, "cameras"));
		
		String screens = "<div class=\"screens\">";
		int min = ms.screens.size();
		double sqr = Math.sqrt(min);
		while (sqr % 1 != 0) {
			min++;
			ms.screens.add("");
			sqr = Math.sqrt(min);
		};
		double pro = 100 / sqr;
		int font = 22;
		String size = "calc(" + Double.toString(pro) + "% - 2px);";
		for (int i = 0; i < ms.screens.size() || i < min; i++) {
			String name = "";
			if (i < ms.screens.size()) {
				name = ms.screens.get(i);
			};
			screens += "<div style=\"width: " + size + "; height: " + size + ";\" class=\"screen\"><div class=\"button\" onclick=\"setScreen(event, " + i + ", false);\"></div><h3 class=\"title\" style=\"font-size: " + font + "px;\">" + name + "</h3></div>";
		};
		screens += "</div>";
		html = ms.website.replaceHTML(html, "<min>", Integer.toString(min));
		html = ms.website.replaceHTML(html, "<screens>", screens);
		
		String camNames = "\"\"";
		String camerasJSON = "";
		String cams = "<div class=\"cameras\"><div class=\"newCamera\"><button onclick=\"openSettings(false);\"><img src=\"new.png\"/></button></div><div class=\"divider\"></div>";
		List<String> names = new ArrayList<String>(ms.cameras.getCameras());
		for (String name : names) {
			Camera cam = ms.cameras.getCamera(name);
			if (cam == null) continue;
			camNames += ",\"" + name + "\"";
			cams += "<div class=\"camera\">"
					+ "<div class=\"view\"><canvas src=\"/cameras?camera=" + name + "\"></canvas></div>"
					+ "<div class=\"info\"><p>Name: " + name + "</p><p>Status: " + (cam.isOnline() ? (cam.isGlitch() ? "Glitch" : "Online") : "Offline") + "</p></div>"
					+ "<div class=\"options\"><button onclick=\"openSettings('" + name + "');\"><img src=\"settings.png\"/></button>"
					+ "<button onclick=\"power('" + name + "');\"><img src=\"power.png\"/></button>"
					+ "<button onclick=\"glitch('" + name + "');\"><img src=\"glitch.png\"/></button></div>"
					+ "</div>";
			cams += "<div class=\"divider\"></div>";
			camerasJSON += "\"" + cam.getName() + "\":" + cam.toString() + ",";
		};
		camerasJSON = camerasJSON.substring(0, camerasJSON.length() - 1);
		cams = cams.substring(0, cams.length() - 27);
		cams += "</div>";
		html = ms.website.replaceHTML(html, "<cameras>", cams);
		
		html = ms.website.loadHTML(html);
		html = ms.website.replaceHTML(html, "<camerasJSON>", camerasJSON);
		html = ms.website.replaceHTML(html, "<camNames>", camNames);
		
		String cameraTypes = "";
		for (CameraType type : CameraType.values()) {
			cameraTypes += "\"" + type.toString() + "\",";
		};
		cameraTypes = cameraTypes.substring(0, cameraTypes.length() - 1);
		
		String streamTypes = "";
		for (StreamType type : StreamType.values()) {
			streamTypes += "\"" + type.toString() + "\",";
		};
		streamTypes = streamTypes.substring(0, streamTypes.length() - 1);
		
		String pathTypes = "";
		for (PathType type : PathType.values()) {
			pathTypes += "\"" + type.toString() + "\",";
		};
		pathTypes = pathTypes.substring(0, pathTypes.length() - 1);
		
		String reasonTypes = "";
		for (ReasonType type : ReasonType.values()) {
			reasonTypes += "\"" + type.toString() + "\",";
		};
		reasonTypes = reasonTypes.substring(0, reasonTypes.length() - 1);
		
		html = ms.website.replaceHTML(html, "<cameraTypes>", cameraTypes);
		html = ms.website.replaceHTML(html, "<streamTypes>", streamTypes);
		html = ms.website.replaceHTML(html, "<pathTypes>", pathTypes);
		html = ms.website.replaceHTML(html, "<reasonTypes>", reasonTypes);
		
		page.setData(html);
		return page;
		
	};
	
	public Page post(MySurveillance ms, Request request, User user) {
		
		if (!user.isAdmin()) return ms.website.unauthorizedPage(ContentType.TEXT, ms.lang.get("unauthorized.privileges"));
		
		try {
			
			JSONObject json = new JSONObject(request.data());
			
			String cmd = json.getString("cmd");
			if (cmd == null) {
				Page page = new Page();
				page.setStatus(400);
				page.setData(ms.lang.get("data.incomplete"));
				return page;
			};
			
			Main.print(this, printType.INFO, "Admin " + user.getUsername() + " on IP: " + request.ip() + ", used the following command: " + cmd);
			
			if (cmd.equals("getStream")) {
				
				String typeS = json.getString("type");
				StreamType type = StreamType.Disabled;
				if (typeS != null) {
					try {
						type = StreamType.valueOf(typeS);
					} catch(IllegalArgumentException ignore) {};
				};
				
				if (type == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.streamTypeNotFound"));
					return page;
				};
				
				String name = json.getString("name");
				Camera cam = null;
				if (name != null) cam = ms.cameras.getCamera(name);
				
				Stream stream = ms.streamManager.getStream(cam, type);
				if (stream == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.streamNotFound"));
					return page;
				};
				
				Page page = new Page();
				page.setStatus(200);
				page.setContentType(ContentType.HTML);
				page.setData(stream.html());
				
				return page;
				
			};
			
			if (cmd.equals("getPath")) {
				
				String typeS = json.getString("type");
				PathType type = PathType.Relative;
				if (typeS != null) {
					try {
						type = PathType.valueOf(typeS);
					} catch(IllegalArgumentException ignore) {};
				};
				
				if (type == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.pathTypeNotFound"));
					return page;
				};
				
				String name = json.getString("name");
				Camera cam = null;
				if (name != null) cam = ms.cameras.getCamera(name);
				
				Path path = ms.fileManager.getPath(cam, type);
				if (path == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.pathNotFound"));
					return page;
				};
				
				Page page = new Page();
				page.setStatus(200);
				page.setContentType(ContentType.HTML);
				page.setData(path.html());
				
				return page;
				
			};
			
			if (cmd.equals("getReason")) {
				
				String typeS = json.getString("type");
				ReasonType type = ReasonType.MaxSize;
				if (typeS != null) {
					try {
						type = ReasonType.valueOf(typeS);
					} catch(IllegalArgumentException ignore) {};
				};
				
				if (type == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.reasonTypeNotFound"));
					return page;
				};
				
				String name = json.getString("name");
				Camera cam = null;
				if (name != null) cam = ms.cameras.getCamera(name);
				
				Reason reason = ms.fileManager.getReason(cam, type);
				if (reason == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.reasonNotFound"));
					return page;
				};
				
				Page page = new Page();
				page.setStatus(200);
				page.setContentType(ContentType.HTML);
				page.setData(reason.html());
				
				return page;
				
			};
			
			if (cmd.equals("power")) {
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				Camera cam = ms.cameras.getCamera(name);
				if (cam == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.camNotFound"));
					return page;
				};
				
				cam.isOnline(!cam.isOnline());
				ms.cameras.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("glitch")) {
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				Camera cam = ms.cameras.getCamera(name);
				if (cam == null) {
					Page page = new Page();
					page.setStatus(404);
					page.setData(ms.lang.get("cameras.camNotFound"));
					return page;
				};
				
				cam.isGlitch(!cam.isGlitch());
				ms.cameras.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("min")) {
				
				if (ms.screens.size() < 2) {
					Page page = new Page();
					page.setStatus(403);
					return page;
				};
				
				ms.screens.remove(ms.screens.size() - 1);
				int min = ms.screens.size();
				double sqr = Math.sqrt(min);
				while (sqr % 1 != 0) {
					min--;
					ms.screens.remove(ms.screens.size() - 1);
					sqr = Math.sqrt(min);
				};
				ms.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("plus")) {
				
				ms.screens.add("");
				int min = ms.screens.size();
				double sqr = Math.sqrt(min);
				while (sqr % 1 != 0) {
					min++;
					ms.screens.add("");
					sqr = Math.sqrt(min);
				};
				ms.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("setScreen")) {
				
				if (!json.contains("screen")) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				int screen = json.getInt("screen");
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				if (screen > ms.screens.size()) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("cameras.screenNotFound"));
					return page;
				};
				
				ms.screens.set(screen, name);
				ms.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("save")) {
				
				JSONObject data = json.getJSONObject("data");
				if (data == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				String name = data.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				String oldName = data.getString("oldName");
				if (oldName == null) oldName = name;
				
				Camera cam = ms.cameras.getCamera(oldName);
				if (cam == null) cam = new Camera(ms, oldName);
				
				if (!oldName.equals(name)) {
					if (ms.cameras.getCamera(name) != null) {
						Page page = new Page();
						page.setStatus(400);
						page.setData(ms.lang.get("accounts.nameUsed"));
						return page;
					};
				};
				
				cam.setName(name);
				
				String cameraType = data.getString("cameraType");
				if (cameraType != null) {
					try {
						CameraType cType = CameraType.valueOf(cameraType);
						cam.setType(cType);
					} catch(IllegalArgumentException ignore) {};
				};
				
				JSONObject stream = data.getJSONObject("stream");
				if (stream != null) {
					String type = stream.getString("type");
					if (type != null) {
						StreamType sType = null;
						try {
							sType = StreamType.valueOf(type);
						} catch(IllegalArgumentException ignore) {};
						if (sType != null) {
							cam.setStream(ms.streamManager.createStream(sType, cam, stream));
						};
					};
				};
				
				JSONObject path = data.getJSONObject("path");
				if (path != null) {
					String type = path.getString("type");
					if (type != null) {
						PathType pType = null;
						try {
							pType = PathType.valueOf(type);
						} catch(IllegalArgumentException ignore) {};
						if (pType != null) {
							cam.setPath(ms.fileManager.createPath(pType, cam, path));
						};
					};
				};
				
				JSONObject reason = data.getJSONObject("reason");
				if (reason != null) {
					String type = path.getString("type");
					if (type != null) {
						ReasonType rType = null;
						try {
							rType = ReasonType.valueOf(type);
						} catch(IllegalArgumentException ignore) {};
						if (rType != null) {
							cam.setReason(ms.fileManager.createReason(rType, cam, reason));
						};
					};
				};
				
				ms.cameras.addCamera(cam);
				ms.cameras.save();
				
				return new Page();
				
			};
			
			if (cmd.equals("remove")) {
				
				String name = json.getString("name");
				if (name == null) {
					Page page = new Page();
					page.setStatus(400);
					page.setData(ms.lang.get("data.incomplete"));
					return page;
				};
				
				ms.cameras.remCamera(name);
				ms.cameras.save();
				
				return new Page();
				
			};
			
		} catch (JSONParse e) {
			Main.print(this, "Can't read data", e);
			Page page = new Page();
			page.setStatus(400);
			page.setData(ms.lang.get("data.parse"));
			return page;
		};
		
		return null;
		
	};
	
};