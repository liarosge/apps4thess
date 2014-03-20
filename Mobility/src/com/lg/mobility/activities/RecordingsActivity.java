package com.lg.mobility.activities;

import static eu.liveandgov.wp1.sensor_collector.configuration.IntentAPI.ACTION_TRANSFER_SAMPLES;
import static eu.liveandgov.wp1.sensor_collector.configuration.ExtendedIntentAPI.ACTION_DELETE_SAMPLES;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter.LengthFilter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lg.mobility.R;
import com.lg.mobility.data.RecordingsAdapter;
import com.lg.mobility.data.RecordingsModel;
import com.lg.mobility.data.TransportationType;
import com.lg.mobility.services.ServiceCentreService;

import eu.liveandgov.wp1.sensor_collector.ServiceSensorControl;
import eu.liveandgov.wp1.sensor_collector.ServiceSensorControl.SensorServiceListener;
import eu.liveandgov.wp1.sensor_collector.persistence.PublicationPipeline;
import eu.liveandgov.wp1.sensor_collector.sensors.SensorSerializer;

public class RecordingsActivity extends FragmentActivity implements OnClickListener, SensorServiceListener {
	private Context ctx = this;
	private SharedPreferences userPrefs;
	private SupportMapFragment mMapfragment;
	private GoogleMap mGoogleMap;
	private ListView mRecordingsList;
	private Dialog dialog;
	private int mId;
	private static ArrayList<ArrayList<LatLng>> routes;
	private static List<Drawable> drawableList;
	private RecordingsAdapter recAdapter;
	private List<RecordingsModel> dataList;
	private ServiceSensorControl sensorService;
	private boolean bound;
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			ServiceSensorControl.LocalBinder binder = (ServiceSensorControl.LocalBinder) service;
			sensorService = binder.getService();
			bound = true;
			if((sensorService.samplesStored()))
			{
				Button submitButton = (Button) findViewById(R.id.recordings_submit_btn);
				submitButton.setOnClickListener((RecordingsActivity)ctx);
				Button deleteButton = (Button) findViewById(R.id.recordings_delete_btn);
				deleteButton.setOnClickListener((RecordingsActivity)ctx);
				submitButton.setEnabled(true);
				deleteButton.setEnabled(true);
				sensorService.setOnSamplesDeletedListener((RecordingsActivity)ctx);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};
	@SuppressLint("NewApi")
	@Override
	//TODO: bind service to get delete callback and storedSamples?
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordings);
		userPrefs = getSharedPreferences(ServiceCentreService.USER_INFO_PREFS, 0);
		mRecordingsList = (ListView) findViewById(R.id.recordings_list);
		if(drawableList == null)
		{
			drawableList = new ArrayList<Drawable>();
			drawableList.add(getResources().getDrawable(R.drawable.ic_running_gray));
			drawableList.add(getResources().getDrawable(R.drawable.ic_walking_gray));
			drawableList.add(getResources().getDrawable(R.drawable.ic_sitting_gray));
			drawableList.add(getResources().getDrawable(R.drawable.ic_standing_gray));
			drawableList.add(getResources().getDrawable(R.drawable.ic_ontable_gray));
			drawableList.add(getResources().getDrawable(R.drawable.ic_unknown_gray));
		}
		if(dataList == null)
		{
			dataList = new ArrayList<RecordingsModel>();
			recAdapter = new RecordingsAdapter(this, R.layout.recordings_list_item, dataList, drawableList);
			routes = new ArrayList<ArrayList<LatLng>>();
			new ListDataLoader().execute(new Void[1]);
		}
		else
		{
			recAdapter = new RecordingsAdapter(this, R.layout.recordings_list_item, dataList, drawableList);
		}
		mRecordingsList.setAdapter(recAdapter);
		mRecordingsList.setOnItemClickListener(new RecordingsItemClickListener());
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	    dialog = new Dialog(this);
	    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    dialog.setContentView(R.layout.recordings_dialog);
		if(mMapfragment== null)
		{
			android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
			mMapfragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map_dialog);
			mGoogleMap = mMapfragment.getMap();
			UiSettings settings = mGoogleMap.getUiSettings();
			settings.setScrollGesturesEnabled(true);
			settings.setMyLocationButtonEnabled(false);
			settings.setZoomControlsEnabled(false);
		}
		permissionCheck();
	}
	@SuppressLint("NewApi")
	private void permissionCheck()
	{
		if(!userPrefs.getString(ServiceCentreService.MOB_CLIENT_SUBMITRECORDING, "0").equals("1"))
		{
			Button submitButton = (Button) findViewById(R.id.recordings_submit_btn);
			submitButton.setEnabled(false);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				submitButton.setAlpha(0.3f);
			}
		}
		if(!userPrefs.getString(ServiceCentreService.MOB_CLIENT_DELETERECORDING, "0").equals("1"))
		{
			Button deleteButton = (Button) findViewById(R.id.recordings_delete_btn);
			deleteButton.setEnabled(false);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				deleteButton.setAlpha(0.3f);
			}
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, ServiceSensorControl.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		unbindService(mConnection);
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	private class ListDataLoader extends AsyncTask<Void, RecordingsModel, Void>
	{
		
		@Override
		protected void onPreExecute() {
			//TODO : Progress bar
			ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@SuppressLint("DefaultLocale")
		@Override
		protected Void doInBackground(Void... params) {
			//TODO: Smarter parsing..
//			File file = new File(PublicationPipeline.publishFile);
			File file = PublicationPipeline.publishFile;
			int count = 0;;
			try {
				count = count(file);
			} catch (IOException e2) {
				e2.printStackTrace();
				return null;
			}
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				return null;
			}
			String line;
			boolean first = false;
			int recordingCounter = 1;
			Long firstTimeStamp = null;
			Long tempTimeStamp = null;
			Long currentRouteDuration = null;
			LatLng firstLatLon = null;
			LatLng tempLatLon = null;
			int lineCount = 0;
			TransportationType currentTransportationType = TransportationType.UNKNOWN;
			ArrayList<LatLng> route = new ArrayList<LatLng>();
			float currentDistance = 0.0f;
			LatLng previousLatLon = null;
			try {
				while ((line = br.readLine()) != null) {
					lineCount ++;
					if(line.startsWith("TAG"))
					{
						String[] chunks = line.split(",");
						if(chunks[3].equals("\"START_RECORDING\"")){
							firstTimeStamp = Long.parseLong(chunks[1]);
							route = new ArrayList<LatLng>();
							first = true;
							currentDistance = 0.0f;
							previousLatLon = null;
//							tempLatLon = null;
						}
						else if(chunks[3].equals("\"STOP_RECORDING\"")){
							if(first) continue;//route is empty
							routes.add(route);
							tempTimeStamp = Long.parseLong(chunks[1]);
							long differenceInSecs = 0;
							try{
								differenceInSecs = (tempTimeStamp - firstTimeStamp)/1000;
							}
							catch(NullPointerException e)
							{
								e.printStackTrace();
							}
							if(differenceInSecs < 0)
								differenceInSecs = 0;
							String time;
							String distance;
							float[] result = new float[1];
							try{
								Location.distanceBetween(firstLatLon.latitude, firstLatLon.longitude, tempLatLon.latitude, tempLatLon.longitude, result);
							}
							catch(NullPointerException e)
							{
								result = new float[]{0};
							}
							if(currentDistance > 1000)
//								distance = String.format("%.3f km", result[0]/1000.0f);
								distance = String.format("%.3f km", currentDistance/1000.0f);
							else
//								distance = String.format("%.1f m", result[0]);
								distance = String.format("%.1f m", currentDistance);
							if(differenceInSecs > 3600)
							{
								if(differenceInSecs % 3600 > 60)
								{
									time = String.format("%d:%02d:%02d", (differenceInSecs/3600), 
											((differenceInSecs%3600)/60), (differenceInSecs%3600)%60);
								}
								else
								{
									time = String.format("%d:00:%02d", (differenceInSecs/3600), differenceInSecs%3600);
								}
							}
							else if(differenceInSecs > 60)
							{
								time = String.format("%02d:%02d", (differenceInSecs/60), differenceInSecs%60);
							}
							else {
								time = String.format("00:%02d", differenceInSecs%60);
							}
							String speed;
							if(differenceInSecs == 0 || currentDistance == 0.0f)
							{
								speed = "-";
							}
							else
							{
								speed = String.format("%.1f km/h",(currentDistance/1000.0f)/((float) differenceInSecs/ 3600.0f));
							}
							Date recordingDate = new Date(firstTimeStamp);
							
						    String date = DateFormat.getMediumDateFormat(ctx).format(recordingDate) + " " + DateFormat.getTimeFormat(ctx).format(recordingDate);
							publishProgress(new RecordingsModel("Recording " + recordingCounter, time, currentTransportationType, distance,(int)(((float)lineCount/(float)count)*100), speed, date));
							
							currentTransportationType = TransportationType.UNKNOWN;
							recordingCounter++;
						}
						else if(chunks[3].startsWith("\"Selected"))
						{
							String[] chunkschunks = chunks[3].split("\\s");
							String activity = chunkschunks[chunkschunks.length-1];
							activity = activity.substring(0, activity.length()-1);
							if(activity.equals("running"))
							{
								currentTransportationType = TransportationType.RUNNING;
							}
							else if(activity.equals("walking"))
							{
								currentTransportationType = TransportationType.WALKING;
							}
							else if(activity.equals("sitting"))
							{
								currentTransportationType = TransportationType.SITTING;
							}
							else if(activity.equals("standing"))
							{
								currentTransportationType = TransportationType.STANDING;
							}
							else if(activity.equals("on table"))
							{
								currentTransportationType = TransportationType.ONTABLE;
							}
							else if(activity.equals("unknown"))
							{
								currentTransportationType = TransportationType.UNKNOWN;
							}
						}
						else
						{
							continue;
						}
					}
					else if(line.startsWith("GPS"))
					{
						String[] chunks = line.split(",");
						String[] gpsData = chunks[3].split("\\s+");
						LatLng latlon = new LatLng(Double.parseDouble(gpsData[0]), Double.parseDouble(gpsData[1]));
						if(previousLatLon != null)
						{
							float[] dist = new float[1];
							Location.distanceBetween(previousLatLon.latitude, previousLatLon.longitude, latlon.latitude, latlon.longitude, dist);
							currentDistance += dist[0];
						}
						previousLatLon = latlon;
						route.add(latlon);
						if(first)
						{
							firstTimeStamp = Long.parseLong(chunks[1]);
							firstLatLon = latlon;
							first = false;
						}
						else
						{
							tempLatLon = latlon;
							tempTimeStamp = Long.parseLong(chunks[1]);
						}
					}
					else if(line.startsWith("ACT"))
					{
						String activity = line.split(",")[3];
						if(activity.equals("\"running\""))
						{
							currentTransportationType = TransportationType.RUNNING;
						}
						else if(activity.equals("\"walking\""))
						{
							currentTransportationType = TransportationType.WALKING;
						}
						else if(activity.equals("\"sitting\""))
						{
							currentTransportationType = TransportationType.SITTING;
						}
						else if(activity.equals("\"standing\""))
						{
							currentTransportationType = TransportationType.STANDING;
						}
						else if(activity.equals("\"on table\""))
						{
							currentTransportationType = TransportationType.ONTABLE;
						}
						else if(activity.equals("\"unknown\""))
						{
							currentTransportationType = TransportationType.UNKNOWN;
						}
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(RecordingsModel... values) {
			recAdapter.add(values[0]);
			recAdapter.notifyDataSetChanged();
			ProgressBar progress = (ProgressBar) findViewById(R.id.progress_bar);
			progress.setProgress(values[0].progress);
			mRecordingsList.invalidate();
			super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(Void result) {
			ProgressBar progress = (ProgressBar) findViewById(R.id.progress_bar);
			progress.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
		
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return recAdapter.getData();
	}
	@Override
	   public boolean onOptionsItemSelected(MenuItem item) 
	   {
	      switch (item.getItemId()) 
	       {
	       case android.R.id.home: 
	           onBackPressed();
	           break;
	        default:
	        	return super.onOptionsItemSelected(item);
        }
        return true;
    }
	
	public int count(File file) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(file));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
    private class RecordingsItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "on_recording", null).build());
        	mGoogleMap.clear();
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(position).get(0),16.0f));
			mGoogleMap.addPolyline(new PolylineOptions().addAll(routes.get(position)).width(10.0f).color(Color.argb(255, 0, 255, 255)));
			TextView tv = (TextView) dialog.findViewById(R.id.recordings_dialog_duration_text_view);
			tv.setText(recAdapter.getItem(position).duration);
			tv.bringToFront();
			TextView tv2 = (TextView) dialog.findViewById(R.id.recordings_dialog_distance_text_view);
			tv2.setText(recAdapter.getItem(position).distance);
			tv2.bringToFront();
			TextView tv3 = (TextView) dialog.findViewById(R.id.recordings_dialog_speed_text_view);
			tv3.setText(recAdapter.getItem(position).speed);
			tv3.bringToFront();
        	dialog.show();
        	tv.invalidate();
        	tv2.invalidate();
        }
    }
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.recordings_submit_btn)
		{
			if(sensorService.transferManager.isTransferring())
			{
				Toast.makeText(this, "Transferring is in progress, please wait", Toast.LENGTH_LONG).show();
				return;
			}
			new AlertDialog.Builder(this)
			.setTitle("Confirm")
			.setMessage("Upload stored samples?\n\nPlease make sure that data charges will not be applied on your account.")
			.setIcon(R.drawable.ic_alert_2)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(ctx, ServiceSensorControl.class);
				    intent.setAction(ACTION_TRANSFER_SAMPLES);
				    try{
				    	EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "recordings_submit_dialog", null).build());
