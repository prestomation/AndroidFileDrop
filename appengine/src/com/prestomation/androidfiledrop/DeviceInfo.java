package com.prestomation.androidfiledrop;
import java.util.HashMap;
import java.util.Map;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class DeviceInfo {
	
	private static String DEVICE_ID = "DeviceID";
	
	public static void setDeviceInfoForUser(String user, String regID)
	{
		Entity userEntry = new Entity("User", user);
		userEntry.setProperty(DEVICE_ID, regID);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(userEntry);
	}
	
	public static String getDeviceInfoForUser(String user)
	
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("User", user);
        Entity userEntity = null;
        try {
			userEntity = datastore.get(userKey);
		} catch (EntityNotFoundException e) {
			//Error handling for no such user. Return null?
		}
		return userEntity.getProperty(DEVICE_ID).toString();
	
	}
	
	public static Map<String, String> getAllUsersDeviceInfo()
	{
		Map<String, String> usersMap = new HashMap<String, String>();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query usersQuery = new Query("User");
        Iterable <Entity> usersInfo = datastore.prepare(usersQuery).asIterable();
        for (Entity userEntity : usersInfo)
        {
        	usersMap.put(userEntity.getKey().toString(), userEntity.getProperty(DEVICE_ID).toString());
        }
        return usersMap;
	}

}
