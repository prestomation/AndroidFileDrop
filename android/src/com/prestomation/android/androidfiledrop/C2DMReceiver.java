package com.prestomation.android.androidfiledrop;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {

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
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD )
		{
			//Use Gingerbread's download manager if it is available
			SharedPreferences settings = Prefs.get(context);
			String googAccountName = settings.getString("accountName", null);

			AppEngineClient client = new AppEngineClient(context, googAccountName);
			
			
		DownloadManager mgr = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
		String ascidCookie = "";
		try {
			ascidCookie = client.getASCIDCookie(false);
		} catch (Exception e) {
			Log.e("AndroidFileDrop", "Not able to get an Appengine Cookie");
		}
		Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
		Log.i("AndroidFileDrop", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
		Request filedrop = new Request( Uri.parse(AppEngineClient.BASE_URL.replace("https","http") + "/download"))
		.setDescription("AndroidFileDrop: " + filename)
		.addRequestHeader("Cookie", ascidCookie)
		.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
		
		
		
		mgr.enqueue(filedrop);
		}
		{
			//TODO: Download manager for Froyo
		}

	}
}
