package com.prestomation.android.androidfiledrop;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.webkit.DownloadListener;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {

	static private String DM_DOWNLOAD_ID = "FILE_ID";
	static private String DM_DOWNLOAD_NAME = "FILE_NAME";

	public C2DMReceiver() {
		super(CloudRegistrar.EMAIL_ID);
	}

	@Override
	public void onRegistered(Context ctx, String registration) {
		Log.i("AndroidFileDrop", "registered and got key: " + registration);
		CloudRegistrar.registerWithCloud(ctx, registration);
	}

	@Override
	public void onError(Context context, String errorId) {

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i("AndroidFileDrop", "Received a message! ");
		Bundle extras = intent.getExtras();
		String filename = "File";
		if (extras != null) {
			filename = (String) extras.get("filename");
			Log.i("AndroidFileDrop",
					"We are supposed to download a file called: " + filename);
		}

		playNotificationSound(context);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			// Use Gingerbread's download manager if it is available
			SharedPreferences settings = Prefs.get(context);
			String googAccountName = settings.getString("accountName", null);

			AppEngineClient client = new AppEngineClient(context,
					googAccountName);

			DownloadManager mgr = (DownloadManager) context
					.getSystemService(DOWNLOAD_SERVICE);
			String ascidCookie = "";
			try {
				ascidCookie = client.getASCIDCookie(false);
			} catch (Exception e) {
				Log.e("AndroidFileDrop", "Not able to get an Appengine Cookie");
			}

			
			
			Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).mkdirs();
			Request filedrop = new Request(Uri.parse(AppEngineClient.BASE_URL
					.replace("https", "http")
					+ "/download")).setDescription(
					"AndroidFileDrop").setTitle(filename).addRequestHeader(
					"Cookie", ascidCookie).setDestinationInExternalPublicDir(
					Environment.DIRECTORY_DOWNLOADS, filename);

			long id = mgr.enqueue(filedrop);
			SharedPreferences.Editor prefsEdit = Prefs.get(context).edit();

			prefsEdit.putLong(DM_DOWNLOAD_ID, id);
			prefsEdit.putString(DM_DOWNLOAD_NAME, filename);
			prefsEdit.commit();
		}
		{
			// TODO: Download manager for Froyo
		}

	}

	public static void playNotificationSound(Context context) {
		Uri uri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (uri != null) {
			Ringtone rt = RingtoneManager.getRingtone(context, uri);
			if (rt != null) {
				rt.setStreamType(AudioManager.STREAM_NOTIFICATION);
				rt.play();
			}
		}

	}
}
