package com.dutch_computer_technology.mySurveillance.cameras;

import java.util.ArrayList;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.streams.Disabled;
import com.dutch_computer_technology.mySurveillance.cameras.streams.MJPEG;
import com.dutch_computer_technology.mySurveillance.cameras.streams.Stream;
import com.dutch_computer_technology.JSONManager.data.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class StreamManager extends Thread {
	
	MySurveillance ms;
	
	public StreamManager(MySurveillance ms) {
		
		this.ms = ms;
		
	};
	
	public enum StreamType {
		Disabled,
		MJPEG
	};
	
	public Stream createStream(StreamType type, Camera cam) {
		if (type == null) return null;
		switch(type) {
			case Disabled:
				return new Disabled(ms, cam);
			case MJPEG:
				return new MJPEG(ms, cam);
			default:
				break;
		};
		return null;
	};
	
	public Stream createStream(StreamType type, Camera cam, JSONObject json) {
		if (type == null) return null;
		switch(type) {
			case Disabled:
				return new Disabled(ms, cam, json);
			case MJPEG:
				return new MJPEG(ms, cam, json);
			default:
				break;
		};
		return null;
	};
	
	public Stream getStream(Camera cam, StreamType type) {
		if (cam == null) return createStream(type, null);
		Stream stream = cam.getStream();
		if (stream != null) {
			if (stream.getType().equals(type)) {
				return stream;
			};
		};
		return createStream(type, cam);
	};
	
	public void run() {
		
		while (ms.run) {
			if (ms.cameras != null) break;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignore) {};
		};
		
		while (ms.run) {
			
			List<String> names = new ArrayList<String>(ms.cameras.getCameras());
			for (String name : names) {
				
				Camera cam = ms.cameras.getCamera(name);
				if (cam == null) continue;
				
				if (!cam.isOnline()) continue;
				if (cam.isRunning()) continue;
				
				Stream stream = cam.getStream();
				if (stream.running()) continue;
				if (!stream.test()) continue;
				stream.start();
				
			};
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Main.print(this, e);
			};
			
		};
		
	};
	
};