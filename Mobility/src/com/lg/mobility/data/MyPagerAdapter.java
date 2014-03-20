package com.lg.mobility.data;

import java.util.ArrayList;

import com.google.android.gms.maps.SupportMapFragment;
import com.lg.mobility.fragments.AuthorityInfoFragment;

import eu.livegov.libraries.issuereporting.fragments.RemarkFragment;
import eu.livegov.libraries.issuereporting.fragments.ReportDetailFragment;
import eu.livegov.libraries.issuereporting.fragments.ReportOverviewFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class MyPagerAdapter extends FragmentStatePagerAdapter{
	private ArrayList<Fragment> _fragments;
	public FragmentManager fManager;
	public Fragment fragment1, fragment2;
	private static final int FRAGMENT_COUNT = 2;
	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
		fManager = fm;
		_fragments = new ArrayList<Fragment>();
	}

	public void removeAll() {
		_fragments.clear();
		_fragments = new ArrayList<Fragment>();
	}

	@Override
	public Fragment getItem(int position) {
		if(position == 0)
			return fragment1;
		else if(position == 1)
			return fragment2;
		else return null;
	}

	@Override
	public int getCount() {
		return FRAGMENT_COUNT;
	}
	
	public void addFragment(Fragment f, int position)
	{
		if(position == 0){
			fragment1 = f;
			notifyDataSetChanged();
		}
		else if(position == 1){
			fragment2 =f;
			notifyDataSetChanged();
		}
		else return;
	}
//	public void addFragment(Fragment f) {
//		_fragments.add(f);
//		notifyDataSetChanged();
//	}
	public void replaceFragment(Fragment f)
	{
		fManager.beginTransaction().remove(fragment2).commit();
		fragment2 = f;
		
		notifyDataSetChanged();
	}
	@Override
	public int getItemPosition(Object object) {
		if(object instanceof AuthorityInfoFragment)
			Log.i("FragmentAdapter", "object is instance of Authority");
		else if(object instanceof ReportOverviewFragment)
			Log.i("FragmentAdapter", "object is instance of Report Overview");
		else if(object instanceof ReportDetailFragment)
			Log.i("FragmentAdapter", "object is instance of Report Detail");
		else if (object instanceof RemarkFragment)
			Log.i("FragmentAdapter", "object is instance of Remark");
		else
			Log.i("FragmentAdapter", "other");
		if(object instanceof AuthorityInfoFragment)
			return POSITION_UNCHANGED;
		return POSITION_NONE;
	}
	
}