//				    	EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("sensor_collector", "start_transfering_samples", null, sensorService.getSampleSize()).build());
				    	EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("sensor_collector", "on_transfer", "number_of_recordings", (long) recAdapter.getCount()).build());
				    }
				    catch(Exception e){}
				    startService(intent);
				    Toast.makeText(ctx, "Transferring recordings..", Toast.LENGTH_LONG).show();
				}
			})
			.setNegativeButton(android.R.string.no, null).show();
		}
		else if(v.getId() == R.id.recordings_delete_btn)
		{
			if(sensorService.transferManager.isTransferring())
			{
				Toast.makeText(this, "Transferring is in progress, please wait", Toast.LENGTH_LONG).show();
				return;
			}
			new AlertDialog.Builder(this)
			.setTitle("Confirm")
			.setMessage("Delete all stored samples?")
			.setIcon(R.drawable.ic_alert_2)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
//			    	dataList = new ArrayList<RecordingsModel>();
//					recAdapter = new RecordingsAdapter(ctx, R.layout.recordings_list_item, dataList, drawableList);
//					routes = new ArrayList<ArrayList<LatLng>>();
//					ListView lv = (ListView) findViewById(R.id.recordings_list);
//					lv.setAdapter(recAdapter);
//					new ListDataLoader().execute(new Void[1]);
			    	try{
			    		EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "recordings_delete_dialog", null).build());
