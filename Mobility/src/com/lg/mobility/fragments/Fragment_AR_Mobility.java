package com.lg.mobility.fragments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lg.mobility.R;
import com.lg.mobility.activities.ARActivity;
import com.lg.mobility.activities.MapActivity;
import com.lg.mobility.utilities.GeometryTouchListener;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ECOLOR_FORMAT;
import com.metaio.sdk.jni.IBillboardGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IGeometryVector;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.tools.io.AssetsManager;


import eu.liveandgov.ar.core.ARViewFragment;
import eu.liveandgov.ar.utilities.Entity;
import eu.liveandgov.ar.utilities.Graphic_Utils;


@SuppressLint("ValidFragment")
public class Fragment_AR_Mobility extends ARViewFragment {

	
	boolean isIgnited = false;
	boolean flag_contents_loaded = false;
	boolean DEBUG_FLAG = true;
	String TAG = getClass().getName();
	
	
	static Bitmap bm_icon_mobility = null; 




	//  Geometries LBS
	private GeometryTouchListener gListener;
	private IBillboardGroup billGroup; // Group of 2D billboards that helps to avoid overlapping
	private IGeometry[] mGeo2D;  // 2D billboards geometries
	private IGeometry[] jams;

	private IRadar mRadar;

//	int N_2d = 0;   List<Integer> indices2D = new ArrayList<Integer>(); // 2D indices   
//	private Class<?> classToCall = Activity_Posterior.class;

	//----------- Constructor -------------------------
	public Fragment_AR_Mobility(){}
	
	public Fragment_AR_Mobility(Bitmap bm_icon_mobility_in) {
		
		bm_icon_mobility = bm_icon_mobility_in;
		
		if (bm_icon_mobility!=null)
			bm_icon_mobility = Bitmap.createScaledBitmap(bm_icon_mobility, 56, 56, true);
		else 
			Log.e("FAM", "bm_icon_mobility is null");
	}
	
	

	/* On Create 	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			AssetsManager.extractAllAssets(getTheContext(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e){
			Log.e("ARActivity", "AssetsManager null pointer");
		}
		billGroup = metaioSDK.createBillboardGroup();
		
	}

	/** Load the contents and Ignite the AR */
//	public void ignite(){
//		deb("FAR_Mob","ignite()");
//		if (mGLSurfaceView==null){
//			Log.e("mGLSurfaceView","mGLSurfaceView is NULL");
//			Toast.makeText(getTheContext(), "mGLSurfaceView is NULL", Toast.LENGTH_LONG).show();
//		} else {
//			mGLSurfaceView.queueEvent(new Runnable() 
//			{
//				@Override
//				public void run() {
//					if (!flag_contents_loaded){
//						loadContents("Ignition");
//						flag_contents_loaded = true;
//
//						try {
//							metaioSDK.setTrackingConfiguration("GPS");
//							isIgnited = true;}
//						catch (Exception e){
//							deb("IGNITION", "ERROR");
//						}
//					}
//				}
//			});
//		}
//	}

	
	/* getContext */
	@Override
	protected Context getTheContext() {
		return ARActivity.ctx;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Create the view 
		deb("FAR_Mob","onCreateView");
		View v = super.onCreateView(inflater, container, savedInstanceState);
		return v; 
	}

