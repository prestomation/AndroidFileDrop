package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class RegisterService extends HttpServlet {

	private static final Logger log = Logger.getLogger(RegisterService.class
			.getName());

	private static final String OK_STATUS = "OK";
	private static final String LOGIN_REQUIRED_STATUS = "LOGIN_REQUIRED";
	private static final String ERROR_STATUS = "ERROR";

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/plain");

		String deviceRegistrationId = req.getParameter("devregid");
		if (deviceRegistrationId == null
				|| "".equals(deviceRegistrationId.trim())) {
			resp.setStatus(400);
			resp.getWriter().println(ERROR_STATUS + "(Must specify devregid)");
			log.severe("Missing registration id ");
			return;
		}
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null)
		{
			resp.setStatus(400);
			resp.getWriter().println(LOGIN_REQUIRED_STATUS);
		}
		else
		{
			//We have a user
			DeviceInfo.setDeviceInfoForUser(user, deviceRegistrationId);
			resp.getWriter().println(OK_STATUS);
			
			
			
		}
		
		

	}
}
