package com.lg.mobility.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.lg.mobility.data.MapDrawing.DrawingType;
import com.lg.mobility.utilities.PolyLineDecoder;
import com.lg.mobility.utilities.TrafficJamUpdate;

public class TrafficJamModel {
	public VehicleModel[] slowVehicles;
	public StopModel[] vehicleStops;
	public ArrayList<List<LatLng>> partialRoutes;
	public double[] cumulativeDelays;
	public boolean isJam;
	public double id, slowVehiclesInJamCount;
	
	public TrafficJamModel(double _id, boolean _isJam, 
			double[] _cumulativeDelays, VehicleModel[] _slowVehicles,
			StopModel[] _vehicleStops, double _slowVehiclesInJamCount, ArrayList<List<LatLng>> _partialRoutes)
	{
		id = _id;
		isJam = _isJam;
		cumulativeDelays = _cumulativeDelays;
		vehicleStops = _vehicleStops;
		slowVehicles = _slowVehicles;
		partialRoutes = _partialRoutes;
	}
	
	public static TrafficJamModel fromLinkedTreeMap(LinkedTreeMap<String, Object> treeMap)
	{
		Boolean _isJam = (Boolean) treeMap.get("IsJam");
		Double _id = (Double) treeMap.get("Id");
		Double _slowVehiclesInJamCount = (Double) treeMap.get("SlowVehiclesInJamCount");
		List<LinkedTreeMap<String,Object>> slowVehiclesAll = (List<LinkedTreeMap<String, Object>>) treeMap.get("SlowVehicles");
		VehicleModel[] _slowVehicles = new VehicleModel[slowVehiclesAll.size()];
		StopModel[] _vehicleStops = new StopModel[slowVehiclesAll.size()];
		double[] _cumulativeDelays = new double[slowVehiclesAll.size()];
		ArrayList<List<LatLng>> _partialRoutes = new ArrayList<List<LatLng>>(slowVehiclesAll.size());
		for(int i = 0 ; i < slowVehiclesAll.size(); i++)
		{
			_slowVehicles[i] = VehicleModel.fromLinkedTreeMap((LinkedTreeMap<String, Object>) slowVehiclesAll.get(i).get("Vehicle"));
//			_vehicleStops[i] = StopModel.fromLinkedTreeMap((LinkedTreeMap<String, Object>) slowVehiclesAll.get(i).get("Stop"));
			_vehicleStops[i] = StopModel.fromLinkedTreeMaps((LinkedTreeMap<String,Object>) slowVehiclesAll.get(i).get("Stop"),
					(LinkedTreeMap<String,Object>) slowVehiclesAll.get(i).get("PreviousStop"), 
							(LinkedTreeMap<String,Object>) slowVehiclesAll.get(i).get("NextStop"));
			_vehicleStops[i].lineID = _slowVehicles[i].lineID;
//			_vehicleStops[i].setNearestStops();
			_partialRoutes.add(PolyLineDecoder.decodeWithLimits(_vehicleStops[i].nextStop.latitude,
					_vehicleStops[i].nextStop.longitude, _vehicleStops[i].previousStop.latitude,
					_vehicleStops[i].previousStop.longitude, _slowVehicles[i].route.hexEncoded));
			Log.e("tjm" , "setNearestStops");
			_cumulativeDelays[i] = ((Double) slowVehiclesAll.get(i).get("CumulativeDelay")).doubleValue();
		}
		return new TrafficJamModel(_id.doubleValue(), _isJam.booleanValue(),
				_cumulativeDelays, _slowVehicles, _vehicleStops, _slowVehiclesInJamCount.doubleValue(), _partialRoutes);
	}
	
