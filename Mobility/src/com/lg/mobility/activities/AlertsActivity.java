package com.lg.mobility.activities;
import java.util.Stack;

import nl.yucat.pushtestclient.activity.PushNotificationsActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.lg.mobility.R;
import com.lg.mobility.data.AlertsAdapter;
import com.lg.mobility.data.MyPagerAdapter;
import com.lg.mobility.data.MyTabHostAdapter;
import com.lg.mobility.fragments.AuthorityInfoFragment;

import eu.livegov.libraries.issuereporting.fragments.LocationFragment;
import eu.livegov.libraries.issuereporting.fragments.RemarkFragment;
import eu.livegov.libraries.issuereporting.fragments.ReportDetailFragment;
import eu.livegov.libraries.issuereporting.fragments.ReportOverviewFragment;
import eu.livegov.libraries.issuereporting.interfaces.OnReportClickListener;
import eu.livegov.libraries.issuereporting.utils.Functions;
 
public class AlertsActivity extends PushNotificationsActivity implements OnReportClickListener {
	
	public static int REMARKFRAGMENT = 0;
	public static int LOCATIONFRAGMENT = 1;
	public static int FEEDBACKFRAGMENT = 2;
 
	private Context ctx;
	private ActionBar mActionBar;
	private ListView alertView;
	private ListView issueView;
	private static AlertsAdapter alertsAdapter;
	private TabHost _tabhost;
	private ViewPager _pager;
	private MyPagerAdapter _pagerAdapter;
	
	private ReportOverviewFragment _reportOverviewFragment;
	private static int ovID;
	private ReportDetailFragment _reportDetailFragment;
	private RemarkFragment _remarkFragment;
	private LocationFragment _locationFragment;
	private AuthorityInfoFragment _authorityFragment;
	private int backCount;
	private int previousBackCount;
	private static Fragment currentReportDetailFragment;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//TODO:Fragments as tabs instead of lists
		Log.d("AlertsActivity", "onCreate()");
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.activity_alerts);
		Functions.setDeviceInfo(this);
		initialiseTabHost();
		initialisePager();
		if(_reportOverviewFragment == null)
		{
			_reportOverviewFragment = new ReportOverviewFragment();
		}
		if(_authorityFragment == null)
		{
			_authorityFragment = new AuthorityInfoFragment();
		}
		addFragment(_authorityFragment , "Authority Info", R.drawable.alert, 0);
		addFragment(_reportOverviewFragment, "User Info", R.drawable.alert, 1);
		if(savedInstanceState == null){
			backCount = 0;
			previousBackCount = 0;
		}
		else{
			backCount = savedInstanceState.getInt("BACK_COUNT", 0);
			previousBackCount = backCount;
		}
//		String[] spinnerObjects = new String[]{"All", "Mine"};
//		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerObjects);
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
//		    getActionBar().setListNavigationCallbacks(spinnerAdapter, navigationCallback);
//		}
//		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
//		tabHost.setup();
//		if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB)
//		{
//			tabHost.setOnTabChangedListener(new OnTabChangeListener() {
//				
//				@Override
//				public void onTabChanged(String tabId) {
//					Log.e("Tab", "tab changed : " + tabId);
//					if(tabId.equals("tab0")){
//						getActionBar().setTitle("Alerts");
//						getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//					}
//					else if(tabId.equals("tab1"))
//					{
//						getActionBar().setTitle("Issues");
//						getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//					}	
//				}
//			});
//		}
//		final TabWidget tabWidget = tabHost.getTabWidget();
//		tabWidget.removeAllViews();
//		final FrameLayout tabContent = tabHost.getTabContentView();
//		
//		alertView = (ListView) findViewById(R.id.alerts_list);
//		List<AlertsModel> dummyAlerts = new ArrayList<AlertsModel>();
////		dummyAlerts.add(new AlertsModel("Accident downtown", AlertType.ACCIDENT, "Reported on 12:01PM"));
//		alertsAdapter = new AlertsAdapter(this, R.layout.alert_list_item, new ArrayList<AlertsModel>());
//		alertView.setAdapter(alertsAdapter);
//		issueView = (ListView) findViewById(R.id.issues_list);
//		List<IssuesModel> dummyIssues = new ArrayList<IssuesModel>();
//		dummyIssues.add(new IssuesModel("User Messages", "user messages will be placed here"));
//		issueView.setAdapter(new IssuesAdapter(this, R.layout.issue_list_item, dummyIssues));
//		for (int index = 0; index < tabContent.getChildCount(); index++) {
//			tabContent.getChildAt(index).setVisibility(View.GONE);
//		}
//		TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("tab0");
//		tabSpec1.setIndicator("Authority Info");
//		tabSpec1.setContent(new TabContentFactory() {
//			
//			@Override
//			public View createTabContent(String tag) {
//				return alertView;
//			}
//		});
//		tabHost.addTab(tabSpec1);
//		
//		TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("tab1");
//		tabSpec2.setIndicator("User Info");
//		tabSpec2.setContent(new TabContentFactory() {
//			
//			@Override
//			public View createTabContent(String tag) {
//				return issueView;
//			}
//		});
//		tabHost.addTab(tabSpec2);
//		tabHost.setCurrentTab(0);
		// Get the original tab textviews and remove them from the viewgroup.
