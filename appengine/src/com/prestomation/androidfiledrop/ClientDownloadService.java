package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.C2DMConfig;
import com.google.android.c2dm.server.C2DMConfigLoader;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class ClientDownloadService extends HttpServlet {

	private static final Logger log = Logger
			.getLogger(ClientDownloadService.class.getName());

	private static final String OK_STATUS = "OK";
	private static final String LOGIN_REQUIRED_STATUS = "LOGIN_REQUIRED";
	private static final String ERROR_STATUS = "ERROR";

	private static final String FILEID_APK = "APK";
	private static final String FILEID_WINDOWS = "exe";

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

		String reqFileName = pathinfo.substring(1);
		BlobKey blobkey = ClientStorage.getClientBinary(reqFileName);
		if (blobkey != null) {

			BlobstoreService blobService = BlobstoreServiceFactory
					.getBlobstoreService();
			blobService.serve(blobkey, resp);
		} else {
			resp.setContentType("text/plain");
			resp.getWriter().write("Bad Request");
			resp.sendError(500, "Bad Request");
		}

	}

}