	public static TrafficJamModel fromLinkedTreeMap(LinkedTreeMap<String, Object> treeMap, TrafficJamUpdate update)
	{
		Boolean _isJam = (Boolean) treeMap.get("IsJam");
		Double _id = (Double) treeMap.get("Id");
		Double _slowVehiclesInJamCount = (Double) treeMap.get("SlowVehiclesInJamCount");
		List<LinkedTreeMap<String,Object>> slowVehiclesAll = (List<LinkedTreeMap<String, Object>>) treeMap.get("SlowVehicles");
		VehicleModel[] _slowVehicles = new VehicleModel[slowVehiclesAll.size()];
		StopModel[] _vehicleStops = new StopModel[slowVehiclesAll.size()];
		double[] _cumulativeDelays = new double[slowVehiclesAll.size()];
		ArrayList<List<LatLng>> _partialRoutes = new ArrayList<List<LatLng>>(slowVehiclesAll.size());
		for(int i = 0 ; i < slowVehiclesAll.size(); i++)
		{
			_slowVehicles[i] = VehicleModel.fromLinkedTreeMap((LinkedTreeMap<String, Object>) slowVehiclesAll.get(i).get("Vehicle"));
			update.drawOnMap(new MapDrawing(_slowVehicles[i].decodedPoly, MapDrawing.DrawingType.ROUTE_LINE));
//			_vehicleStops[i] = StopModel.fromLinkedTreeMap((LinkedTreeMap<String, Object>) slowVehiclesAll.get(i).get("Stop"));
			_vehicleStops[i] = StopModel.fromLinkedTreeMaps((LinkedTreeMap<String,Object>) slowVehiclesAll.get(i).get("Stop"),
					(LinkedTreeMap<String,Object>) slowVehiclesAll.get(i).get("PreviousStop"), 
							(LinkedTreeMap<String,Object>) slowVehiclesAll.get(i).get("NextStop"));
			_vehicleStops[i].lineID = _slowVehicles[i].lineID;
//			_vehicleStops[i].setNearestStops();
			try{
				List<LatLng> poly = PolyLineDecoder.decodeWithLimits(_vehicleStops[i].nextStop.latitude,
						_vehicleStops[i].nextStop.longitude, _vehicleStops[i].previousStop.latitude,
						_vehicleStops[i].previousStop.longitude, _slowVehicles[i].route.hexEncoded);
				update.drawOnMap(new MapDrawing(poly, MapDrawing.DrawingType.JAM_LINE));
				_partialRoutes.add(poly);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
//			_partialRoutes.add(PolyLineDecoder.decodeWithLimits(_vehicleStops[i].nextStop.latitude,
//					_vehicleStops[i].nextStop.longitude, _vehicleStops[i].previousStop.latitude,
//					_vehicleStops[i].previousStop.longitude, _slowVehicles[i].encodedRoute));
//			Log.e("tjm" , "setNearestStops");
			_cumulativeDelays[i] = ((Double) slowVehiclesAll.get(i).get("CumulativeDelay")).doubleValue();
		}
		return new TrafficJamModel(_id.doubleValue(), _isJam.booleanValue(),
				_cumulativeDelays, _slowVehicles, _vehicleStops, _slowVehiclesInJamCount.doubleValue(), _partialRoutes);
		
	}
	
	public void drawOnMap(TrafficJamUpdate update)
	{
		for(int i = 0; i < slowVehicles.length; i++)
		{
			update.drawOnMap(new MapDrawing(slowVehicles[i].decodedPoly, MapDrawing.DrawingType.ROUTE_LINE));
			update.drawOnMap(new MapDrawing(partialRoutes.get(i), MapDrawing.DrawingType.JAM_LINE));
		}
	}
	
	public static StopModel searchJams(TrafficJamModel[] jams, String lineID, int direction, MutableInt jammedStopIndex, MutableDouble jammedStopDelay, String[] jammedStopTitle)
	{
		for(TrafficJamModel jam : jams)
		{
			for(int i = 0 ; i < jam.slowVehicles.length; i++)
			{
				if(jam.slowVehicles[i].lineID.equals(lineID))
				{
					if(jam.slowVehicles[i].direction == direction){
						jammedStopIndex.setValue(i);
						jammedStopDelay.setValue(jam.cumulativeDelays[i]);
						jammedStopTitle[0] = jam.vehicleStops[i].name;
						return jam.vehicleStops[i];
					}
				}
			}
		}
		return null;
	}
}
