package com.prestomation.android.androidfiledrop;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;

public class DownloadService extends Service {

	static private String DOWNLOAD_URL = "http://androidfiledrop.appspot.com/download/";

	private NotificationManager mNotifyManager;
	private String mFilename;
	private String mCookie;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mFilename = intent.getStringExtra("filename");
		mCookie = intent.getStringExtra("ascid");

		new Thread() {
			@Override
			public void run() {
				try {
					Notification mNotification;
					// Start our notification
					mNotification = new Notification(R.drawable.icon,
							"AndroidFiledDrop " + mFilename, System
									.currentTimeMillis());
					mNotification.flags = mNotification.flags
							| Notification.FLAG_ONGOING_EVENT;
					mNotification.contentView = new RemoteViews(
							getApplicationContext().getPackageName(),
							R.layout.downloadstatus);
					// TODO: Pretty bitmap
					// mNotification.contentView.setImageViewResource(viewId,
					// srcId)
					mNotification.contentView.setTextViewText(
							R.id.downloadstatus_text, "AndroidFileDrop "
									+ mFilename);
					// Indeterminate progress
					mNotification.contentView.setProgressBar(
							R.id.downloadstatus_progress, 100, 0, false);
					//We use a dummy intent
					mNotification.contentIntent = PendingIntent.getActivity(
							getApplicationContext(), 0, new Intent("noop"), 0);

					mNotifyManager.notify(1, mNotification);

					URL url = new URL(DOWNLOAD_URL + mFilename);
					DefaultHttpClient client = new DefaultHttpClient();

					Log.i("AndroidFileDrop", "download");
					HttpGet downloadmethod = new HttpGet(DOWNLOAD_URL
							+ mFilename);

					downloadmethod.setHeader("Cookie", mCookie);
					HttpResponse response = client.execute(downloadmethod);
					InputStream input = new BufferedInputStream(response
							.getEntity().getContent());
					String outputFileName = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
							+ "/" + mFilename;

					Log.i("AndroidFileDrop", "outputfile: " + outputFileName );
					OutputStream output = new FileOutputStream(outputFileName);

					byte data[] = new byte[1024];

					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						total += count;
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
					mNotifyManager.cancel(1);
					mNotification = new Notification(R.drawable.icon,
							"Download complete!", System.currentTimeMillis());
					String content = mFilename + " download complete";

					String fileurl = "file:///"
							+ Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
							+ "/" + mFilename;

					// Create an intent to open it.
					String[] filenameParts = mFilename.split("\\.");
					String extension = "";
					if (filenameParts.length > 1) {
						extension = filenameParts[filenameParts.length - 1];
					}
					// TODO: Better mimetype detection
					MimeTypeMap mimemap = MimeTypeMap.getSingleton();
					String mimetype = mimemap
							.getMimeTypeFromExtension(extension.toLowerCase());
					if (mimetype == null) {
						// We'll assume it's text if the extension isn't helping
						mimetype = "text/plain";
					}

					Log.i("AndroidFileDrop", "file: " + fileurl + " mimetype: " + mimetype);

					Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
					openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_GRANT_READ_URI_PERMISSION);
					openFileIntent.setDataAndType(Uri.parse(fileurl), mimetype);
					
					PendingIntent pendingIntent = PendingIntent.getActivity(
							getApplicationContext(), 0, openFileIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);

					String appName = "AndroidFileDrop";
					mNotification.setLatestEventInfo(getApplicationContext(),
							appName, content, pendingIntent);
					mNotification.flags = Notification.FLAG_AUTO_CANCEL;

					mNotifyManager.notify(1, mNotification);

				} catch (Exception e) {
					Log.e("AndroidFileDrop", e.getMessage());
				}
			};
		}.start();

		return START_NOT_STICKY;

	}
}