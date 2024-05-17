package com.dutch_computer_technology.mySurveillance.pages;

import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;
import com.dutch_computer_technology.mySurveillance.website.ContentType;
import com.dutch_computer_technology.mySurveillance.website.PageHandler;
import com.dutch_computer_technology.mySurveillance.website.Website.FileType;

public class Home implements PageHandler {
	
	public Page get(MySurveillance ms, Request request, User user) {
		
		Page page = new Page();
		page.setStatus(200);
		page.setContentType(ContentType.HTML);
		
		String html = new String(ms.website.loadFile(FileType.HTML, "home"));
		
		String cams = "<div class=\"cameras\">";
		int min = ms.screens.size();
		double sqr = Math.sqrt(min);
		while (sqr % 1 != 0) {
			min++;
			sqr = Math.sqrt(min);
		};
		double pro = 100 / sqr;
		int font = 22;
		String size = "calc(" + Double.toString(pro) + "% - 2px);";
		for (int i = 0; i < ms.screens.size() || i < min; i++) {
			String name = "";
			if (i < ms.screens.size()) name = ms.screens.get(i);
			String src = name.length() > 0 ? "/cameras?camera=" + name : "";
			cams += "<div style=\"width: " + size + "; height: " + size + ";\" class=\"camera\"><canvas class=\"view\" src=\"" + src + "\"></canvas><h3 class=\"title\" style=\"font-size: " + font + "px;\">" + name + "</h3></div>";
		};
		cams += "</div></div>";
		html = ms.website.replaceHTML(html, "<cameras>", cams);
		
		html = ms.website.loadHTML(html);
		page.setData(html);
		return page;
		
	};
	
};