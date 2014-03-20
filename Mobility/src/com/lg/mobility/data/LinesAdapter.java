package com.lg.mobility.data;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.lg.mobility.R;
import com.lg.mobility.R.drawable;
import com.lg.mobility.data.LineModel.Type;

public class LinesAdapter extends ArrayAdapter<LineModel> implements Filterable{
	Context ctx;
	int layoutResourceId;
	List<LineModel> data;
	List<LineModel> filteredData;
	LineFilter filter;
	Drawable[] drawables;
	
	static class LinesHolder
	{
		TextView lineID;
		TextView lineFullName;
		ImageView lineIcon;
	}
	public LinesAdapter(Context context, int textViewResourceId,
			List<LineModel> objects, Drawable[] transportDrawables) {
		super(context, textViewResourceId, objects);
		ctx = context;
		layoutResourceId = textViewResourceId;
		data = objects;
		data.add(0, new LineModel("?", "?", "Auto-detect", "?" , "?", Type.UKNOWN));
		filter = new LineFilter();
		filteredData = new ArrayList<LineModel>();
		drawables = transportDrawables;
		for(LineModel m : data)
			filteredData.add(m);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		LinesHolder holder;
		if(row == null){
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			 row = inflater.inflate(R.layout.line_list_item, null);
			 holder = new LinesHolder();
			 holder.lineFullName = (TextView) row.findViewById(R.id.line_fullname);
			 holder.lineID = (TextView) row.findViewById(R.id.line_number);
			 holder.lineIcon = (ImageView) row.findViewById(R.id.line_icon);
		     row.setTag(holder);			
		}
		else {
			holder = (LinesHolder) row.getTag();
		}
		try{
			LineModel curModel = filteredData.get(position);
			holder.lineID.setText(curModel.shortName);
			holder.lineFullName.setText(curModel.longName);
			switch(curModel.type)
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
		}
		catch (IndexOutOfBoundsException e) {
			holder.lineID.setText("");
			holder.lineFullName.setText("No results");
			holder.lineIcon.setBackgroundDrawable(null);
		}
		row.setEnabled(false);
		return row;
	}
	public LineModel getModelOnFilteredPosition(int position)
	{
		return filteredData.get(position);
	}
	@Override
	public Filter getFilter() {
		return filter;
	}
	@Override
	public int getCount() {
		return filteredData.size();
	}
	public class LineFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
        	//TODO:Faster filter algorithm
        	long s = System.currentTimeMillis();
            FilterResults Result = new FilterResults();
            // if constraint is empty return the original names
            if(constraint.length() == 0 ){
                Result.values = data;
                Result.count = data.size();
                return Result;
            }

            ArrayList<LineModel> Filtered_Names = new ArrayList<LineModel>();
            String filterString = constraint.toString().toLowerCase();
            String filterableString;
            Filtered_Names.add(data.get(0));
            for(int i = 1; i<data.size(); i++){
                filterableString = data.get(i).shortName + " " + data.get(i).longName;
                if(filterableString.toLowerCase().contains(filterString)){
                    Filtered_Names.add(data.get(i));
                }
//                if(filterableString2.toLowerCase().contains(filterString)){
//                	Filtered_Names.add(data.get(i));
//                }
            }
            Result.values = Filtered_Names;
            Result.count = Filtered_Names.size();
            Log.i("LinesAdapter", "filter time = " + (System.currentTimeMillis() - s));
            return Result;
        }

        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
            filteredData = (ArrayList<LineModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
