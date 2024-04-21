package com.dutch_computer_technology.mySurveillance.cameras;

import org.json.simple.JSONObject;

import com.dutch_computer_technology.mySurveillance.exceptions.CameraParse;

public class Camera {
	
	private String name;
	private String address;
	private CameraTypes cameraType;
	private StreamTypes streamType;
	private String streamAddress;
	
	private String username;
	private String password;
	
	private boolean online;
	private boolean running;
	private boolean glitch;
	
	private byte[] stream;
	
	public Camera(String name) {
		
		this.name = name;
		
	};
	
	public Camera(JSONObject json) throws CameraParse {
		
		if (!json.containsKey("name")) throw new CameraParse("Can't load camera, missing name");
		
		this.name = (String) json.get("name");
		
		if (json.containsKey("address")) this.address = (String) json.get("address");
		if (json.containsKey("cameraType")) this.cameraType = CameraTypes.valueOf((String) json.get("cameraType"));
		if (json.containsKey("streamType")) this.streamType = StreamTypes.valueOf((String) json.get("streamType"));
		if (json.containsKey("streamAddress")) this.streamAddress = (String) json.get("streamAddress");
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
	
	public CameraTypes getType() {
		return cameraType;
	};
	public void setType(CameraTypes type) {
		this.cameraType = type;
	};
	
	public StreamTypes getStreamType() {
		return streamType;
	};
	public void setStreamType(StreamTypes type) {
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