	/* (non-Javadoc)
	 * @see eu.liveandgov.ar.core.ARViewFragment#onResume()
	 */
	@Override
	public void onResume() {
//		deb("FAR_Mob","onResume");
//		
//		
//		if ( !isIgnited ){
//			mGLSurfaceView.postDelayed( new Runnable(){
//				@Override
//				public void run(){
//					ignite();
//				}
//			}, 1000 );
//			isIgnited = true;
//		}
//		
		super.onResume();

	}

	
	/* DESTROY */
	@Override
	public void onDestroy() {
		deb("FAT","onDestroy");
		
		
		super.onDestroy();
	}

	
	//================ LoadContent ==========
	/**
	 *   Create the geometries and add to Radar
	 */
//	protected void loadContents(String caller) {
//
//
//		metaioSDK.setLLAObjectRenderingLimits(100 , 100000);
//		//----------- 2 D ------------------
//		mGeo2D = new IGeometry[entitiesLBS.size()];
//
//		LLACoordinate userLocLLA = mSensors.getLocation();
//		
//		Location userLoc = new Location("user");
//		userLoc.setLatitude(userLocLLA.getLatitude());
//		userLoc.setLongitude(userLocLLA.getLongitude());
//		
//		for (int i=0; i < entitiesLBS.size(); i++){
//			Entity e  = entitiesLBS.get(i);
//
//			//--- Calculate Distance -------
//			float dist = e.location.distanceTo(userLoc); 
//
//			String distSTR = "";
//			if (dist < 1000)
//				distSTR =  Integer.toString((int)dist) + "m";
//			else 
//				distSTR = Integer.toString((int) (dist/1000)) + "km";
//
//			
//			//---------- Create Billboard --------
//			
//			//Log.e("IsMainThread", " " + (Looper.myLooper() == Looper.getMainLooper()));
//			
//			mGeo2D[i] = metaioSDK.createGeometryFromImage(
//					Graphic_Utils.createBillboardTexture(e.title, distSTR, bm_icon_mobility, 
//							ARActivity.ctx, getResources(), R.drawable.poi_background), true);
//			
//			billGroup.addBillboard(mGeo2D[i]);
//			
//			//************************
//			mGeo2D[i].setName(e.id);
//			//************
//			
//			//bm.recycle();
//			//bm = null;
//		}
//
//		//------- Location update -------------
//		updateGeometriesLocation(mSensors.getLocation());
//
//		// ------ Create radar ------
//		if (mRadar!=null)
//			mRadar.delete();
//		
//		mRadar = metaioSDK.createRadar();
//
//		//---- radar back image --
//		Bitmap bmradar = BitmapFactory.decodeResource(getResources(), R.drawable.radar);
//		int bytes = bmradar.getWidth()*bmradar.getHeight()*4;
//		ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
//		bmradar.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
//		byte[] bmarray = buffer.array(); //Get the underlying array containing the data.
//		ImageStruct imageradar = new ImageStruct(bmarray,bmradar.getWidth(), bmradar.getHeight(), ECOLOR_FORMAT.ECF_A8R8G8B8, false, 0);
//		mRadar.setBackgroundTexture("radar", imageradar );
//		mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);
//
//		// add geometries to the radar
//		for (int i=0; i<entitiesLBS.size(); i++)
//			mRadar.add(mGeo2D[i]);
//
//		mRadar.setVisible(true);
//	}	

