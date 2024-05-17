package com.dutch_computer_technology.mySurveillance.cameras.streams;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.main.ByteManager;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class MJPEG extends Thread {
	
	private final byte[] empty = ("\r\n\r\n").getBytes();
	private final byte[] xTimestamp = ("X-Timestamp: ").getBytes();
	private final long timeout = 30000;
	
	private MySurveillance ms;
	private Camera cam;
	private String address;
	private String streamAddress;
	private String username;
	private String password;
	
	public MJPEG(MySurveillance ms, Camera cam, String address, String streamAddress, String username, String password) {
		
		this.ms = ms;
		this.cam = cam;
		this.address = address;
		this.streamAddress = streamAddress;
		this.username = username;
		this.password = password;
		
	};
	
	private void record() {
		
		if (cam == null) return;
		
		LocalDateTime now = LocalDateTime.now();
		int min = now.getMinute();
		String sMon = Integer.toString(now.getMonthValue());
		if (sMon.length() < 2) sMon = "0" + sMon;
		String sDay = Integer.toString(now.getDayOfMonth());
		if (sDay.length() < 2) sDay = "0" + sDay;
		String sHour = Integer.toString(now.getHour());
		if (sHour.length() < 2) sHour = "0" + sHour;
		String sMin = "00";
		if (min > 44) {
			sMin = "45";
		} else if (min > 29) {
			sMin = "30";
		} else if (min > 14) {
			sMin = "15";
		};
		
		byte[] stream = cam.getStream().clone();
		if (stream == null) return;
		if (stream.length == 0) return;
		
		String name = cam.getName() + Main.slash() +  (now.getYear() + "-" + sMon + "-" + sDay + " " + sHour + sMin) + ".mjpeg";
		ms.fileManager.addData(cam, name, stream);
		
	};
	
	public void run() {
		
		if (cam == null) return;
		cam.isRunning(true);
		
		try {
			
			Socket socket = new Socket(address, 80);
			
			if (!socket.isConnected()) {
				socket.close();
				if (cam != null) cam.isRunning(false);
				return;
			};
			
			OutputStream out = socket.getOutputStream();
			if (out == null) {
				socket.close();
				if (cam != null) cam.isRunning(false);
				return;
			};
			
			byte[] s = new byte[0];
			s = ByteManager.append(s, "GET " + streamAddress + " HTTP/1.1\n");
			String base = new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
			s = ByteManager.append(s, "Authorization: Basic " + base + "\n");
			s = ByteManager.append(s, "\n\n");
			
			socket.getOutputStream().write(s);
			InputStream is = socket.getInputStream();
			if (is == null) {
				socket.close();
				if (cam != null) cam.isRunning(false);
				return;
			};
			
			boolean headers = true;
			byte[] bound = ("\r\n--myboundary").getBytes();
			byte[] data = new byte[0];
			long time = new Date().getTime();
			long xTime = time;
			int status = 0;
			
			do {
				
				data = ByteManager.append(data, is.readNBytes(is.available()));
				
				if (cam == null) break;
				if (!cam.isOnline()) break;
				
				if (headers) {
					
					int hIndex = ByteManager.indexOf(data, empty);
					if (hIndex == -1) continue;
					
					int spIndex = ByteManager.indexOf(data, " ".getBytes());
					if (spIndex != -1) {
						int speIndex = ByteManager.indexOf(data, " ".getBytes(), spIndex+1);
						if (speIndex != -1) {
							try {
								status = Integer.valueOf(new String(ByteManager.between(data, spIndex+1, speIndex)));
							} catch(NumberFormatException ignore) {};
						};
					};
					
					int cIndex = ByteManager.indexOf(data, "Content-Type: ".getBytes());
					if (cIndex != -1) {
						byte[] co = (";boundary=").getBytes();
						int coIndex = ByteManager.indexOf(data, co, cIndex);
						if (coIndex != -1) {
							int lIndex = ByteManager.indexOf(data, "\r\n".getBytes(), coIndex);
							if (lIndex != -1) {
								bound = ("\r\n").getBytes();
								bound = ByteManager.append(bound, ByteManager.between(data, coIndex+co.length, lIndex));
							};
						};
					};
					
					data = ByteManager.between(data, hIndex);
					headers = false;
					
					if (status != 200) break;
					
				};
				
				long now = new Date().getTime();
				if (now-time > timeout) break;
				
				int bIndex = ByteManager.indexOf(data, bound);
				if (bIndex == -1) continue;
				
				int eIndex = ByteManager.indexOf(data, empty, bIndex);
				if (eIndex == -1) continue;
				
				int sbIndex = ByteManager.indexOf(data, empty, eIndex+empty.length);
				if (sbIndex == -1) continue;
				
				byte[] boundInfo = ByteManager.between(data, bIndex, eIndex);
				int xIndex = ByteManager.indexOf(boundInfo, xTimestamp);
				if (xIndex != -1) {
					int xeIndex = ByteManager.indexOf(boundInfo, "\r\n".getBytes(), xIndex);
					if (xeIndex != -1) {
						try {
							xTime = Long.valueOf(new String(ByteManager.between(boundInfo, xIndex+xTimestamp.length, xeIndex)));
						} catch(NumberFormatException ignore) {};
					};
				};
				
				if (xTime < time) {
					data = ByteManager.delete(data, 0, sbIndex);
					time = now;
					continue;
				};
				
				byte[] stream = ByteManager.between(data, eIndex+empty.length, sbIndex);
				if (cam == null) break;
				if (!cam.isOnline()) break;
				cam.setStream(stream);
				record();
				data = ByteManager.delete(data, 0, sbIndex);
				time = now;
				xTime = now;
				
			} while(socket.isConnected() && !socket.isInputShutdown() && ms.run);
			if (socket.isConnected()) socket.close();
			if (cam != null) cam.isRunning(false);
			
		} catch (ConnectException ignore) {
			if (cam != null) cam.isRunning(false);
		} catch (SocketException ignore) {
			if (cam != null) cam.isRunning(false);
		} catch (Exception e) {
			if (cam != null) cam.isRunning(false);
			Main.print(this, e);
		};
		
	};
	
};