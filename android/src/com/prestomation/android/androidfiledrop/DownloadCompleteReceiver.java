package com.prestomation.android.androidfiledrop;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class DownloadCompleteReceiver extends BroadcastReceiver {

	static private String DM_DOWNLOAD_ID = "FILE_ID";
	static private String DM_DOWNLOAD_NAME = "FILE_NAME";

	public void downloadFile() {

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences prefs = Prefs.get(context);
		String filename = prefs.getString(DM_DOWNLOAD_NAME, "dummyfilename");
		Long fileID = prefs.getLong(DM_DOWNLOAD_ID, 0);

		Long incomingId = intent.getLongExtra(
				DownloadManager.EXTRA_DOWNLOAD_ID, 0);
		if (fileID != incomingId) {
			// Not our file, ignore

			return;
		}

		final NotificationManager notifyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		final DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Query myQuery = new DownloadManager.Query();
		myQuery.setFilterById(fileID);
		Cursor myCursor = downloadManager.query(myQuery);
		myCursor.moveToFirst();

		String downloadMime = myCursor.getString(myCursor
				.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
		int downloadStatus = myCursor.getInt(myCursor
				.getColumnIndex(DownloadManager.COLUMN_STATUS));
		
		Intent openFileIntent;
		Notification downloadNotification;
		String content;
		if (downloadStatus != DownloadManager.STATUS_SUCCESSFUL) {
			// TODO: Do something. Toast and open DownloadManager?
			Log.e("AndroidFileDrop", "The Download was unsuccessful!");
			openFileIntent = new Intent(
					DownloadManager.ACTION_VIEW_DOWNLOADS);
			openFileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			downloadNotification = new Notification(
					R.drawable.icon, "Download Unsuccessful!", System
							.currentTimeMillis());
			content = filename + " Download Unsuccessful!";
		} else {
			// Download Successful

			downloadNotification = new Notification(
					R.drawable.icon, "Download complete!", System
							.currentTimeMillis());
			content = filename + " download complete";

			String fileurl = "file:///"
					+ Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
					+ "/" + filename;

			// Create an intent to open it.
			openFileIntent = new Intent(Intent.ACTION_VIEW);
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_GRANT_READ_URI_PERMISSION);
			openFileIntent.setDataAndType(Uri.parse(fileurl), downloadMime);
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				openFileIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		String appName = "AndroidFileDrop";
		downloadNotification.setLatestEventInfo(context, appName, content,
				pendingIntent);
		downloadNotification.flags = Notification.FLAG_AUTO_CANCEL;

		notifyManager.notify(1, downloadNotification);
	}
}
