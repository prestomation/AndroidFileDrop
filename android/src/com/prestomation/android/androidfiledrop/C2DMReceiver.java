package com.prestomation.android.androidfiledrop;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

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
		SharedPreferences settings = Prefs.get(ctx);
		String nickname = settings.getString(SetupActivity.PREF_DEVICE_NICKNAME, "myDevice");
		CloudRegistrar.registerWithCloud(ctx, nickname, registration);
	}

	@Override
	public void onError(Context context, String errorId) {

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i("AndroidFileDrop", "Received a message! ");
		Bundle extras = intent.getExtras();
		String filename = "";
		if (extras != null) {
			filename = (String) extras.get("filename");
			if (filename == null)
			{
			Log.i("AndroidFileDrop",
					"No filename attached!") ;
				return;
			}
			Log.i("AndroidFileDrop",
					"We are supposed to download a file called: " + filename);
		}
		if (filename == "")
		{
			return;
		}

		playNotificationSound(context);
		SharedPreferences settings = Prefs.get(context);
		String googAccountName = settings.getString("accountName", null);

		AppEngineClient client = new AppEngineClient(context, googAccountName);
		String ascidCookie = "";
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {

			// Use Gingerbread's download manager if it is available

			try {
				ascidCookie = client.getASCIDCookie(false);
			} catch (Exception e) {
				Log.e("AndroidFileDrop", "Not able to get an Appengine Cookie");
			}

			DownloadManager mgr = (DownloadManager) context
					.getSystemService(DOWNLOAD_SERVICE);

			Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).mkdirs();
			Request filedrop = new Request(Uri.parse(AppEngineClient.BASE_URL
					.replace("https", "http")
					+ "/api/files/" + filename)).setDescription("AndroidFileDrop").setTitle(
					filename).addRequestHeader("Cookie", ascidCookie)
					.setDestinationInExternalPublicDir(
							Environment.DIRECTORY_DOWNLOADS, filename);

			long id = mgr.enqueue(filedrop);
			SharedPreferences.Editor prefsEdit = Prefs.get(context).edit();
			Log.i("AndroidFileDrop", "saving ID: " + id);
			prefsEdit.putLong(DM_DOWNLOAD_ID, id);
			prefsEdit.putString(DM_DOWNLOAD_NAME, filename);
			prefsEdit.commit();
		}
		else
		{
			
			//Otherwise, use our crappy download manager

			try {
				ascidCookie = client.getASCIDCookie(false);
			} catch (Exception e) {
				Log.e("AndroidFileDrop", "Not able to get an Appengine Cookie");
			}
			Intent froyoIntent = new Intent(context, DownloadService.class);
			froyoIntent.putExtra("filename", filename);
			froyoIntent.putExtra("ascid", ascidCookie);
			context.startService(froyoIntent);
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
