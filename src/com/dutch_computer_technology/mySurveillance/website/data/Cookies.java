package com.dutch_computer_technology.mySurveillance.website.data;

import java.util.ArrayList;
import java.util.List;

public class Cookies {
	
	private List<Cookie> cookies;
	
	public Cookies() {
		this.cookies = new ArrayList<Cookie>();
	};
	
	public void cookies(List<Cookie> cookies) {
		this.cookies = cookies;
	};
	
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	};
	
	public boolean hasCookie(String key) {
		return getCookie(key) != null;
	};
	
	public Cookie getCookie(String key) {
		for (Cookie cookie : cookies) {
			if (cookie.key().equals(key)) return cookie;
		};
		return null;
	};
	
	public List<Cookie> cookies() {
		return cookies;
	};
	
};