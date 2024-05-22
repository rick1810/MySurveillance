package com.dutch_computer_technology.mySurveillance.cameras;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Cameras.CameraType;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager.StreamType;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Path;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Relative;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.MaxSize;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.Reason;
import com.dutch_computer_technology.mySurveillance.cameras.streams.Disabled;
import com.dutch_computer_technology.mySurveillance.cameras.streams.Stream;
import com.dutch_computer_technology.mySurveillance.exceptions.CameraParse;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;
import com.dutch_computer_technology.mySurveillance.main.FileManager.ReasonType;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Camera {
	
	private String name;
	private CameraType cameraType;
	private boolean online;
	private boolean glitch;
	
	private Path path;
	private Reason reason;
	private Stream stream;
	
	private boolean running;
	private byte[] stream_data;
	
	public Camera(MySurveillance ms, String name) {
		
		this.name = name;
		Path path = new Relative(ms);
		path.setPath(Main.path(Main.defaultSaveLocation) + Main.slash() + name);
		this.path = path;
		this.reason = new MaxSize(ms, this);
		this.stream = new Disabled(ms, this);
		
	};
	
	private Path getDefaultPath(MySurveillance ms) {
		Relative path = new Relative(ms);
		path.setPath(Main.path(Main.defaultSaveLocation) + Main.slash() + name);
		return path;
	};
	
	private Path getPath(MySurveillance ms, JSONObject json) {
		
		if (!json.contains("path")) return getDefaultPath(ms);
		
		JSONObject path = json.getJSONObject("path");
		if (path == null) return getDefaultPath(ms);
		
		String typeS = path.getString("type");
		if (typeS == null) return getDefaultPath(ms);
		
		PathType type;
		try {
			type = PathType.valueOf(typeS);
		} catch(IllegalArgumentException ignore) {
			return getDefaultPath(ms);
		};
		
		return ms.fileManager.createPath(type, path);
		
	};
	
	private Reason getDefaultReason(MySurveillance ms, Camera cam) {
		return new MaxSize(ms, cam);
	};
	
	private Reason getReason(MySurveillance ms, Camera cam, JSONObject json) {
		
		if (!json.contains("reason")) return getDefaultReason(ms, cam);
		
		JSONObject reason = json.getJSONObject("reason");
		if (reason == null) return getDefaultReason(ms, cam);
		
		String typeS = reason.getString("type");
		if (typeS == null) return getDefaultReason(ms, cam);
		
		ReasonType type;
		try {
			type = ReasonType.valueOf(typeS);
		} catch(IllegalArgumentException ignore) {
			return getDefaultReason(ms, cam);
		};
		
		return ms.fileManager.createReason(type, cam, reason);
		
	};
	
	private Stream getDefaultStream(MySurveillance ms, Camera cam) {
		return new Disabled(ms, cam);
	};
	
	private Stream getStream(MySurveillance ms, Camera cam, JSONObject json) {
		
		if (!json.contains("stream")) return getDefaultStream(ms, cam);
		
		JSONObject stream = json.getJSONObject("stream");
		if (stream == null) return getDefaultStream(ms, cam);
		
		String typeS = stream.getString("type");
		if (typeS == null) return getDefaultStream(ms, cam);
		
		StreamType type;
		try {
			type = StreamType.valueOf(typeS);
		} catch(IllegalArgumentException ignore) {
			return getDefaultStream(ms, cam);
		};
		
		return ms.streamManager.createStream(type, cam, stream);
		
	};
	
	public Camera(MySurveillance ms, JSONObject json) throws CameraParse {
		
		if (json == null) throw new CameraParse("Can't load camera, null");
		
		String name = json.getString("name");
		if (name == null) throw new CameraParse("Can't load camera, missing name");
		this.name = name;
		
		if (json.contains("cameraType")) this.cameraType = CameraType.valueOf(json.getString("cameraType"));
		this.path = getPath(ms, json);
		this.reason = getReason(ms, this, json);
		this.stream = getStream(ms, this, json);
		if (json.contains("online")) this.online = json.getBoolean("online");
		if (json.contains("glitch")) this.glitch = json.getBoolean("glitch");
		
	};
	
	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		json.put("name", name);
		
		if (cameraType != null) json.put("cameraType", cameraType.toString());
		if (stream != null) json.put("stream", stream.toJSON());
		if (path != null) json.put("path", path.toJSON());
		if (reason != null) json.put("reason", reason.toJSON());
		
		json.put("online", online);
		json.put("glitch", glitch);
		
		return json;
		
	};
	
	@Override
	public String toString() {
		JSONObject json = toJSON();
		return json.toString();
	};
	
	public boolean equals(Camera cam) {
		return cam.getName().equals(name);
	};
	
	public String getName() {
		return name;
	};
	public void setName(String name) {
		this.name = name;
	};
	
	public CameraType getType() {
		return cameraType;
	};
	public void setType(CameraType type) {
		this.cameraType = type;
	};
	
	public boolean isOnline() {
		return online;
	};
	public void isOnline(boolean online) {
		if (!online && stream != null) stream.stop();
		this.online = online;
	};
	
	public boolean isRunning() {
		return running;
	};
	public void isRunning(boolean running) {
		this.running = running;
	};
	
	public boolean isGlitch() {
		return glitch;
	};
	public void isGlitch(boolean glitch) {
		this.glitch = glitch;
	};
	
	public Path getPath() {
		return path;
	};
	public void setPath(Path path) {
		this.path = path;
	};
	
	public Reason getReason() {
		return reason;
	};
	public void setReason(Reason reason) {
		this.reason = reason;
	};
	
	public Stream getStream() {
		return stream;
	};
	public void setStream(Stream stream) {
		if (this.stream != null) this.stream.stop();
		this.stream = stream;
	};
	
	public byte[] getStreamData() {
		return stream_data;
	};
	public void setStreamData(byte[] stream_data) {
		this.stream_data = stream_data;
	};
	
};