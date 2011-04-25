package com.prestomation.android.androidfiledrop;

import java.util.ArrayList;

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
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.c2dm.C2DMessaging;

public class SetupActivity extends Activity {
	public static final String UPDATE_UI_ACTION = "com.prestomation.android.androidfiledrop.UPDATE_UI";
	public static final String AUTH_PERMISSION_ACTION = "com.prestomation.android.androidfiledrop.AUTH_PERMISSION";

	private boolean mPendingAuth = false;
	private int mAccountSelectedPosition = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accountselection);
		setSelectAccount();
		
		registerReceiver(AuthPermissionReceiver, new IntentFilter(AUTH_PERMISSION_ACTION)) ;

	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(AuthPermissionReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(mPendingAuth){
			mPendingAuth = false;
			String regID = C2DMessaging.getRegistrationId(this);
			if (regID != null && !regID.equals("")){
				CloudRegistrar.registerWithCloud(this, regID);
			}
			else{
			C2DMessaging.register(this, CloudRegistrar.EMAIL_ID);
			}
		}
	}
	
	private void setSelectAccount() {
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
				nextButton.setEnabled(false);
				registerAccount((String) account.getText());
				
			}
		});
		String accounts[] = getAccounts();
		ListView accountLV = (ListView) findViewById(R.id.AccountSelectlistView)
;
		accountLV.setAdapter(new ArrayAdapter<String>(this, R.layout.account, accounts));
		accountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		accountLV.setItemChecked(0, true);
		

	}
	
	private String[] getAccounts(){
		
		ArrayList<String> accounts = new ArrayList<String>();
		for (Account ac : AccountManager.get(this).getAccounts()){
			if (ac.type.equals("com.google")){
				accounts.add(ac.name);
			}
		}
		String[] accountArray = new String[accounts.size()];
		accounts.toArray(accountArray);
		return accountArray;
	}
	private void registerAccount(String theAccount)
	{
			SharedPreferences prefs = this.getSharedPreferences("AndroidFileDrop_Prefs", 0);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("accountName", theAccount);
			editor.commit();
			C2DMessaging.register(this, CloudRegistrar.EMAIL_ID);
	}
	
	private final BroadcastReceiver AuthPermissionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getBundleExtra("AccountManagerBundle");
			if (extras != null ){
				Intent authIntent = (Intent) extras.get(AccountManager.KEY_INTENT);
				if (authIntent != null) {
					mPendingAuth = true;
					startActivity(authIntent);
				}
			}
		}
	};
	
}