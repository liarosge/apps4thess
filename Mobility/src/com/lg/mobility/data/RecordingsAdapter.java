package com.lg.mobility.data;

import java.util.List;

import com.lg.mobility.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecordingsAdapter extends ArrayAdapter<RecordingsModel> {
	
	Context ctx;
	int layoutResourceId;
	List<RecordingsModel> data;
	List<Drawable> transportDrawables;
	
	static class RecordingHolder
	{
		ImageView transportationIcon;
		TextView recordingTitle;
		TextView recordingDuration;
		TextView recordingDistance;
		TextView recordingSpeed;
		TextView recordingDate;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return true;
	}
	public RecordingsAdapter(Context context, int textViewResourceId,
			List<RecordingsModel> objects, List<Drawable> _transportDrawables) {
		super(context, textViewResourceId, objects);
		ctx = context;
		layoutResourceId = textViewResourceId;
		data = objects;
		transportDrawables = _transportDrawables;
	}
	public List<RecordingsModel> getData() {
		return data;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordingHolder holder;
		if(row == null){
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			 row = inflater.inflate(R.layout.recordings_list_item, null);
			 holder = new RecordingHolder();
		     holder.transportationIcon = (ImageView) row.findViewById(R.id.transport_icon);
		     holder.recordingTitle = (TextView) row.findViewById(R.id.recording_title);
		     holder.recordingDuration = (TextView) row.findViewById(R.id.recording_duration);
		     holder.recordingDistance = (TextView) row.findViewById(R.id.recording_distance);
		     holder.recordingSpeed = (TextView) row.findViewById(R.id.recording_speed);
		     holder.recordingDate = (TextView) row.findViewById(R.id.recording_date);
		     row.setTag(holder);			
		}
		else {
			holder = (RecordingHolder) row.getTag();
		}
		RecordingsModel curModel = data.get(position);
		
		switch(curModel.transportationType)
		{
		case RUNNING:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(0));
			break;
		case WALKING:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(1));
			break;
		case SITTING:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(2));
			break;
		case STANDING:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(3));
			break;
		case ONTABLE:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(4));
			break;
		case UNKNOWN:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(5));
			break;
		default:
			holder.transportationIcon.setImageDrawable(transportDrawables.get(5));
			break;
		}
		holder.recordingTitle.setText(curModel.title);
		holder.recordingDuration.setText(curModel.duration);
		holder.recordingDistance.setText(curModel.distance);
		holder.recordingSpeed.setText(curModel.speed);
		holder.recordingDate.setText(curModel.date);
		return row;
		
	}

}
