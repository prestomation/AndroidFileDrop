package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class DownloadService extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DownloadService.class
			.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			log.warning("Somebody didn't login");
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
			return;
		}
		// TODO Handle /done
		String pathinfo = req.getPathInfo();
		log.info("pathinfo: " + pathinfo);
		if (pathinfo == null || pathinfo.equals("")) {
			// For /download requests
			// Redirect to the actual downloadpath
			String filename = UserInfo.getUserFileName(user);
			if (filename == null) {
				resp.sendRedirect("/upload");
			} else {
				resp.sendRedirect("/download/" + filename);
			}

		} else {
			// For /download/filename requests
			// This is the destination of the redirect above
			// If the filename asked for isn't what the user owns, they are
			// given
			// a permission denied error
			String reqFileName = pathinfo.substring(1);
			String userFile = UserInfo.getUserFileName(user);
			if (reqFileName.equals(userFile)) {

				BlobstoreService blobService = BlobstoreServiceFactory
						.getBlobstoreService();
				BlobKey blobKey = UserInfo.getUserFile(user);
				if (blobKey != null) {

					// log.info("File served: " + userFile);
					blobService.serve(blobKey, resp);
				} else {
					resp.setContentType("text/plain");
					resp.getWriter().write("No file associated with this user");
					resp.sendError(500, "No file associated with this user");
					log.warning("No file associated with this user");

				}
			} else {
				// They didn't ask for the right file
				resp.setContentType("text/plain");
				resp.getWriter().write("Permission Denied");
				resp.sendError(403, "Permission Denied");
				log.warning("User asked for " + reqFileName
						+ "but this user owns " + userFile);
			}
		}

	}
}
