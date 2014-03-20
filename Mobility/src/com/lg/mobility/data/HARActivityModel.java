package com.lg.mobility.data;

import android.graphics.drawable.Drawable;

public class HARActivityModel {
	public static Drawable[] drawables;
	public enum Type
	{
		RUNNING, WALKING, SITTING, STANDING, ONTABLE, UNKNOWN;
		
		Drawable getIcon()
		{
			switch(this)
			{
			case UNKNOWN:
				return drawables[0];
			case RUNNING:
				return drawables[1];
			case WALKING:
				return drawables[2];
			case SITTING:
				return drawables[3];
			case STANDING:
				return drawables[4];
			case ONTABLE:
				return drawables[5];
			default:
				return drawables[5];
			}
		}
	}
//	* running 14872
//	* sitting 4860
//	* standing 150925
//	* walking 8650
//	* on_table 20768
	public String name;
	public Type type;
	HARActivityModel(String _name, Type _type)
	{
		name = _name;
		type = _type;
	}
	
	static void setDrawables(Drawable[] _drawables)
	{
		HARActivityModel.drawables = _drawables;
	}
	static HARActivityModel[] getActivities()
	{
		HARActivityModel[] activities = new HARActivityModel[6];
		activities[0] = new HARActivityModel("Auto-Detect", Type.UNKNOWN);
		activities[1] = new HARActivityModel("Running", Type.RUNNING);
		activities[2] = new HARActivityModel("Walking", Type.WALKING);
		activities[3] = new HARActivityModel("Sitting", Type.SITTING);
		activities[4] = new HARActivityModel("Standing", Type.STANDING);
		activities[5] = new HARActivityModel("On table", Type.ONTABLE);
		return activities;
	}
}