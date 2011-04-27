package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class NotifyService extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
		} else {
			if (notifyUserDevice(user, resp)) {
				resp.getWriter().write("Notification Succeeded!");
			}

		}

	}

	protected boolean notifyUserDevice(User user, HttpServletResponse resp)
			throws IOException {
		C2DMessaging push = C2DMessaging.get(getServletContext());
		String collapseKey = "collapseKey";
		String devID = DeviceInfo.getDeviceInfoForUser(user);
		if (devID == null) {

			resp.getWriter().write("User has no device registered");
			return false;
		}

		boolean response = false;
		try {

			response = push.sendNoRetry(devID, collapseKey, "");
		} catch (IOException ex) {
			if ("NotRegistered".equals(ex.getMessage())
					|| "InvalidRegistration".equals(ex.getMessage())) {
				// This device isn't registered with C2DM
				DeviceInfo.clearUser(user);

			}

		}

		return response;

	}
}
