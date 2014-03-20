package com.lg.mobility.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.lg.mobility.R;
import com.lg.mobility.data.DataHandler;
import com.lg.mobility.data.MobilityApplication;
import com.lg.mobility.services.ServiceCentreService;
import com.lg.mobility.utilities.ServiceCentreUtils;

public class StartActivity extends Activity {
	
	private StartActivity ctx = this;
	private DataHandler dHandler;
	public static volatile boolean analyticsAccepted = false;
	public static volatile boolean userIDReceived = false;
	public static volatile boolean permissionsReceived = false;
	private BroadcastReceiver _receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(ServiceCentreService.ACTION_RETURN_USER_ID)){
				//user id has been retrieved from service center
				onUserIDReceived();
			}
			else if(action.equals(ServiceCentreService.ACTION_RETURN_USER_PERMISSIONS)){
				//permissions have been received
				onPermissionsReceived();
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_start);
		MobilityApplication app = (MobilityApplication) getApplication();
		dHandler = app.dHandler;
		final SharedPreferences prefsAnalytics = getSharedPreferences("analytics", 0);
		
		//check if using analytics have been previously accepted by the user
		if(!prefsAnalytics.getBoolean("ENABLED", false))
		{
			//accept analytics dialog
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Google Analytics");
			alert.setMessage("This application collects anonymous data that are used to improve the overall user experience. You must agree with this policy to use the application.");
			alert.setPositiveButton("I agree", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				SharedPreferences.Editor editor = prefsAnalytics.edit();
				editor.putBoolean("ENABLED", true);
				editor.commit();
				StartActivity.analyticsAccepted = true;
				if(StartActivity.userIDReceived && StartActivity.permissionsReceived)
					ctx.startActivity(new Intent(ctx, MapActivity.class));
			  }
			});

			alert.setNegativeButton("I disagree", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    finish();
			  }
			});

			alert.show();
		}
		else
		{
			final SharedPreferences userPrefs = getSharedPreferences(ServiceCentreService.USER_INFO_PREFS, 0);
			String userId = userPrefs.getString(ServiceCentreService.USER_INFO_TAG_USER_ID, null);
			if(userId != null){
				dHandler.getUserInfo().setUserID(userId);
				//if already received user id from previous session and analytics accepted, start immediately
				startActivity(new Intent(this, MapActivity.class));
				super.onCreate(savedInstanceState);
				return;
			}
			else
			{
				//if user id unavailable wait for it
				StartActivity.analyticsAccepted = true;
			}
		}
		//get/update user id
		requestServiceCentre(ServiceCentreService.ACTION_GET_ANON_USER_ID);
		//get/update permissions
		requestServiceCentre(ServiceCentreService.ACTION_GET_PERMISSIONS_LIST);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		//register receiver for data input
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ServiceCentreService.ACTION_RETURN_USER_ID);
		intentFilter.addAction(ServiceCentreService.ACTION_RETURN_USER_PERMISSIONS);
		registerReceiver(_receiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		//unregister receiver if already registered
		try{
			unregisterReceiver(_receiver);
		}
		catch (IllegalArgumentException e)
		{
		}
		super.onStop();
	}
	
	private void onUserIDReceived()
	{
		Log.i("StartActivity", "user id received = " + dHandler.getUserInfo().getUserID());
		try{
			if(dHandler.getUserInfo().getUserID() == null)
			{
				//if couldn't get user id -> probably not connected to the internet.. internet connection required for the first run of the app/cant run without user id
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle("Could not register the device");
				alert.setMessage("An internet connection is required when the application starts for the first time.\n The application will terminate now.");
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				  finish();
				  }
				});
				alert.show();
				return;
			}
		}
		catch(Exception e)
		{
			//strange error
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Could not register the device");
			alert.setMessage("An internet connection is required when the application starts for the first time.\n The application will terminate now."); 
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  finish();
			  }
			});
			alert.show();
			return;
		}
		if(StartActivity.analyticsAccepted && StartActivity.permissionsReceived)
			//start application if already received permissions and analytics accepted
			startActivity(new Intent(this, MapActivity.class));
		StartActivity.userIDReceived = true;
	}
	
	private void onPermissionsReceived()
	{
		SharedPreferences userPrefs = getSharedPreferences(ServiceCentreService.USER_INFO_PREFS, 0);
		int permissionsNumber = userPrefs.getAll().size();
		Log.i("StartActivity", "number of permissions = " + permissionsNumber);
		if(StartActivity.analyticsAccepted && StartActivity.userIDReceived)
			//start application if already received user id and analytics accepted
			startActivity(new Intent(this, MapActivity.class));
		StartActivity.permissionsReceived = true;		
	}
	
	//call service centre api
	private void requestServiceCentre(String action)
	{
		Intent intent = new Intent(this, ServiceCentreService.class);
		intent.setAction(action);
		startService(intent);
	}
}
