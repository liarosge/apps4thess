package com.lg.mobility.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.lg.mobility.R;
import com.lg.mobility.data.ARDeparture;
import com.lg.mobility.data.ARStop;
import com.lg.mobility.data.DataHandler;
import com.lg.mobility.data.MobilityApplication;
import com.lg.mobility.data.StaticStopModel;
import com.lg.mobility.data.TimetableAdapter;
import com.lg.mobility.data.TimetableModel;
import com.lg.mobility.fragments.Fragment_AR_Mobility;
import com.lg.mobility.services.DataService;
import com.lg.mobility.utilities.GeometryTouchListener;

import eu.liveandgov.ar.utilities.Entity;

public class ARActivity extends FragmentActivity implements OnClickListener, GeometryTouchListener {

	public static Fragment_AR_Mobility arFragment;
	public static Context ctx;
	private BroadcastReceiver _receiver;
	private ProgressDialog pd;
	private View timetables;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		Log.d("ARActivity", "onCreate()");
		setContentView(R.layout.activity_ar);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().hide();
		}
		if(arFragment == null)
		{
//			MobilityApplication app = (MobilityApplication) getApplication();
//			List<StaticStopModel> stops = app.dHandler.getNearestStops(this, new LatLng(MapActivity.lastLoc.getLatitude(), MapActivity.lastLoc.getLongitude()));
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			arFragment = new Fragment_AR_Mobility(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
			ft.add(R.id.ar_container_view, arFragment);
			ft.commit();
//			ArrayList<Entity> entities = new ArrayList<Entity>();
//			for(StaticStopModel stop : stops)
//			{
//				Log.i("ARActivity", "adding stop " + stop.name);
//				Location stopLoc = new Location("");
//				stopLoc.setLatitude(stop.coord.latitude);
//				stopLoc.setLongitude(stop.coord.longitude);
//				entities.add(new Entity("" + stop.id, stop.name, "" , "", stopLoc, "", "", "", "", "", ""));
//			}
//			arFragment.updateEntities(entities);
			
		}
		else
		{
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.ar_container_view, arFragment);
			ft.commit();
//			if(arFragment.isDetached())
//			{
//				android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//				ft.attach(arFragment);
//				ft.commit();
//			}
		}
		if(arFragment != null)
			arFragment.setTheListener(this);
//		arFragment = new Fragment_AR_Mobility(entitiesLBS, BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_logo));
//		ft.add(R.id.ar_container_view, arFragment);
//		ft.commit();
		_receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(action.equals(DataService.ACTION_RETURN_STOP_DEPARTURES))
				{
					presentTimeTables();
				}
				else if(action.equals(DataService.ACTION_RETURN_AR_STOPS))
				{
					Log.i("ARActivity", "received ar stops");
					presentStops();
				}
			}
		};
	}
	
	private void presentStops()
	{
		MobilityApplication app = (MobilityApplication) getApplication();
		List<StaticStopModel> stops = app.dHandler.getNearestStops(this, new LatLng(MapActivity.lastLoc.getLatitude(), MapActivity.lastLoc.getLongitude()));
		ArrayList<Entity> entities = new ArrayList<Entity>();
		for(StaticStopModel stop : stops)
		{
			Log.i("ARActivity", "adding stop " + stop.name);
			Location stopLoc = new Location("");
			stopLoc.setLatitude(stop.coord.latitude);
			stopLoc.setLongitude(stop.coord.longitude);
			entities.add(new Entity("" + stop.id, stop.name, "" , "", stopLoc, "", "", "", "", "", ""));
		}
		arFragment.updateEntities(entities);
		
	}
