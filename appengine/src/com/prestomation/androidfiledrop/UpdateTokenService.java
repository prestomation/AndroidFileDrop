package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.C2DMConfig;
import com.google.android.c2dm.server.C2DMConfigLoader;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UpdateTokenService extends HttpServlet {

	private static final Logger log = Logger.getLogger(UpdateTokenService.class
			.getName());

	private static final String OK_STATUS = "OK";
	private static final String LOGIN_REQUIRED_STATUS = "LOGIN_REQUIRED";
	private static final String ERROR_STATUS = "ERROR";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");

		String newtoken = req.getParameter("token");
		if (newtoken == null
				|| "".equals(newtoken.trim())) {
			resp.setStatus(400);
			resp.getWriter().println(ERROR_STATUS + "(Must specify valid token)");
			log.severe("Missing token ");
			return;
		}
		
		//There is a updatetoken.py file which will automatically update the token 
		//This is not checked in due to sensitive information
		C2DMessaging push = C2DMessaging.get(getServletContext());
		push.getServerConfig().updateToken(newtoken);
		log.info("Updated Token!");
		
		

	}
}
