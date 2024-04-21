package com.dutch_computer_technology.mySurveillance.cameras;

import java.util.ArrayList;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.cameras.streams.MJPEG;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class StreamManager extends Thread {
	
	MySurveillance ms;
	Cameras cameras;
	
	public StreamManager(MySurveillance ms, Cameras cameras) {
		
		this.ms = ms;
		this.cameras = cameras;
		
	};
	
	public void run() {
		
		while (ms.run) {
			
			List<String> names = new ArrayList<String>(cameras.getCameras());
			for (String name : names) {
				
				Camera cam = cameras.getCamera(name);
				if (cam == null) continue;
				
				if (!cam.isOnline()) continue;
				if (cam.isRunning()) continue;
				
				String address = cam.getAddress();
				if (address == null) continue;
				
				String streamAddress = cam.getStreamAddress();
				if (streamAddress == null) continue;
				
				StreamTypes streamType = cam.getStreamType();
				if (streamType == null) continue;
				
				String username = cam.getUsername();
				if (username == null) continue;
				
				String password = cam.getPassword();
				if (password == null) continue;
				
				if (streamType.equals(StreamTypes.MJPEG)) {
					
					MJPEG stream = new MJPEG(ms, cam, address, streamAddress, username, password);
					stream.start();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
					
				};
				
			};
			
		};
		
	};
	
};