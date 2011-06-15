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

public class UserInfo {

	static String DEVICE_ID = "DeviceID";
	static String DEVICE_NAME = "DeviceName";
	static String FILE_BLOB_KEY = "FileBlobKey";
	private static final Logger log = Logger
			.getLogger(UserInfo.class.getName());

	public static void setUserDevice(User user, String deviceName, String regID) {
		// Users are stored in bigtable using their UUID as a key. their
		// nickname is stored(mostly for admin ease of use) along with the C2DM
		// reg key
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Entity userEntry;
		Key userKey = KeyFactory.createKey("User", user.getUserId());
		try {

			// If they already exist, pull the existing record out so we don't
			// lose track of old files
			userEntry = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			userEntry = new Entity(userKey);

		}

		userEntry.setProperty(DEVICE_ID, regID);
		userEntry.setProperty(DEVICE_NAME, deviceName);
		userEntry.setProperty("Nickname", user.getEmail());

		datastore.put(userEntry);
		log.info("Added user: " + user.getEmail());
	}

	public static String getUserDevice(User user) {
		// Given a User, return the C2DM ID registered, or null
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key userKey = KeyFactory.createKey("User", user.getUserId());
		Entity userEntity = null;
		try {
			userEntity = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			// Error handling for no such user. Return null?
			log.warning("User: " + user.getNickname() + " with UID: "
					+ user.getUserId() + " does not have a registered device");
			return null;
		}
		return (String) userEntity.getProperty(DEVICE_ID);

	}

	public static Map<String, String> getAllUsersDeviceInfo() {
		// Returns a map of nicknames to devregids, primarily for the admin info
		// page
		Map<String, String> usersMap = new HashMap<String, String>();
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Query usersQuery = new Query("User");
		Iterable<Entity> usersInfo = datastore.prepare(usersQuery).asIterable();
		for (Entity userEntity : usersInfo) {
			usersMap.put(userEntity.getProperty("Nickname").toString(),
					userEntity.getProperty(DEVICE_ID).toString());
		}
		return usersMap;
	}

	public static void clearUser(User user) {
		// Delete a given user from the registration table
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key userKey = KeyFactory.createKey("User", user.getUserId());

		Entity userEntity = null;
		try {
			userEntity = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			return;
		}
		BlobKey blobkey = (BlobKey) userEntity.getProperty(FILE_BLOB_KEY);
		if (blobkey != null) {
			destroyBlobFile(blobkey);
		}
		datastore.delete(userKey);
	}

	public static boolean setUserFile(User user, BlobKey blobkey) {
		// Associates the blob with the given user

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key userKey = KeyFactory.createKey("User", user.getUserId());
		Entity userEntity = null;
		boolean response;
		try {
			userEntity = datastore.get(userKey);

			if (userEntity.hasProperty(FILE_BLOB_KEY)) {
				BlobKey oldblobkey = (BlobKey) (userEntity
						.getProperty(FILE_BLOB_KEY));
				if (oldblobkey != null) {
					destroyBlobFile(oldblobkey);
				}
			}
			userEntity.setProperty(FILE_BLOB_KEY, blobkey);
			datastore.put(userEntity);
			response = true;
		} catch (EntityNotFoundException e) {
			response = false;
		}
		log.info("Added file for user: " + user.getEmail());
		return response;
	}

	public static BlobKey getUserFile(User user) {
		// Given a user, returns their blobkey, or null
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key userKey = KeyFactory.createKey("User", user.getUserId());
		Entity userEntity = null;

		try {
			userEntity = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			return null;
		}
		if (!userEntity.hasProperty(FILE_BLOB_KEY)) {
			return null;
		}
		return (BlobKey) ((userEntity.getProperty(FILE_BLOB_KEY)));
	}

	public static String getUserFileName(User user) {

		BlobInfoFactory blobinfofac = new BlobInfoFactory();
		BlobKey key = getUserFile(user);
		if (key == null) {
			return null;
		}
		BlobInfo blobinfo = blobinfofac.loadBlobInfo(key);
		if (blobinfo == null) {
			// User does not have a file
			return null;
		}
		String fullFilename = blobinfo.getFilename();
		String filename;
		if (fullFilename.contains("\\") || fullFilename.contains("/")) {
			String[] parts = fullFilename.replace("\\", "/").split("/");
			filename = parts[parts.length - 1];

		} else {
			filename = fullFilename;
		}
		return filename;
	}

	public static void clearUserFile(User user) {

		// Clear the blobproperty and destroy the associated blob for a
		// given user
		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		Key userKey = KeyFactory.createKey("User", user.getUserId());
		Entity userEntity = null;

		try {
			userEntity = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			return;
		}
		BlobKey blobkey = (BlobKey) (userEntity.getProperty(FILE_BLOB_KEY));
		if (blobkey != null) {
			destroyBlobFile(blobkey);
		}

		userEntity.removeProperty(FILE_BLOB_KEY);
		datastore.put(userEntity);

	}

	private static void destroyBlobFile(BlobKey key) {
		BlobstoreService blobService = BlobstoreServiceFactory
				.getBlobstoreService();
		blobService.delete(key);
	}
}
