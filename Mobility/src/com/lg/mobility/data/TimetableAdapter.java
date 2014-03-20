package com.lg.mobility.data;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.lg.mobility.R;

public class TimetableAdapter extends ArrayAdapter<TimetableModel> implements Filterable{
	Context ctx;
	int layoutResourceId;
	List<TimetableModel> data;
	Drawable[] drawables;

	
	static class TimetablesHolder
	{
		TextView lineID;
		ImageView lineIcon;
		TextView direction;
		TextView time;
	}
	public TimetableAdapter(Context context, int textViewResourceId,
			List<TimetableModel> objects, Drawable[] transportDrawables) {
		super(context, textViewResourceId, objects);
		ctx = context;
		layoutResourceId = textViewResourceId;
		data = objects;
		drawables = transportDrawables;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		TimetablesHolder holder;
		if(row == null){
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			 row = inflater.inflate(R.layout.timetable_list_item, null);
			 holder = new TimetablesHolder();
			 holder.direction= (TextView) row.findViewById(R.id.timetable_direction_name);
			 holder.time = (TextView) row.findViewById(R.id.timetable_departure_minutes);
			 holder.lineID = (TextView) row.findViewById(R.id.timetable_line_number);
			 holder.lineIcon = (ImageView) row.findViewById(R.id.timetable_line_icon);
		     row.setTag(holder);			
		}
		else {
			holder = (TimetablesHolder) row.getTag();
		}
		TimetableModel curModel = data.get(position);
		holder.lineID.setText(curModel.line.shortName);
		if(curModel.line.direction == 1)
		{
			holder.direction.setText(curModel.line.stopName);
		}
		else
		{
			holder.direction.setText(curModel.line.startName);
		}
		holder.time.setText(curModel.departureTime);
		switch(curModel.line.type)
		{
			case BUS:
				holder.lineIcon.setBackgroundDrawable(drawables[0]);
				break;
			case FERRY:
				holder.lineIcon.setBackgroundDrawable(drawables[1]);
				break;
			case TRAM:
				holder.lineIcon.setBackgroundDrawable(drawables[2]);
				break;
			case RAIL:
				holder.lineIcon.setBackgroundDrawable(drawables[4]);
				break;
			default :
				holder.lineIcon.setBackgroundDrawable(drawables[3]);
				break;
		}
		row.setEnabled(false);
		return row;
	}
	
	@Override
	public int getCount() {
		return data.size();
	}
}
