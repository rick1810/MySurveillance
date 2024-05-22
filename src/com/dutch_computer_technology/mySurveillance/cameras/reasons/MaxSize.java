package com.dutch_computer_technology.mySurveillance.cameras.reasons;

import java.io.File;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import com.dutch_computer_technology.mySurveillance.Main;
import com.dutch_computer_technology.mySurveillance.cameras.Camera;
import com.dutch_computer_technology.mySurveillance.cameras.paths.Path;
import com.dutch_computer_technology.mySurveillance.json.JSONObject;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.main.FileManager.ReasonType;

public class MaxSize extends Reason {
	
	private BigInteger maxSize;
	private Unit unit;
	private BigInteger bufferedSize;
	
	public MaxSize(MySurveillance ms, Camera cam) {
		super(ms, ReasonType.MaxSize, cam);
		this.maxSize = con_GB_Bytes(BigInteger.valueOf(120L));
		this.unit = Unit.gb;
	};
	
	private enum Unit {
		tb,
		gb,
		mb,
		kb
	};
	
	public MaxSize(MySurveillance ms, Camera cam, JSONObject json) {
		super(ms, ReasonType.MaxSize, cam, json);
		BigInteger maxSize = con_GB_Bytes(BigInteger.valueOf(120L));
		Unit unit = Unit.gb;
		Object oUnit = json.get("unit");
		if (oUnit instanceof String) {
			try {
				unit = Unit.valueOf((String) oUnit);
			} catch(IllegalArgumentException ignore) {};
		};
		Object oSize = json.get("size");
		try {
			if (oSize instanceof BigInteger || oSize instanceof String) {
				BigInteger size;
				if (oSize instanceof String) {
					size = new BigInteger((String) oSize);
				} else {
					size = (BigInteger) oSize;
				};
				switch(unit) {
					case tb:
						size = con_TB_Bytes(size);
						break;
					case mb:
						size = con_MB_Bytes(size);
						break;
					case kb:
						size = con_KB_Bytes(size);
						break;
					case gb: default:
						size = con_GB_Bytes(size);
						break;
				};
				maxSize = size;
				this.unit = unit;
			};
		} catch(NumberFormatException ignore) {};
		this.maxSize = maxSize;
	};
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		BigInteger size = maxSize;
		json.put("unit", unit.toString());
		switch(unit) {
			case tb:
				size = con_Bytes_TB(size);
				break;
			case mb:
				size = con_Bytes_MB(size);
				break;
			case kb:
				size = con_Bytes_KB(size);
				break;
			case gb: default:
				size = con_Bytes_GB(size);
				break;
		};
		json.put("size", size.toString());
		return json;
	};
	
	private BigInteger con_TB_Bytes(BigInteger bytes) {
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_GB_Bytes(BigInteger bytes) {
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_MB_Bytes(BigInteger bytes) {
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_KB_Bytes(BigInteger bytes) {
		bytes = bytes.multiply(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_Bytes_TB(BigInteger bytes) {
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_Bytes_GB(BigInteger bytes) {
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_Bytes_MB(BigInteger bytes) {
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		return bytes;
	};
	private BigInteger con_Bytes_KB(BigInteger bytes) {
		bytes = bytes.divide(BigInteger.valueOf(1024L));
		return bytes;
	};
	
	@Override
	public String html() {
		BigInteger size = maxSize;
		switch(unit) {
			case tb:
				size = con_Bytes_TB(size);
				break;
			case mb:
				size = con_Bytes_MB(size);
				break;
			case kb:
				size = con_Bytes_KB(size);
				break;
			case gb: default:
				size = con_Bytes_GB(size);
				break;
		};
		return "<div><p>Reason:</p><input key=\"size\" type=\"number\" min=\"1\" max=\"1024\" value=\"" + size.toString() + "\"/><p>" + unit.toString().toUpperCase() + "</p><input hidden key=\"unit\" value=\"" + unit.toString() + "\"</div>";
	};
	
	@Override
	public boolean test() {
		
		Camera cam = getCam();
		if (cam == null) return false;
		
		Path path = cam.getPath();
		if (path == null) return false;
		
		BigInteger size = BigInteger.valueOf(0);
		try {
			
			List<File> files = path.files();
			for (File file : files) {
				if (file.isFile()) size.add(BigInteger.valueOf(file.length()));
			};
			
			this.bufferedSize = size;
			
			return (size.compareTo(maxSize) == 1);
			
		} catch (Exception e) {
			Main.print(this, e);
		};
		
		return false;
		
	};
	
	@Override
	public void delete() {
		
		Camera cam = getCam();
		if (cam == null) return;
		
		Path path = cam.getPath();
		if (path == null) return;
		
		try {
			
			List<File> files = path.files();
			Collections.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
			
			int dels = 0;
			int i = 0;
			while (bufferedSize.compareTo(maxSize) == 1 && files.size() > 0 && dels < 1000) { //Don't delete too much in a single loop, everything has to wait for this!
				File file = files.get(i);
				if (path.delete(file.getName())) bufferedSize.subtract(BigInteger.valueOf(file.length()));
				i++;
			};
			
		} catch (Exception e) {
			Main.print(this, e);
		};
		
	};
	
};