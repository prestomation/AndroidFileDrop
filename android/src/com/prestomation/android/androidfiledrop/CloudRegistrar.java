package com.prestomation.android.androidfiledrop;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

public class CloudRegistrar {

	public static final String REGISTER_PATH = "/register";
	public static final String STATUS_EXTRA = "Status";
	public static final int REGISTERED_STATUS = 1;
	public static final int AUTH_ERROR_STATUS = 2;
	public static final int UNREGISTERED_STATUS = 3;
	public static final int ERROR_STATUS = 4;

	static final String EMAIL_ID = "droidfiledrop@gmail.com";

	public static void registerWithCloud(final Context ctx,
			final String deviceRegID) {
		new Thread(new Runnable() {
			public void run() {
				Intent updateUI = new Intent(SetupActivity.UPDATE_UI_ACTION);
				try {
					HttpResponse res = makeRequest(ctx, deviceRegID,
							REGISTER_PATH);

					if (res.getStatusLine().getStatusCode() == 200) 
					{
						SharedPreferences.Editor prefseditor = Prefs.get(ctx)
								.edit();
						prefseditor.putString("deviceRegID", deviceRegID);
						prefseditor.commit();
						updateUI.putExtra(STATUS_EXTRA, REGISTERED_STATUS);

					} else // Else there was a registration error
					{
						Log.w("AndroidFileDrop", "Registration Error");
						updateUI.putExtra(STATUS_EXTRA, ERROR_STATUS);

					}
					updateUI.putExtra(STATUS_EXTRA, REGISTERED_STATUS);

					// This gets caught by SetupActivity to resume activation
					ctx.sendBroadcast(updateUI);

				} catch (AppEngineClient.PendingAuthException pae) {
					// ignore, this will just register at a later time
				} catch (Exception e) {
					Log.w("AndroidFileDrop", "Registration error "
							+ e.getMessage());
					updateUI.putExtra(STATUS_EXTRA, ERROR_STATUS);
					ctx.sendBroadcast(updateUI);
				}
			}
		}).start();
	}

	private static HttpResponse makeRequest(Context ctx, String deviceRegID,
			String urlPath) throws Exception {
		SharedPreferences settings = Prefs.get(ctx);
		String googAccountName = settings.getString("accountName", null);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("devregid", deviceRegID));

		AppEngineClient client = new AppEngineClient(ctx, googAccountName);
		return client.makeRequest(urlPath, params);

	}
}
