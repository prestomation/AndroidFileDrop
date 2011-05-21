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

public class ClientUploadService extends HttpServlet {

	private static final Logger log = Logger
			.getLogger(ClientUploadService.class.getName());

	private static final String FILEID_APK = "AndroidFileDrop.apk";
	private static final String FILEID_WINDOWS = "AndroidFileDrop.exe";
	private static final String FILEID_LINUX = "AndroidFileDrop";

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/html");

		// Create a blob upload URL
		BlobstoreService blobService = BlobstoreServiceFactory
				.getBlobstoreService();
		String uploadUrl = blobService.createUploadUrl("/admin/clientupload");

		resp.setContentType("text/html");

		// We print out a simple HTML upload form

		resp.getWriter().println("<body>");
		resp.getWriter().println("Upload apk file:");
		resp.getWriter().println(
				"<form action=\""
						+ uploadUrl
						+ "` method=`post` enctype=`multipart/form-data`>"
								.replace('`', '"'));
		resp.getWriter().println(
				"<input type=`file` name=`".replace('`', '"')
						+ FILEID_APK
						+ "`><input type=`submit` value=`Submit`></form>"
								.replace('`', '"'));

		resp.getWriter().println("Upload Windows executable:");
		resp.getWriter().println(
				"<form action=\""
						+ uploadUrl
						+ "` method=`post` enctype=`multipart/form-data`>"
								.replace('`', '"'));
		resp
				.getWriter()
				.println(
						"<input type=`file` name=`".replace('`', '"')
								+ FILEID_WINDOWS
								+ "`><input type=`submit` value=`Submit`></form></body>"
										.replace('`', '"'));

		resp.getWriter().println("Upload Linux executable:");
		resp.getWriter().println(
				"<form action=\""
						+ uploadUrl
						+ "` method=`post` enctype=`multipart/form-data`>"
								.replace('`', '"'));
		resp
				.getWriter()
				.println(
						"<input type=`file` name=`".replace('`', '"')
								+ FILEID_LINUX
								+ "`><input type=`submit` value=`Submit`></form></body>"
										.replace('`', '"'));

		resp.getWriter().println("</body>");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/html");
		BlobstoreService blobstoreService = BlobstoreServiceFactory
				.getBlobstoreService();
		Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);

		for (String blobkey : blobs.keySet()) {
			ClientStorage.setClientBinary(blobkey, blobs.get(blobkey));

		}
		resp.sendRedirect("/admin/clientupload");

	}

}