//		ListView[] originalListViews = new ListView[tabWidget.getTabCount()];
//		for (int index = 0; index < tabWidget.getTabCount(); index++) {
//			originalListViews[index] = (ListView) tabWidget.getChildTabViewAt(index);
//		}
//		tabWidget.removeAllViews();
//		
		// Ensure that all tab content childs are not visible at startup.
//		for (int index = 0; index < tabContent.getChildCount(); index++) {
//			tabContent.getChildAt(index).setVisibility(View.GONE);
//		}
//		
//		// Create the tabspec based on the textview childs in the xml file.
//		// Or create simple tabspec instances in any other way...
//		for (int index = 0; index < originalListViews.length; index++) {
//			final ListView tabWidgetListView = originalListViews[index];
//			final View tabContentView = tabContent.getChildAt(index);
//			TabSpec tabSpec = tabHost.newTabSpec((String) tabWidgetListView.getTag());
//			tabSpec.setContent(new TabContentFactory() {
//				@Override
//				public View createTabContent(String tag) {
//					return tabContentView;
//				}
//			});
//			tabHost.addTab(tabSpec);
//		}
		
//		tabHost.setCurrentTab(0);
	}
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if(_tabhost.getCurrentTab() == 0)
			menu.findItem(R.id.action_clear).setVisible(true);
		else
			menu.findItem(R.id.action_clear).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
	    {
	    case android.R.id.home: 
	    	Log.d("AlertsActivity", "Home pressed");
	    	onBackPressed();
	        break;
	    case R.id.action_clear:
	    	Log.d("AlertsActivity", "Clear pressed");
	    	boolean returnValue = super.onOptionsItemSelected(item);
	    	_authorityFragment.clearMessages();
	    	return returnValue;
	    default:
	    	Log.d("AlertsActivity", "Other pressed");
	    	return true;
		}
	    return true;
	}
	
	protected void addFragment(Fragment f, String title, int resourceID, int position) {
		TabSpec tabSpec = _tabhost.newTabSpec(title);
//		Drawable d = getResources().getDrawable(resourceID);
//		tabSpec.setIndicator("", d);
		tabSpec.setIndicator(title);
		tabSpec.setContent(new MyTabHostAdapter(this));
		_tabhost.addTab(tabSpec);
		_pagerAdapter.addFragment(f, position);
	}

	/**
	 * Initializes the tabhost and updates the viewpager when tab changed.
	 */
	private void initialiseTabHost() {
		_tabhost = (TabHost) findViewById(android.R.id.tabhost);
		_tabhost.setup();
		_tabhost.setOnTabChangedListener(new OnTabChangeListener() {

			@SuppressLint("NewApi")
			@Override
			public void onTabChanged(String tabId) {
				int pos = _tabhost.getCurrentTab();
				if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB)
				{
					switch(pos)
					{
					case 0:
						EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "tab_change", "authority_info_tab", null).build());
						getActionBar().setTitle("Authority Info");
						invalidateOptionsMenu();
						previousBackCount = backCount;
						backCount = 0;
						break;
					case 1:
						EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "tab_change", "user_info_tab", null).build());
						getActionBar().setTitle("User Info");
						invalidateOptionsMenu();
						backCount = previousBackCount;
					}
				}
				_pager.setCurrentItem(pos);
			}
		});
	}

	/**
	 * Initializes the viewpager and sets the current tab position on pager scrolled
	 */
	private void initialisePager() {
		_pager = (ViewPager) findViewById(R.id.viewpager);
		_pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		_pager.setAdapter(_pagerAdapter);
		_pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				int pos = _pager.getCurrentItem();
				_tabhost.setCurrentTab(pos);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	public void onOpenDetailViewClick(int fragment, Bundle args, boolean addToBackStack) {
		Log.i("AlertsActivity", "onOpenDetailViewClick");
		if((fragment == REMARKFRAGMENT) || fragment == FEEDBACKFRAGMENT) {
			if(_remarkFragment == null) {
				_remarkFragment = new RemarkFragment();
				_remarkFragment.setArguments(args);
			} else {
				Bundle currentArgs = _remarkFragment.getArguments();
				if(currentArgs == null) {
					_remarkFragment.setArguments(args);
				} else {
					currentArgs.putString("Text", args.getString("Text"));
					currentArgs.putBoolean("ReadOnly", args.getBoolean("ReadOnly", false));
					currentArgs.putString("title", args.getString("title"));
				}
			}
			backCount ++;
			_pagerAdapter.replaceFragment(_remarkFragment);
			EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "report_detail_remark", null).build());
//	        if(addToBackStack) {
//	        	ft.addToBackStack(null);
//	        }
//	        ft.commit();
		} else if(fragment == LOCATIONFRAGMENT) {
			if(_locationFragment == null) {
				_locationFragment = new LocationFragment();
				_locationFragment.setArguments(args);
			} else {
				Bundle currentArgs = _locationFragment.getArguments();
				if(currentArgs == null) {
					currentArgs = new Bundle();
				}
				currentArgs.putAll(args);
			}
			backCount ++;
			_pagerAdapter.replaceFragment(_locationFragment);
			EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "report_detail_location", null).build());
