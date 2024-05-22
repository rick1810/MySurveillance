package com.dutch_computer_technology.mySurveillance.website;

import com.dutch_computer_technology.mySurveillance.accounts.User;
import com.dutch_computer_technology.mySurveillance.website.data.Page;
import com.dutch_computer_technology.mySurveillance.website.data.Request;
import com.dutch_computer_technology.mySurveillance.main.MySurveillance;

public interface PageHandler {
	
	public default Page get(MySurveillance ms, Request request, User user) {
		return ms.website.notFoundPage(ContentType.HTML);
	};
	
	public default Page post(MySurveillance ms, Request request, User user) {
		return ms.website.notFoundPage(ContentType.TEXT);
	};
	
};