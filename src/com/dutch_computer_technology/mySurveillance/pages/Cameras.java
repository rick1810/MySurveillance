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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.cameras.CameraTypes;
import com.dutch_computer_technology.mySurveillance.cameras.StreamTypes;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.Random;
import com.dutch_computer_technology.mySurveillance.website.ContentTypes;
import com.dutch_computer_technology.mySurveillance.website.Page;
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
	
	public Page request(MySurveillance ms, String method, String url, JSONObject cookies, User user, String data) {
		
		if (method.equals("GET")) {
			
			Map<String, String> query = ms.website.getQuery(url);
			if (query.containsKey("camera")) {
				
				String name = query.get("camera");
				name = name.split(".mjpg")[0];
				Camera cam = ms.cameras.getCamera(name);
				
				Page page = new Page();
				if (cam == null) {
					page.setStatus(404);
					page.setData("Camera: " + name + " not found!");
				} else {
					byte[] s = new byte[0];
					s = ByteManager.append(s, "HTTP/1.1 200 OK\nServer: MySurveillance");
					s = ByteManager.append(s, "\nContent-Type: " + ContentTypes.JPEG.toString());
					s = ByteManager.append(s, "\nConnection: Close\nCache-Control: no-cache, no-store\n\n");
					byte[] stream = cam.getStream();
					if (stream == null || !cam.isRunning()) {
						LocalDateTime now = LocalDateTime.now();
						BufferedImage defImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						Graphics2D g = defImage.createGraphics();
						g.setFont(new Font("Serif", Font.PLAIN, 11));
						g.setColor(Color.white);
						g.drawString(getDate(now), 3, 11);
						g.setFont(new Font("Serif", Font.PLAIN, 32));
						if (now.getSecond() % 2 == 0) g.setColor(Color.red);
						g.drawString("No Video", (width/2)-70, (height/2)+6);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						try {
							ImageIO.write(defImage, "jpg", baos);
						} catch (IOException e) {
							e.printStackTrace();
						};
						s = ByteManager.append(s, baos.toByteArray());
					} else {
						if (cam.isGlitch()) {
							if (Math.floor(Math.random() * 20) == 0) {
								int index = (int) Math.floor(Math.random() * stream.length);
								int size = (int) Math.floor(Math.random() * 4096) + 1;
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
					page.setRawData(s);
				};
				
				return page;
				
			};
			
			Page page = new Page();
			page.setStatus(200);
			page.setContentType(ContentTypes.HTML);
			
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
						+ "<div class=\"info\"><p>Status: " + (cam.isOnline() ? (cam.isGlitch() ? "Glitch" : "Online") : "Offline") + "</p></div>"
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
			for (CameraTypes type : CameraTypes.values()) {
				cameraTypes += "\"" + type.toString() + "\",";
			};
			cameraTypes = cameraTypes.substring(0, cameraTypes.length() - 1);
			
			String streamTypes = "";
			for (StreamTypes type : StreamTypes.values()) {
				streamTypes += "\"" + type.toString() + "\",";
			};
			streamTypes = streamTypes.substring(0, streamTypes.length() - 1);
			
			html = ms.website.replaceHTML(html, "<cameraTypes>", cameraTypes);
			html = ms.website.replaceHTML(html, "<streamTypes>", streamTypes);
			
			page.setData(html);
			return page;
			
		} else if (method.equals("POST")) {
			
			try {
				
				JSONObject json = (JSONObject) new JSONParser().parse(data);
				if (!json.containsKey("cmd")) {
					Page page = new Page();
					page.setStatus(400);
					page.setData("Data incomplete");
					return page;
				};
				
				Object oCmd = json.get("cmd");
				if (!(oCmd instanceof String)) {
					Page page = new Page();
					page.setStatus(400);
					page.setData("Data incomplete");
					return page;
				};
				
				String cmd = (String) oCmd;
				if (cmd.equals("power")) {
					
					Object oName = json.get("name");
					if (!(oName instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					String name = (String) oName;
					Camera cam = ms.cameras.getCamera(name);
					if (cam == null) {
						Page page = new Page();
						page.setStatus(404);
						page.setData("Camera not found");
						return page;
					};
					
					cam.isOnline(!cam.isOnline());
					ms.cameras.save();
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				} else if (cmd.equals("glitch")) {
					
					Object oName = json.get("name");
					if (!(oName instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					String name = (String) oName;
					Camera cam = ms.cameras.getCamera(name);
					if (cam == null) {
						Page page = new Page();
						page.setStatus(404);
						page.setData("Camera not found");
						return page;
					};
					
					cam.isGlitch(!cam.isGlitch());
					ms.cameras.save();
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				} else if (cmd.equals("min")) {
					
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
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				} else if (cmd.equals("plus")) {
					
					ms.screens.add("");
					int min = ms.screens.size();
					double sqr = Math.sqrt(min);
					while (sqr % 1 != 0) {
						min++;
						ms.screens.add("");
						sqr = Math.sqrt(min);
					};
					ms.save();
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				} else if (cmd.equals("setScreen")) {
					
					Object oScreen = json.get("screen");
					if (!(oScreen instanceof Long)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					Object oName = json.get("name");
					if (!(oName instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					int screen = ((Long) oScreen).intValue();
					if (screen > ms.screens.size()) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Screen does not exist");
						return page;
					};
					
					String name = (String) oName;
					ms.screens.set(screen, name);
					ms.save();
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				} else if (cmd.equals("save")) {
					
					Object oData = json.get("data");
					if (!(oData instanceof JSONObject)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					JSONObject json_data = (JSONObject) oData;
					
					Object oOldName = json_data.get("oldName");
					if (!(oOldName instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					Object oName = json_data.get("name");
					if (!(oName instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					Camera cam = ms.cameras.getCamera((String) oOldName);
					if (cam == null) cam = new Camera((String) oOldName);
					
					if (!((String) oOldName).equals( ((String) oName)) ) {
						if (ms.cameras.getCamera((String) oName) != null) {
							Page page = new Page();
							page.setStatus(400);
							page.setData("Name already in-use");
							return page;
						};
					};
					
					cam.setName((String) oName);
					
					Object oAddress = json_data.get("address");
					if (oAddress instanceof String) cam.setAddress((String) oAddress);
					
					Object oUsername = json_data.get("username");
					if (oUsername instanceof String) cam.setUsername((String) oUsername);
					
					Object oPassword = json_data.get("password");
					if (oPassword instanceof String) cam.setPassword((String) oPassword);
					
					Object oCameraType = json_data.get("cameraType");
					if (oCameraType instanceof String) {
						CameraTypes cType = CameraTypes.valueOf((String) oCameraType);
						if (cType == null) cam.setType(cType);
					};
					
					Object oStreamType = json_data.get("streamType");
					if (oStreamType instanceof String) {
						StreamTypes sType = StreamTypes.valueOf((String) oStreamType);
						if (sType == null) cam.setStreamType(sType);
					};
					
					Object oStreamAddress = json_data.get("streamAddress");
					if (oStreamAddress instanceof String) cam.setStreamAddress((String) oStreamAddress);
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				} else if (cmd.equals("remove")) {
					
					Object oName = json.get("name");
					if (!(oName instanceof String)) {
						Page page = new Page();
						page.setStatus(400);
						page.setData("Data incomplete");
						return page;
					};
					
					ms.cameras.remCamera((String) oName);
					
					Page page = new Page();
					page.setStatus(200);
					return page;
					
				};
				
			} catch (ParseException e) {
				e.printStackTrace();
				Page page = new Page();
				page.setStatus(400);
				page.setData("Can't read data");
				return page;
			};
			
		};
		
		return null;
		
	};
	
};