package com.prestomation.android.androidfiledrop;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {

	public C2DMReceiver() {
		super(CloudRegistrar.EMAIL_ID);
	}

	@Override
	public void onRegistered(Context ctx, String registration){
		Log.i("AndroidFileDrop", "registered and got key: " + registration);
		CloudRegistrar.registerWithCloud(ctx, registration);
	}
	@Override
	public void onError(Context context, String errorId) {

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		//TODO We've received a message! Do something with it, like, I dunno, download a file
		Log.i("AndroidFileDrop", "Received a message! ");
	}

}
