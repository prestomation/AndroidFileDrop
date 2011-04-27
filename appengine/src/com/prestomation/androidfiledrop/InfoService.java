package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


public class InfoService extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if (user != null)
        {
		resp.getWriter().println("This is the info servlet: " + user.getNickname());
        }
        else
        {
		 resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
        }
        resp.getWriter().println("<p>Users:</p><p>");
        Map<String, String> usersMap = DeviceInfo.getAllUsersDeviceInfo();
        for(Map.Entry<String, String> entry : usersMap.entrySet())
        {	
        	resp.getWriter().println("<p>" + entry.getKey() + ": " + entry.getValue() + "</p>");
        
        }
        
        
        
	}
}
