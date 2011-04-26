package com.prestomation.android.androidfiledrop;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class CloudRegistrar {

	public static final String STATUS_EXTRA = "Status";
	public static final int REGISTERED_STATUS = 1;
	public static final int AUTH_ERROR_STATUS = 2;
	public static final int UNREGISTERED_STATUS = 3;
	public static final int ERROR_STATUS = 4;

	static final String EMAIL_ID = "droidfiledrop@gmail.com";

	public static void registerWithCloud(final Context ctx, final String regID){
		new Thread(new Runnable() {
			public void run() {
				Intent updateUI = new Intent(SetupActivity.UPDATE_UI_ACTION);
				
				//TODO Register with androidfiledrop.appspot.com
				if (true) //TODO: If registration was successful, we will assume it was  
				{
					SharedPreferences.Editor prefseditor = Prefs.get(ctx).edit();
					prefseditor.putString("deviceRegID", regID);
					prefseditor.commit();
					updateUI.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
				
				}
				else //Else there was a registration error
				{
					Log.w("AndroidFileDrop", "Registration Error");
					updateUI.putExtra(STATUS_EXTRA, ERROR_STATUS);
				
				}
				updateUI.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
				
				//This gets caught by SetupActivity to resume activation
				ctx.sendBroadcast(updateUI);	
					
			}
		}).start();
	}
}