//	        if(addToBackStack) {
//	        	ft.addToBackStack(null);
//	        }
//	        ft.commit();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt("BACK_COUNT", backCount);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onReportClickListener(int report_id) {
		Log.i("AlertsActivity", "onReportClickListener");
		if(_reportDetailFragment == null) {
			_reportDetailFragment = new ReportDetailFragment();
			Bundle args = new Bundle();
			args.putInt("Report_id", report_id);
			_reportDetailFragment.setArguments(args);
		} else {
			Bundle args = _reportDetailFragment.getArguments();
			if(args == null) {
				args = new Bundle();
			}
			args.putInt("Report_id", report_id);
		}
		currentReportDetailFragment = _reportDetailFragment;
		backCount ++;
		_pagerAdapter.replaceFragment(_reportDetailFragment);
		EasyTracker.getInstance(ctx).send(MapBuilder.createEvent("ui_action", "button_press", "report_detail", null).build());
//		ft.
//		ft.add(android.R.id.tabcontent, _reportDetailFragment);
//        ft.replace(R.id.viewpager, _reportDetailFragment);
        
//        currentFragment = _reportDetailFragment;
//        ft.addToBackStack(null);
//        ft.commit();
        setTitle(getString(R.string.view_reportdetails));
	}
	
	@Override
	public void onBackPressed() {
		if(backCount == 2)
		{
			_pagerAdapter.replaceFragment(currentReportDetailFragment);
			backCount --;
		}
		else if(backCount == 1)
		{
			if(_reportOverviewFragment == null)
			{
				_pagerAdapter.replaceFragment(new ReportOverviewFragment());
			}
			else
			{
				_pagerAdapter.replaceFragment(_reportOverviewFragment);
			}
			backCount --;
		}
		else
		{
			super.onBackPressed();
		}
		return;
	}

	@Override
	protected void onDestroy() {
		Log.d("AlertsActivity", "onDestroy()");
		super.onDestroy();
	}
	@Override
	protected ListView getListView() {
		//Possible bug
		if(_authorityFragment == null)
		{
			Log.w("AlertsActivity", "authorityFragment is null");
		}
		if(_authorityFragment.rootView == null)
		{
			Log.w("AlertsActivity", "authorityFragment View is null");
		}
		ListView lv = (ListView) _authorityFragment.rootView.findViewById(R.id.alerts_list);
		if(lv == null) Log.i("AlertsActivity", "lv = null");
		return (ListView) _authorityFragment.rootView.findViewById(R.id.alerts_list);
	}
}