//	private void presentStops()
//	{
//		DataHandler dHandler = ((MobilityApplication) getApplication()).dHandler;
//		List<ARStop> arStops = dHandler.getARStops();
//		Log.d("ARActivity", "arStops size = " + arStops.size());
//		ArrayList<Entity> entities = new ArrayList<Entity>();
//		for(ARStop stop : arStops)
//		{
//			Location stopLoc = new Location(stop.code);
//			stopLoc.setLatitude(stop.coords.latitude);
//			stopLoc.setLongitude(stop.coords.longitude);
//			Log.d("ARActivity" , "adding code:" + stop.code + " name:" + stop.name + " latitude:" + stopLoc.getLatitude() + " longitude:" + stopLoc.getLongitude());
//			entities.add(new Entity(""+ stop.code, stop.name, "", "", stopLoc, "", "", "", "", "", ""));
//		}
//		arFragment.updateEntities(entities);
//	}
	public void presentTimeTables()
	{
		if (pd!=null) {
			pd.dismiss();
		}
		if(timetables == null)
		{
			DataHandler dHandler = ((MobilityApplication) getApplication()).dHandler;
			List<ARDeparture> arDepartures = dHandler.getARDepartures();
			String title = arDepartures.get(0).stopName;
			ArrayList<TimetableModel> timetableEntries = new ArrayList<TimetableModel>(arDepartures.size());
			for(ARDeparture departure : arDepartures)
			{
				String direction;
				if(departure.lineAssoc.direction ==1)
					direction = departure.lineAssoc.stopName + " " + departure.time;
				else
					direction = departure.lineAssoc.startName + " " + departure.time;
				timetableEntries.add(new TimetableModel(departure.lineAssoc, departure.time));
			}
//			TimetableAdapter timetableAdapte = new LinesAdapter(this, R.layout.line_list_item, lines, new Drawable[]{getResources().getDrawable(R.drawable.ic_bus_gray), getResources().getDrawable(R.drawable.ic_ferry_gray), getResources().getDrawable(R.drawable.ic_tram_gray), getResources().getDrawable(R.drawable.ic_unknown_gray), getResources().getDrawable(R.drawable.ic_rail_gray)});
			TimetableAdapter timetableAdapter = new TimetableAdapter(this, R.layout.timetable_list_item, timetableEntries, new Drawable[]{getResources().getDrawable(R.drawable.ic_bus_gray), getResources().getDrawable(R.drawable.ic_ferry_gray), getResources().getDrawable(R.drawable.ic_tram_gray), getResources().getDrawable(R.drawable.ic_unknown_gray), getResources().getDrawable(R.drawable.ic_rail_gray)});
			timetables = getLayoutInflater().inflate(R.layout.timetable_layout_list, null);
			FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lparams.setMargins(20, 150, 20, 20);
			timetables.setLayoutParams(lparams);
			TextView titleView = (TextView) timetables.findViewById(R.id.timetables_title);
			titleView.setText(title);
//			TextView descriptionView = (TextView) timetables.findViewById(R.id.timetables_content);
//			descriptionView.setText(descriptionBuilder);
			ListView timetableList = (ListView) timetables.findViewById(R.id.timetables_list_view);
			timetableList.setAdapter(timetableAdapter);
			FrameLayout vg = (FrameLayout) findViewById(R.id.ar_container_view);
			vg.addView(timetables, lparams);
			AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
			animation.setDuration(1000);
			animation.setFillAfter(true);
			timetables.startAnimation(animation);
			ScaleAnimation scaleAnimation = new ScaleAnimation(0.4f, 1.0f, 0.4f, 1.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(300);
			animation.setFillAfter(true);
			timetables.startAnimation(scaleAnimation);
			timetables.findViewById(R.id.timetables_close_button).setOnClickListener(this);
			timetables.bringToFront();
		}
	}
	@Override
	protected void onResume() {
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		Log.d("ARActivity", "onResume");
		Intent intent = new Intent(this, DataService.class);
		intent.setAction(DataService.ACTION_GET_AR_STOPS);
		startService(intent);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DataService.ACTION_RETURN_AR_STOPS);
		intentFilter.addAction(DataService.ACTION_RETURN_STOP_DEPARTURES);
		registerReceiver(_receiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(_receiver);
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.timetables_close_button) {
			FrameLayout vg = (FrameLayout) findViewById(R.id.ar_container_view);
			AlphaAnimation animation1 = new AlphaAnimation(1.0f, 0.0f);
			animation1.setDuration(1000);
			animation1.setFillAfter(true);
			timetables.startAnimation(animation1);
			vg.removeView(timetables);
			timetables = null;
		}
	}
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, MapActivity.class).putExtra("AR", true));
	}
	
	@Override
	protected void onDestroy() {
		Log.d("ARActivity", "onDestroy()");
		super.onDestroy();
	}
	
	@Override
	public void onGeometryTouched(String code) {
		Log.i("ARActivity", "geometry code = " + code);
		pd = new ProgressDialog(this);
		pd.setTitle("Getting stop info");
		pd.setMessage("Please wait...");
		pd.setCancelable(true);
		pd.setCanceledOnTouchOutside(true);
		pd.setIndeterminate(true);
		pd.show();
		Intent intent = new Intent(this, DataService.class);
		intent.setAction(DataService.ACTION_GET_AR_DEPARTURES);
		intent.putExtra(DataService.INTENT_AR_DEPARTURES_CODE_TAG, code);
		startService(intent);
		
	}
}
