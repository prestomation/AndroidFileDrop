package com.prestomation.androidfiledrop;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;

public class DeviceInfo {
	
	static String DEVICE_ID = "DeviceID";
    private static final Logger log = Logger.getLogger(DeviceInfo.class.getName());
    
	public static void setDeviceInfoForUser(User user, String regID)
	{
		//Users are stored in bigtable using their UUID as a key. their nickname is stored(mostly for admin ease of use) along with the C2DM reg key
		Entity userEntry = new Entity("User", user.getUserId());
		userEntry.setProperty(DEVICE_ID, regID);
		userEntry.setProperty("Nickname", user.getEmail());
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(userEntry);
	}
	
	public static String getDeviceInfoForUser(User user)
	{
		//Given a User, return the C2DM ID registered, or null
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("User", user.getUserId());
        Entity userEntity = null;
        try {
			userEntity = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			//Error handling for no such user. Return null?
			log.warning("User: " +user.getNickname() + " with UID: " + user.getUserId() + " does not have a registered device");
			return null;
		}
		return (String) userEntity.getProperty(DEVICE_ID);
		
	
	}
	
	public static Map<String, String> getAllUsersDeviceInfo()
	{
		//Returns a map of nicknames to devregids, primarily for the admin info page
		Map<String, String> usersMap = new HashMap<String, String>();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query usersQuery = new Query("User");
        Iterable <Entity> usersInfo = datastore.prepare(usersQuery).asIterable();
        for (Entity userEntity : usersInfo)
        {
        	usersMap.put(userEntity.getProperty("Nickname").toString(), userEntity.getProperty(DEVICE_ID).toString());
        }
        return usersMap;
	}
	
	public static void clearUser(User user)
	{
		//Delete a given user from the registration table
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("User", user.getUserId());
        datastore.delete(userKey);
	}


}
