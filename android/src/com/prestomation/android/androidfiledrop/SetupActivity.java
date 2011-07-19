package com.prestomation.android.androidfiledrop;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.c2dm.C2DMessaging;

public class SetupActivity extends Activity {
	public static final String UPDATE_UI_ACTION = "com.prestomation.android.androidfiledrop.UPDATE_UI";
	public static final String AUTH_PERMISSION_ACTION = "com.prestomation.android.androidfiledrop.AUTH_PERMISSION";
	public static final String PREF_SCREEN_ID = "savedScreenId";
	public static final String PREF_DEVICE_NICKNAME = "deviceNickname";
	public static final String TAG = "AndroidFileDrop";

	private boolean mPendingAuth = false;
	private int mScreenId = -1;
	private int mAccountSelectedPosition = 0;
	private String mNickname = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int savedScreenId = Prefs.get(this).getInt(PREF_SCREEN_ID, R.layout.accountselection);

		setScreenContent(savedScreenId);

		registerReceiver(UpdateUIReceiver, new IntentFilter(UPDATE_UI_ACTION));
		registerReceiver(AuthPermissionReceiver, new IntentFilter(AUTH_PERMISSION_ACTION));
		mNickname = Prefs.get(this).getString(PREF_DEVICE_NICKNAME, android.os.Build.MODEL);

	}

	@Override
	public void onDestroy() {
		unregisterReceiver(UpdateUIReceiver);
		unregisterReceiver(AuthPermissionReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPendingAuth) {
			Log.i(TAG, "mPendingAuth is TRUE!");
			mPendingAuth = false;
			String regID = C2DMessaging.getRegistrationId(this);
			if (regID != null && !regID.equals("")) {
				CloudRegistrar.registerWithCloud(this, mNickname, regID);
			} else {
				C2DMessaging.register(this, CloudRegistrar.EMAIL_ID);
			}
		}
	}

	private void setScreenContent(int screenId) {
		mScreenId = screenId;

		setContentView(screenId);

		switch (screenId) {
		case R.layout.accountselection: {
			Log.i(TAG, "Entering account selection screen");
			setSelectAccountScreenContent();
			break;

		}

		case R.layout.select_options: {
			Log.i(TAG, "Entering options screen");
			setOptionsScreenContent();
			break;
		}

		}

		SharedPreferences.Editor editor = Prefs.get(this).edit();
		editor.putInt(PREF_SCREEN_ID, screenId);
		editor.commit();

	}

	private void setOptionsScreenContent() {
		// TODO Auto-generated method stub
		// Populate our options
		// Possible options:
		// 1. Automatically download file, or prompt
		// 2. Target directory for downloads

		Button clearPrefsButton = (Button) findViewById(R.id.clearSettings);
		clearPrefsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				resetPrefs();
				finish();

			}
		});
		clearPrefsButton.setEnabled(true);

	}

	private void setSelectAccountScreenContent() {
		Button backButton = (Button) findViewById(R.id.back);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		final Button nextButton = (Button) findViewById(R.id.next);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ListView listview = (ListView) findViewById(R.id.AccountSelectlistView);
				mAccountSelectedPosition = listview.getCheckedItemPosition();
				TextView account = (TextView) listview.getChildAt(mAccountSelectedPosition);
				promptForDeviceName(account.getText().toString());

			}
		});
		String accounts[] = getAccounts();
		ListView accountLV = (ListView) findViewById(R.id.AccountSelectlistView);
		accountLV.setAdapter(new ArrayAdapter<String>(this, R.layout.account, accounts));
		accountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		accountLV.setItemChecked(0, true);

	}

	private String[] getAccounts() {

		ArrayList<String> accounts = new ArrayList<String>();
		for (Account ac : AccountManager.get(this).getAccounts()) {
			if (ac.type.equals("com.google")) {
				accounts.add(ac.name);
			}
		}
		String[] accountArray = new String[accounts.size()];
		accounts.toArray(accountArray);
		return accountArray;
	}

	private void registerAccount(String theAccount) {
		SharedPreferences prefs = Prefs.get(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("accountName", theAccount);
		editor.commit();
		C2DMessaging.register(this, CloudRegistrar.EMAIL_ID);
	}

	private final BroadcastReceiver UpdateUIReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mScreenId == R.layout.accountselection) {
				// We must be in the middle of selecting account/registering
				handleConnectingUpdate(intent.getIntExtra(CloudRegistrar.STATUS_EXTRA,
						CloudRegistrar.ERROR_STATUS));

			}
			// TODO: disconnecting case
			else
			// else if (mScreenId == R.layout.connected)
			{

			}
		}
	};

	private void handleConnectingUpdate(int status) {
		if (status == CloudRegistrar.REGISTERED_STATUS) {
			setScreenContent(R.layout.select_options);
		} else {
			// There was an error
			Button nextButton = (Button) findViewById(R.id.next);
			nextButton.setEnabled(true);
		}

	}

	private final BroadcastReceiver AuthPermissionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getBundleExtra("AccountManagerBundle");
			if (extras != null) {
				Intent authIntent = (Intent) extras.get(AccountManager.KEY_INTENT);
				if (authIntent != null) {
					mPendingAuth = true;
					startActivity(authIntent);
				}
			}
		}
	};

	private void resetPrefs() {
		Prefs.deletePrefs(this);
	}

	private void promptForDeviceName(final String accountName) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Device Name");
		alert.setMessage("Please enter a name for this device");

		final EditText input = new EditText(this);
		input.setText(mNickname);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mNickname = input.getText().toString();
				SharedPreferences.Editor editor = Prefs.get(getBaseContext()).edit();
				editor.putString(PREF_DEVICE_NICKNAME, mNickname);
				editor.commit();
				// Disable the next button
				final Button nextButton = (Button) findViewById(R.id.next);
				nextButton.setEnabled(false);

				// Register the account
				registerAccount(accountName);

			}
		});

		alert.show();
	}

}