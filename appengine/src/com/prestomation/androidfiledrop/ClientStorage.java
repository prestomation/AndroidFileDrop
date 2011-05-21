package com.prestomation.androidfiledrop;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;

public class ClientStorage {

	static String FILE_BLOB_KEY = "FileBlobKey";
	private static final Logger log = Logger
			.getLogger(ClientStorage.class.getName());

	public static void setClientBinary(String binaryname, BlobKey blobkey) {
		Entity clientEntry = new Entity("ClientBinary", binaryname);
		clientEntry.setProperty(FILE_BLOB_KEY, blobkey);
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		datastore.put(clientEntry);
		log.info("Added client: " + binaryname);
	}

	public static BlobKey getClientBinary(String binaryname) {
		// Given a binaryname, returns their blobkey, or null
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key clientKey = KeyFactory.createKey("ClientBinary", binaryname);
		Entity clientEntity = null;
		try {
			clientEntity = datastore.get(clientKey);
		} catch (EntityNotFoundException e) {
			return null;
		}
		if(!clientEntity.hasProperty(FILE_BLOB_KEY))
		{
			return null;
		}
		return (BlobKey) ((clientEntity.getProperty(FILE_BLOB_KEY)));

	}
}
