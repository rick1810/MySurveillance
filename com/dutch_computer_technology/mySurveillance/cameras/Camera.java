package com.dutch_computer_technology.mySurveillance.cameras;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Cameras.CameraType;
import com.dutch_computer_technology.mySurveillance.cameras.StreamManager.StreamType;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Path;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Relative;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.MaxSize;
import com.dutch_computer_technology.mySurveillance.cameras.reasons.Reason;
import com.dutch_computer_technology.mySurveillance.exceptions.CameraParse;
import com.dutch_computer_technology.mySurveillance.main.FileManager.PathType;
import com.dutch_computer_technology.mySurveillance.main.FileManager.ReasonType;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public class Camera {
	
	private String name;
	private String address;
	private CameraType cameraType;
	private StreamType streamType;
	private String streamAddress;
	
	private Path path;
	private Reason reason;
	
	private String username;
	private String password;
	
	private boolean online;
	private boolean running;
	private boolean glitch;
	
	private byte[] stream;
	
	public Camera(MySurveillance ms, String name) {
		
		this.name = name;
		Path path = new Relative(ms);
		path.setPath(Main.path(Main.defaultSaveLocation) + Main.slash() + name);
		this.path = path;
		this.reason = new MaxSize(ms, this);
		
	};
	
	private Path getDefaultPath(MySurveillance ms) {
		Relative path = new Relative(ms);
		path.setPath(Main.path(Main.defaultSaveLocation) + Main.slash() + name);
		return path;
	};
	
	private Path getPath(MySurveillance ms, JSONObject json) {
		
		if (!json.containsKey("path")) return getDefaultPath(ms);
		
		Object oPath = json.get("path");
		if (!(oPath instanceof JSONObject)) return getDefaultPath(ms);
		
		JSONObject path = (JSONObject)oPath;
		if (!path.containsKey("type")) return getDefaultPath(ms);
		
		Object oType = path.get("type");
		if (!(oType instanceof String)) return getDefaultPath(ms);
		
		PathType type;
		try {
			type = PathType.valueOf((String)oType);
		} catch(IllegalArgumentException ignore) {
			return getDefaultPath(ms);
		};
		
		return ms.fileManager.createPath(type, path);
		
	};
	
	private Reason getDefaultReason(MySurveillance ms, Camera cam) {
		return new MaxSize(ms, cam);
	};
	
	private Reason getReason(MySurveillance ms, Camera cam, JSONObject json) {
		
		if (!json.containsKey("reason")) return getDefaultReason(ms, cam);
		
		Object oReason = json.get("reason");
		if (!(oReason instanceof JSONObject)) return getDefaultReason(ms, cam);
		
		JSONObject reason = (JSONObject)oReason;
		if (!reason.containsKey("type")) return getDefaultReason(ms, cam);
		
		Object oType = reason.get("type");
		if (!(oType instanceof String)) return getDefaultReason(ms, cam);
		
		ReasonType type;
		try {
			type = ReasonType.valueOf((String)oType);
		} catch(IllegalArgumentException ignore) {
			return getDefaultReason(ms, cam);
		};
		
		return ms.fileManager.createReason(type, cam, reason);
		
	};
	
	public Camera(MySurveillance ms, JSONObject json) throws CameraParse {
		
		if (!json.containsKey("name")) throw new CameraParse("Can't load camera, missing name");
		
		this.name = (String) json.get("name");
		
		if (json.containsKey("address")) this.address = (String) json.get("address");
		if (json.containsKey("cameraType")) this.cameraType = CameraType.valueOf((String) json.get("cameraType"));
		if (json.containsKey("streamType")) this.streamType = StreamType.valueOf((String) json.get("streamType"));
		if (json.containsKey("streamAddress")) this.streamAddress = (String) json.get("streamAddress");
		this.path = getPath(ms, json);
		this.reason = getReason(ms, this, json);
		if (json.containsKey("username")) this.username = (String) json.get("username");
		if (json.containsKey("password")) this.password = (String) json.get("password");
		if (json.containsKey("online")) this.online = (Boolean) json.get("online");
		if (json.containsKey("glitch")) this.glitch = (Boolean) json.get("glitch");
		
	};
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		json.put("name", name);
		
		if (address != null) json.put("address", address);
		if (cameraType != null) json.put("cameraType", cameraType.toString());
		if (streamType != null) json.put("streamType", streamType.toString());
		if (streamAddress != null) json.put("streamAddress", streamAddress);
		if (path != null) json.put("path", path.toJSON());
		if (reason != null) json.put("reason", reason.toJSON());
		if (username != null) json.put("username", username);
		if (password != null) json.put("password", password);
		
		json.put("online", online);
		json.put("glitch", glitch);
		
		return json;
		
	};
	
	@Override
	public String toString() {
		JSONObject json = toJSON();
		return json.toJSONString();
	};
	
	public boolean equals(Camera cam) {
		return cam.getName().equals(name);
	};
	
	public boolean isOnline() {
		return online;
	};
	public void isOnline(boolean online) {
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
	
	public String getName() {
		return name;
	};
	public void setName(String name) {
		this.name = name;
	};
	
	public String getUsername() {
		return username;
	};
	public void setUsername(String username) {
		this.username = username;
	};
	
	public String getPassword() {
		return password;
	};
	public void setPassword(String password) {
		this.password = password;
	};
	
	public byte[] getStream() {
		return stream;
	};
	public void setStream(byte[] stream) {
		this.stream = stream;
	};
	
	public CameraType getType() {
		return cameraType;
	};
	public void setType(CameraType type) {
		this.cameraType = type;
	};
	
	public StreamType getStreamType() {
		return streamType;
	};
	public void setStreamType(StreamType type) {
		this.streamType = type;
	};
	
	public String getStreamAddress() {
		return streamAddress;
	};
	public void setStreamAddress(String streamAddress) {
		this.streamAddress = streamAddress;
	};
	
	public String getAddress() {
		return address;
	};
	public void setAddress(String address) {
		this.address = address;
	};
	
};