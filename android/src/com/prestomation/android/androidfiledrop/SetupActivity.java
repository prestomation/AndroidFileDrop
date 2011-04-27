package com.prestomation.android.androidfiledrop;

import java.util.ArrayList;

import android.R.layout;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.c2dm.C2DMessaging;

public class SetupActivity extends Activity {
	public static final String UPDATE_UI_ACTION = "com.prestomation.android.androidfiledrop.UPDATE_UI";
	public static final String AUTH_PERMISSION_ACTION = "com.prestomation.android.androidfiledrop.AUTH_PERMISSION";
	public static final String TAG = "AndroidFileDrop";

	private boolean mPendingAuth = false;
	private int mScreenId = -1;
	private int mAccountSelectedPosition = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int savedScreenId = Prefs.get(this).getInt("savedScreenId",
				R.layout.accountselection);

		setScreenContent(savedScreenId);

		registerReceiver(UpdateUIReceiver, new IntentFilter(UPDATE_UI_ACTION));
		registerReceiver(AuthPermissionReceiver, new IntentFilter(
				AUTH_PERMISSION_ACTION));

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
			mPendingAuth = false;
			String regID = C2DMessaging.getRegistrationId(this);
			if (regID != null && !regID.equals("")) {
				CloudRegistrar.registerWithCloud(this, regID);
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
		editor.putInt("savedScreenId", screenId);
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
				TextView account = (TextView) listview
						.getChildAt(mAccountSelectedPosition);
				nextButton.setEnabled(false);
				registerAccount((String) account.getText());

			}
		});
		String accounts[] = getAccounts();
		ListView accountLV = (ListView) findViewById(R.id.AccountSelectlistView);
		accountLV.setAdapter(new ArrayAdapter<String>(this, R.layout.account,
				accounts));
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
				handleConnectingUpdate(intent.getIntExtra(
						CloudRegistrar.STATUS_EXTRA,
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
				Intent authIntent = (Intent) extras
						.get(AccountManager.KEY_INTENT);
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

}