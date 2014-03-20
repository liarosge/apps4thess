package com.lg.mobility.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "Mobility";
	public static final int DATABASE_VERSION = 1;
  
	public static final String TABLE_STOPS = "tblStops";
	
	final String KEY_STOP_ID = "stop_id";
	final String KEY_STOP_NAME = "stop_name";
	final String KEY_STOP_LAT = "stop_lat";
	final String KEY_STOP_LON ="stop_lon";
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String c = "CREATE TABLE " + TABLE_STOPS + "("
				+ KEY_STOP_ID + " INTEGER PRIMARY KEY,"
				+ KEY_STOP_NAME + " TEXT,"
				+ KEY_STOP_LAT + " REAL,"
				+ KEY_STOP_LON + " REAL)";
		db.execSQL(c);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOPS);
		// Create tables again
		onCreate(db);

	}
	public boolean isTableExists(String tableName, boolean openDb) {
//	    if(openDb) {
//	        if(mDatabase == null || !mDatabase.isOpen()) {
//	            mDatabase = getReadableDatabase();
//	        }
//
//	        if(!mDatabase.isReadOnly()) {
//	            mDatabase.close();
//	            mDatabase = getReadableDatabase();
//	        }
//	    }

	    Cursor cursor = getReadableDatabase().rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if(cursor!=null) {
	        if(cursor.getCount()>0) {
	        	cursor.close();
	            return true;
	        }
	        cursor.close();
	    }
	    return false;
	}
	
	/* the approach:
	 * get all stops with difference from user user latitude +- 0.005887 and longitude +- 0.021076
	 * this returns all stops with at least 500m Distance from user
	 * on the returned results use java objects for more accurate distance and reapply filter to 500m.
	 */
	
//	public static final double LATITUDE_DIFF = 1.005887;
//	public static final double LONGITUDE_DIFF = 1.021076;
	public static final double LATITUDE_DIFF = 50.0; //test for koblenz
	public static final double LONGITUDE_DIFF = 50.0; //test for koblenz
	public static final double TEST_LAT =  60.189025;
	public static final double TEST_LON =  24.955765;
//	public static final float DISTANCE_FILTER = 2600.0f;
	public static final float DISTANCE_FILTER = 15000000.0f;//test for koblenz
	public List<StaticStopModel> getNearestStops(double userLatitude, double userLongitude)
	{
		//SELECT * FROM `AugReal_ARImages` WHERE (id between 300 and 320) and (image_count between 20 and 80)
		String lat1 = "" +(userLatitude - LATITUDE_DIFF);
		String lat2 = "" + (userLatitude + LATITUDE_DIFF);
		String lon1 = "" + (userLongitude - LONGITUDE_DIFF);
		String lon2 = "" + (userLongitude + LONGITUDE_DIFF);
		Cursor stopsCursor = getReadableDatabase().rawQuery("select * from " + TABLE_STOPS + " where (" + KEY_STOP_LAT + " between " + lat1 + " and " + lat2 + ") and (" + KEY_STOP_LON + " between " + lon1 + " and " + lon2 + ")", null);
		ArrayList<StaticStopModel> stops = new ArrayList<StaticStopModel>();
		if (stopsCursor.moveToFirst()) {
			do {
				StaticStopModel stop = new StaticStopModel(stopsCursor.getInt(0), stopsCursor.getString(1), stopsCursor.getDouble(2), stopsCursor.getDouble(3));
				stops.add(stop);
			} while (stopsCursor.moveToNext());
		}
		ArrayList<StaticStopModel> stopsFinal = new ArrayList<StaticStopModel>();
		for(StaticStopModel stop : stops)
		{
			float[] result = new float[1];
			Location.distanceBetween(stop.coord.latitude, stop.coord.longitude, userLatitude, userLongitude, result);
			if(result[0] < DISTANCE_FILTER)
				stopsFinal.add(stop);
//			Log.i("DatabaseHandler", "name: " + stop.name + " distance: " + result[0]);
		}
		stops.clear();
		for(StaticStopModel stop: stopsFinal)
		{
			float[] result = new float[1];
			Location.distanceBetween(stop.coord.latitude, stop.coord.longitude, userLatitude, userLongitude, result);
		}
		return stopsFinal;
				
	}
	
	public int getStopsCount()
	{
		Cursor mCount= getReadableDatabase().rawQuery("select count(*) from " + TABLE_STOPS, null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		return count;
	}
	
	public List<StaticStopModel> getStops()
	{
		SQLiteDatabase db = getReadableDatabase();
		ArrayList<StaticStopModel> stops = new ArrayList<StaticStopModel>();
		Cursor cursor = db.rawQuery("select * from " + TABLE_STOPS + ";", null);
		if (cursor.moveToFirst()) {
			do {
				StaticStopModel stop = new StaticStopModel(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3));
				stops.add(stop);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return stops;
	}
	
	public void addStops(List<StaticStopModel> stops, Context ctx)
	{
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		for(int i = 0 ; i < stops.size(); i++){
			db.execSQL("INSERT INTO " + TABLE_STOPS + " VALUES(" + stops.get(i).id + ", \'" + stops.get(i).name + "\', " + stops.get(i).coord.latitude + ", " + stops.get(i).coord.longitude + ");");
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
