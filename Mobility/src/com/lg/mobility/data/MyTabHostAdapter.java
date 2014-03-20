package com.lg.mobility.data;

import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

public class MyTabHostAdapter implements TabContentFactory {

	private final Context _context;

	public MyTabHostAdapter(Context context) {
		_context = context;
	}

	public View createTabContent(String tag) {
		View v = new View(_context);
		v.setMinimumWidth(0);
		v.setMinimumHeight(0);
		return v;
	}
}