	/**
	 * Update the location of the geometries
	 * 
	 * @param location
	 */
//	private void updateGeometriesLocation(LLACoordinate location){
//
//		//----------------------- 2D ------------
//		for (int i=0; i< entitiesLBS.size(); i++){
//			Entity e = entitiesLBS.get(i);
//
//			LLACoordinate loc = new LLACoordinate(e.location.getLatitude(), 
//					e.location.getLongitude(),	0,	e.location.getAccuracy());
//
//			if (mGeo2D[i]!=null ){
//				mGeo2D[i].setTranslationLLA(loc);
//			} else {
//				Log.e("mGeo2D[i]" + " " + i, "is null");
//			}
//		}
//	}
	
//	public void updateJams(final List<Entity> jamEntities)
//	{
//		try{
//			mGLSurfaceView.queueEvent(new Runnable() {
//				
//				@Override
//				public void run() {
//					for(int i = 0; i < jams.length ; i++)
//					{
//						metaioSDK.unloadGeometry(jams[i]);
//					}
//					metaioSDK.setTrackingConfiguration("GPS");
//					metaioSDK.setLLAObjectRenderingLimits(100, 100000);
//					jams = new IGeometry[jamEntities.size()];
//					LLA Coordinate userLocLLA = mSensosrs.getLoca
//				}
//			};
//		}
//	}
	public void updateEntities(final List<Entity> entities)
	{
		try{
			mGLSurfaceView.queueEvent(new Runnable() 
			{
				@Override
				public void run() {
					IGeometryVector geometries = metaioSDK.getLoadedGeometries();
					Log.d("ARActivity", "geometries size = " + geometries.size());
					for(int i = 0; i < geometries.size(); i++)
					{
						metaioSDK.unloadGeometry(geometries.get(i));
					}
//		    		entitiesLBS = (ArrayList<Entity>) entities;
//		    		loadContents("");
					
					metaioSDK.setTrackingConfiguration("GPS");
					metaioSDK.setLLAObjectRenderingLimits(100 , 100000);
				//	----------- 2 D ------------------
					mGeo2D = new IGeometry[entities.size()];
					
					LLACoordinate userLocLLA = mSensors.getLocation();
					
					Location userLoc = new Location("user");
					userLoc.setLatitude(userLocLLA.getLatitude());
					userLoc.setLongitude(userLocLLA.getLongitude());
					
					for (int i=0; i < entities.size(); i++){
						Entity e  = entities.get(i);
						
					//	--- Calculate Distance -------
						float dist = e.location.distanceTo(userLoc); 
						
						String distSTR = "";
						if (dist < 1000)
							distSTR =  Integer.toString((int)dist) + "m";
						else 
							distSTR = Integer.toString((int) (dist/1000)) + "km";
						
						
					//	---------- Create Billboard --------
						
					//	Log.e("IsMainThread", " " + (Looper.myLooper() == Looper.getMainLooper()));
						
						mGeo2D[i] = metaioSDK.createGeometryFromImage(
								Graphic_Utils.createBillboardTexture(e.title, distSTR, bm_icon_mobility, 
										ARActivity.ctx, getResources(), R.drawable.poi_background), true);
						
						billGroup.addBillboard(mGeo2D[i]);
						
					//	************************
						mGeo2D[i].setName(e.id+"," +e.title);
					//	************
						
					//bm	.recycle();
						//	bm = null;
				}
					
					//------- Location update -------------
					for (int i=0; i< entities.size(); i++){
						Entity e = entities.get(i);
						
					LLACoordinate loc = new LLACoordinate(e.location.getLatitude(), 
							e.location.getLongitude(),	0,	e.location.getAccuracy());
					
					if (mGeo2D[i]!=null ){
							mGeo2D[i].setTranslationLLA(loc);
						} else {
							Log.e("mGeo2D[i]" + " " + i, "is null");
						}
					}
					
				// 	------ Create radar ------
//					if (mRadar!=null)
					//					mRadar.delete();
					if(mRadar == null)
					{
						mRadar = metaioSDK.createRadar();
				//	---- radar back image --
						Bitmap bmradar = BitmapFactory.decodeResource(getResources(), R.drawable.radar);
						int bytes = bmradar.getWidth()*bmradar.getHeight()*4;
						ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
						bmradar.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
						byte[] bmarray = buffer.array(); //Get the underlying array containing the data.
						ImageStruct imageradar = new ImageStruct(bmarray,bmradar.getWidth(), bmradar.getHeight(), ECOLOR_FORMAT.ECF_A8R8G8B8, false, 0);
						mRadar.setBackgroundTexture("radar", imageradar );
						mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);
						mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("ic_location_dot_blue.png"));
//						Bitmap objTexture = BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_dot_blue);
//						int 	bytes2 = objTexture.getWidth()*objTexture.getHeight()*4;
//						ByteBuffer buffer2 = ByteBuffer.allocate(bytes2);
//						objTexture.copyPixelsToBuffer(buffer2);
//						byte[] bmarray2 = buffer2.array();
//						ImageStruct objTextureStruct = new ImageStruct(bmarray2, objTexture.getWidth(), objTexture.getHeight(), ECOLOR_FORMAT.ECF_A8B8G8R8, false, 0);
//						mRadar.setObjectsDefaultTexture("objTexture", objTextureStruct);
					}	
					// 	add geometries to the radar
					for (int i=0; i<entities.size(); i++)
						mRadar.add(mGeo2D[i]);
					
					mRadar.setVisible(true);
				}
			});
		}
		catch(NullPointerException e)
		{
			//
		}
	}
		
	//	=============== onGeometryTouched ===============================
	/**
	 *  On Touch billboard.
	 *  @param geometry The geometry touched
	 */
	public void setTheListener(GeometryTouchListener l)
	{
		gListener = l;
	}
	protected void onGeometryTouched(final IGeometry geometry) {
		MetaioDebug.log("Geometry selected: "+geometry);
		if(gListener != null)
			gListener.onGeometryTouched(geometry.getName());
		//---- on Billboard or 3d model touched -> Go to details activity ----------
//		String id_STR = geometry.getName();
		
//		int id = Integer.parseInt(id_STR);

//		Intent mI = new Intent(MapActivity.ctx, classToCall);
//								mI.putExtra("idEntity", id);
//		startActivity(mI);
	}

	//============ deb ===============================
	public void deb(String mes, String mes2){
		if (DEBUG_FLAG)
			Log.e(TAG,  mes + ":" + mes2);
	}
}