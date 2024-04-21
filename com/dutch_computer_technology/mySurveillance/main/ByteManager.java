package com.dutch_computer_technology.mySurveillance.main;

public class ByteManager {
	
	public static byte[] append(byte[] arr, String str) {
		
		return append(arr, str.getBytes());
		
	};
	
	public static byte[] append(byte[] arr, byte[] b) {
		
		byte[] buf = new byte[arr.length + b.length];
		for (int i = 0; i < arr.length; i++) {
			buf[i] = arr[i];
		};
		for (int i = 0; i < b.length; i++) {
			buf[arr.length + i] = b[i];
		};
		return buf;
		
	};
	
	public static byte[] append(byte[] arr, byte b) {
		
		byte[] buf = new byte[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			buf[i] = arr[i];
		};
		buf[arr.length] = b;
		return buf;
		
	};

	public static byte[] delete(byte[] arr, int start) {
		
		return delete(arr, start, arr.length);
		
	};
	
	public static byte[] delete(byte[] arr, int start, int end) {
		
		int dif = arr.length - (end-start);
		if (dif < 1) return new byte[0];
		
		byte[] buffer = new byte[dif];
		int o = 0;
		for (int i = 0; i < arr.length; i++) {
			if (i < start) continue;
			if (i > end) break;
			if (o+1 > dif) break;
			buffer[o] = arr[i];
			o++;
		};
		return buffer;
		
	};
	
	public static byte[] between(byte[] arr, int start) {
		
		return between(arr, start, arr.length);
		
	};
	
	public static byte[] between(byte[] arr, int start, int end) {
		
		int dif = end - start;
		if (dif < 1) return new byte[0];
		
		byte[] buffer = new byte[dif];
		int o = 0;
		for (int i = start; i < end; i++) {
			buffer[o] = arr[i];
			o++;
		};
		return buffer;
		
	};
	
	public static boolean equals(byte[] arr, byte[] arr2) {
		
		if (arr.length != arr2.length) return false;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != arr2[i]) return false;
		};
		return true;
		
	};
	
	public static int indexOf(byte[] arr, byte[] find) {
		
		return indexOf(arr, find, 0);
		
	};
	
	public static int indexOf(byte[] arr, byte[] find, int start) {
		
		for (int i = start; i <= arr.length - find.length; i++) {
			boolean found = true;
			for (int o = 0; o < find.length; o++) {
				if (arr[i + o] != find[o]) {
					found = false;
					break;
				};
			};
			if (found) {
				return i;
			};
		};
		return -1;
		
	};
	
};