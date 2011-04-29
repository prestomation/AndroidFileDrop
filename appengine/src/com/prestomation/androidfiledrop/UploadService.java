package com.prestomation.androidfiledrop;

import java.io.IOException;
import java.util.Map;
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
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UploadService extends HttpServlet {
	private static final Logger log = Logger.getLogger(UpdateTokenService.class
			.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
			return;
		}

		// Create a blob upload URL
		BlobstoreService blobService = BlobstoreServiceFactory
				.getBlobstoreService();
		String uploadUrl = blobService.createUploadUrl("/upload");
		String pathinfo = req.getPathInfo();
		if (pathinfo != null && req.getPathInfo().equals("/geturl")) {
			resp.setContentType("text/plain");
			// A request for "/upload/geturl" is an api for an app to easily get
			// an upload url
			resp.getWriter().write(uploadUrl);
			return;

		}

		resp.setContentType("text/html");

		resp.getWriter().println("<body>");
		String status = req.getParameter("status");
		if (status != null) {
			if (status.equals("nosuchuser")) {
				resp.getWriter().println(
						"Your user does not have a device registered");

			} else if (status.equals("failure")) {

				resp.getWriter().println("Upload failure: Please try again");

			}
		}

		// We print out a simple HTML upload form
		resp.getWriter().println(
				"<form action=\""
						+ uploadUrl
						+ "` method=`post` enctype=`multipart/form-data`>"
								.replace('`', '"'));
		resp
				.getWriter()
				.println(
						"<input type=`file` name=`myFile`><input type=`submit` value=`Submit`></form></body>"
								.replace('`', '"'));

	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/html");
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		BlobstoreService blobstoreService = BlobstoreServiceFactory
				.getBlobstoreService();
		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
		if (user == null) {
			log.warning("Not logged in....Deleting file");
			BlobKey deadblobKey = blobs.get("myFile");
			if (deadblobKey != null) {

				blobstoreService.delete(deadblobKey);
			}
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
			return;
		}

		BlobKey blobKey = blobs.get("myFile");
		if (blobKey == null) {
			log
					.warning("Upload failed, redirection to upload with failure status");
			resp.sendRedirect("/upload?status=failure");
		} else {
			if (!UserInfo.setUserFile(user, blobKey)) {
				// This user doesn't exist. Delete what was just uploaded and
				// alert the user
				blobstoreService.delete(blobKey);
				log
						.warning("This user not have a device registered, redirection to upload with nosuchuser status");
				resp.sendRedirect("/upload?status=nosuchuser");

			}
			log.info("File uploaded succesfully!");
			resp.sendRedirect("/notify");
		}

	}
}