//			    		EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("sensor_collector", "delete_stored_samples", null, sensorService.getSampleSize()).build());
			    	}
			    	catch(Exception e){}
					Intent intent = new Intent(ctx, ServiceSensorControl.class);
					intent.setAction(ACTION_DELETE_SAMPLES);
					startService(intent);
			    }})
			 .setNegativeButton(android.R.string.no, null).show();
			
//			dataList = new ArrayList<RecordingsModel>();
//			recAdapter = new RecordingsAdapter(this, R.layout.recordings_list_item, dataList, drawableList);
//			routes = new ArrayList<ArrayList<LatLng>>();
//			ListView lv = (ListView) findViewById(R.id.recordings_list);
//			lv.setAdapter(recAdapter);
//			new ListDataLoader().execute(new Void[1]);
		}
	}

	@Override
	public void onDeletionCompleted() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				recAdapter.clear();
				recAdapter.notifyDataSetChanged();
				Button submitButton = (Button) findViewById(R.id.recordings_submit_btn);
				submitButton.setEnabled(false);
				Button deleteButton = (Button) findViewById(R.id.recordings_delete_btn);
				deleteButton.setEnabled(false);
			}
		});		
//		dataList = new ArrayList<RecordingsModel>();
//		recAdapter = new RecordingsAdapter(this, R.layout.recordings_list_item, dataList, drawableList);
//		routes = new ArrayList<ArrayList<LatLng>>();
//		ListView lv = (ListView) findViewById(R.id.recordings_list);
//		lv.setAdapter(recAdapter);
//		new ListDataLoader().execute(new Void[1]);
	}
}
