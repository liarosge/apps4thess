package com.lg.mobility.activities;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import eu.livegov.libraries.issuereporting.activities.NewReport;
import eu.livegov.libraries.issuereporting.interfaces.OnMenuItemSelected;
import eu.livegov.libraries.issuereporting.utils.Constants;

public class ReportActivity extends NewReport {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) 
	    {
	    case android.R.id.home: 
	    	onBackPressed();
	        break;
	    default:
	     	return super.onOptionsItemSelected(item);
	    }
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onMenuItemSelected(int menuId) {
		if(menuId == Constants.MENU_NEWREPORT)
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "menu_item_selected", "new_report", null).build());
		else if(menuId == Constants.LOCATION)
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "menu_item_selected", "location", null).build());
		else if(menuId == Constants.CATEGORY)
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "menu_item_selected", "category", null).build());
		else if(menuId == Constants.REMARK)
			EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "menu_item_selected", "remark", null).build());
		super.onMenuItemSelected(menuId);
	}
	
	@Override
	public void clearReport() {
		EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "clear_report", null).build());
		super.clearReport();
	}
	
	@Override
	public void sendReport() {
		EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", "button_press", "send_report", null).build());
		super.sendReport();
	}
}
