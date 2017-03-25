package com.sapuseven.untis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.sapuseven.untis.BuildConfig;
import com.sapuseven.untis.R;
import com.sapuseven.untis.adapter.AdapterItemGridView;
import com.sapuseven.untis.adapter.AdapterTimetable;
import com.sapuseven.untis.adapter.AdapterTimetableHeader;
import com.sapuseven.untis.fragment.FragmentDatePicker;
import com.sapuseven.untis.notification.StartupReceiver;
import com.sapuseven.untis.utils.AutoUpdater;
import com.sapuseven.untis.utils.ElementName;
import com.sapuseven.untis.utils.ListManager;
import com.sapuseven.untis.utils.SessionInfo;
import com.sapuseven.untis.utils.TimegridUnitManager;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static com.sapuseven.untis.utils.ElementName.CLASS;
import static com.sapuseven.untis.utils.ElementName.ROOM;
import static com.sapuseven.untis.utils.ElementName.TEACHER;
import static com.sapuseven.untis.utils.StreamUtils.readStream;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	public SwipeRefreshLayout swipeRefresh;
	public SessionInfo sessionInfo;
	public int currentViewPos = 50;
	private ViewPager pagerHeader;
	private ViewPager pagerTable;
	private ListManager listManager;
	private AdapterTimetableHeader pagerHeaderAdapter;
	private AdapterTimetable pagerTableAdapter;
	private AlertDialog dialog;
	private Calendar lastCalendar;
	private JSONObject userDataList;
	private float scale;
	private long lastBackPress;

	public static void setupTheme(Context context, boolean actionBar) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (actionBar)
			switch (prefs.getString("preference_theme", "default")) {
				case "untis":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeUntis_ActionBar);
					else
						context.setTheme(R.style.AppTheme_ThemeUntis_ActionBar);
					break;
				case "blue":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeBlue_ActionBar);
					else
						context.setTheme(R.style.AppTheme_ThemeBlue_ActionBar);
					break;
				case "green":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeGreen_ActionBar);
					else
						context.setTheme(R.style.AppTheme_ThemeGreen_ActionBar);
					break;
				case "pink":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemePink_ActionBar);
					else
						context.setTheme(R.style.AppTheme_ThemePink_ActionBar);
					break;
				case "cyan":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeCyan_ActionBar);
					else
						context.setTheme(R.style.AppTheme_ThemeCyan_ActionBar);
					break;
				default:
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ActionBar);
					else
						context.setTheme(R.style.AppTheme_ActionBar);
			}
		else
			switch (prefs.getString("preference_theme", "default")) {
				case "untis":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeUntis);
					else
						context.setTheme(R.style.AppTheme_ThemeUntis);
					break;
				case "blue":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeBlue);
					else
						context.setTheme(R.style.AppTheme_ThemeBlue);
					break;
				case "green":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeGreen);
					else
						context.setTheme(R.style.AppTheme_ThemeGreen);
					break;
				case "pink":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemePink);
					else
						context.setTheme(R.style.AppTheme_ThemePink);
					break;
				case "cyan":
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark_ThemeCyan);
					else
						context.setTheme(R.style.AppTheme_ThemeCyan);
					break;
				default:
					if (prefs.getBoolean("preference_dark_theme", false))
						context.setTheme(R.style.AppThemeDark);
					else
						context.setTheme(R.style.AppTheme);
			}
	}

	public static void setupBackground(Activity context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (sharedPrefs.getBoolean("preference_dark_theme_amoled", false) && sharedPrefs.getBoolean("preference_dark_theme", false))
			context.getWindow().getDecorView().setBackgroundColor(Color.BLACK);
	}

	public static void restartApplication(Context context) {
		Intent i = new Intent(context, ActivityMain.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setupTheme(this, false);
		super.onCreate(savedInstanceState);
		final Fabric fabric = new Fabric.Builder(this)
				.kits(new Crashlytics.Builder()
						.core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
						.build())
				.debuggable(true)
				.build();
		Fabric.with(fabric);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!checkLoginState()) {
			Intent i = new Intent(this, ActivityLogin.class);
			startActivity(i);
			finish();
		} else {
			Intent intent = new Intent(this, StartupReceiver.class);
			sendBroadcast(intent);

			setContentView(R.layout.activity_main);
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);

			DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
			ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
					this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			drawer.addDrawerListener(toggle);
			toggle.syncState();
			listManager = new ListManager(getApplicationContext());
			dialog = new AlertDialog.Builder(this).create();
			scale = getResources().getDisplayMetrics().density;
			try {
				userDataList = new JSONObject(listManager.readList("userData", false));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
			swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					listManager.delete(sessionInfo.getElemType() + "-" + sessionInfo.getElemId() + "-" + getStartDateFromWeek() + "-" + addDaysToInt(getStartDateFromWeek(), 4), true);
					refresh();
				}
			});

			Calendar c = Calendar.getInstance();
			if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
					&& c.getFirstDayOfWeek() == Calendar.MONDAY))
				currentViewPos++;

			pagerHeader = (ViewPager) findViewById(R.id.viewpagerHeader);
			pagerHeaderAdapter = new AdapterTimetableHeader(getSupportFragmentManager());
			pagerHeader.setAdapter(pagerHeaderAdapter);

			pagerTable = (ViewPager) findViewById(R.id.viewpagerTimegrid);
			pagerTableAdapter = new AdapterTimetable(getSupportFragmentManager());
			pagerTable.setAdapter(pagerTableAdapter);

			pagerTable.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_MOVE && !swipeRefresh.isRefreshing())
						swipeRefresh.setEnabled(false);
					else if (!swipeRefresh.isRefreshing())
						swipeRefresh.setEnabled(true);
					return false;
				}
			});

			pagerHeader.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				private int scrollState = ViewPager.SCROLL_STATE_IDLE;

				@Override
				public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
					if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
						return;
					}
					pagerTable.scrollTo(pagerHeader.getScrollX(), pagerTable.getScrollY());
				}

				@Override
				public void onPageSelected(final int position) {
					currentViewPos = position;
				}

				@Override
				public void onPageScrollStateChanged(final int state) {
					scrollState = state;
					if (state == ViewPager.SCROLL_STATE_IDLE) {
						pagerTable.setCurrentItem(pagerHeader.getCurrentItem(), false);
					}
				}
			});

			pagerTable.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				private int scrollState = ViewPager.SCROLL_STATE_IDLE;

				@Override
				public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
					if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
						return;
					}
					pagerHeader.scrollTo(pagerTable.getScrollX(), pagerHeader.getScrollY());
				}

				@Override
				public void onPageSelected(final int position) {
					currentViewPos = position;
				}

				@Override
				public void onPageScrollStateChanged(final int state) {
					scrollState = state;
					if (state == ViewPager.SCROLL_STATE_IDLE) {
						pagerHeader.setCurrentItem(pagerTable.getCurrentItem(), false);
					}
				}
			});

			lastCalendar = Calendar.getInstance();

			ImageView ivSelectDate = (ImageView) findViewById(R.id.ivSelectDate);
			ivSelectDate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					DialogFragment fragment = new FragmentDatePicker();
					Bundle args = new Bundle();
					args.putInt("year", lastCalendar.get(Calendar.YEAR));
					args.putInt("month", lastCalendar.get(Calendar.MONTH));
					args.putInt("day", lastCalendar.get(Calendar.DAY_OF_MONTH));
					fragment.setArguments(args);
					fragment.show(getSupportFragmentManager(), "datePicker");
				}
			});

			try {
				TimegridUnitManager unitManager = new TimegridUnitManager();
				unitManager.setList(userDataList.getJSONObject("masterData").getJSONObject("timeGrid").getJSONArray("days"));
				ArrayList<TimegridUnitManager.UnitData> units = unitManager.getUnits();

				for (int i = 0; i < units.size(); i++)
					addHour(units.get(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
			navigationView.setNavigationItemSelectedListener(this);
			navigationView.setCheckedItem(R.id.nav_show_personal);

			((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_drawer_header_line1)).setText(userDataList.optJSONObject("userData").optString("displayName", getString(R.string.app_name)));
			((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_drawer_header_line2)).setText(userDataList.optJSONObject("userData").optString("schoolName", getString(R.string.contact_email)));

			pagerHeader.setCurrentItem(currentViewPos);
			pagerTable.setCurrentItem(currentViewPos);

			checkVersion();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				new CheckForNewFeatures().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			final int oldVersion = prefs.getInt("last_version", 0);
			if (oldVersion < BuildConfig.VERSION_CODE) {
				/*
				RE-ENABLE FOR A 'NEW VERSION'-DIALOG

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.new_version_message))
						.setCancelable(false)
						.setNeutralButton(R.string.view_changelog, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
								new DisplayChangelog(ActivityMain.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, oldVersion);
							}
						})
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
				AlertDialog alertDialog = builder.create();
				alertDialog.show();

				final Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				button.setEnabled(false);

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						button.setEnabled(true);
					}
				}, 3000);*/

				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("last_version", BuildConfig.VERSION_CODE);
				editor.apply();
			}
			setupBackgroundColor();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (sharedPrefs.getBoolean("restart", false)) {
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putBoolean("restart", false);
			editor.apply();
			restartApplication(this);
		}
	}

	public void goTo(Calendar c1) {
		c1.set(Calendar.HOUR_OF_DAY, 0);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);
		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.DAY_OF_WEEK, c1.get(Calendar.DAY_OF_WEEK));
		c2.set(Calendar.HOUR_OF_DAY, 0);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);
		currentViewPos = (int) (50L + (c1.getTimeInMillis() - c2.getTimeInMillis()) / (7 * 24 * 60 * 60 * 1000));
		pagerHeader.setCurrentItem(currentViewPos);
		pagerTable.setCurrentItem(currentViewPos);
	}

	public void refresh() {
		if (dialog.isShowing())
			dialog.dismiss();
		pagerTable.setAdapter(pagerTableAdapter);
		pagerHeader.setAdapter(pagerHeaderAdapter);
		pagerTable.setCurrentItem(currentViewPos);
		pagerHeader.setCurrentItem(currentViewPos);

		setupBackgroundColor();
	}

	private void setupBackgroundColor() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (sharedPrefs.getBoolean("preference_dark_theme_amoled", false) && sharedPrefs.getBoolean("preference_dark_theme", false)) {
			findViewById(R.id.input_date).setBackgroundColor(Color.BLACK);
			findViewById(R.id.hour_view_sidebar).setBackgroundColor(Color.BLACK);
		}
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			if (sessionInfo != null && sessionInfo.getElemId() != userDataList.optJSONObject("userData").optInt("elemId", -1)) {
				setTarget(
						userDataList.optJSONObject("userData").optInt("elemId", -1),
						SessionInfo.getElemTypeId(userDataList.optJSONObject("userData").optString("elemType", "")),
						userDataList.optJSONObject("userData").optString("displayName", "BetterUntis"));
			} else {
				if (System.currentTimeMillis() - 2000 > lastBackPress) {
					Snackbar.make(findViewById(R.id.content_main), R.string.snackbar_press_back_double, 2000).show();
					lastBackPress = System.currentTimeMillis();
				} else {
					super.onBackPressed();
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.action_settings:
				Intent i = new Intent(ActivityMain.this, ActivityPreferences.class);
				startActivity(i);
				break;
			case R.id.action_refresh:
				refresh();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		// Handle navigation view item clicks here.
		switch (sessionInfo.getElemType()) {
			case "CLASS":
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_classes);
				break;
			case "TEACHER":
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_teachers);
				break;
			case "ROOM":
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_rooms);
				break;
			default:
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_personal);
		}

		switch (item.getItemId()) {
			case R.id.nav_show_personal:
				setTarget(
						userDataList.optJSONObject("userData").optInt("elemId", -1),
						SessionInfo.getElemTypeId(userDataList.optJSONObject("userData").optString("elemType", "")),
						userDataList.optJSONObject("userData").optString("displayName", "BetterUntis"));
				break;
			case R.id.nav_show_classes:
				//noinspection SpellCheckingInspection
				showItemList(CLASS, R.string.hint_search_classes, R.string.title_class, "klassen");
				break;
			case R.id.nav_show_teachers:
				showItemList(TEACHER, R.string.hint_search_teachers, -10, "teachers");
				break;
			case R.id.nav_show_rooms:
				showItemList(ROOM, R.string.hint_search_rooms, R.string.title_room, "rooms");
				break;
			case R.id.nav_settings:
				Intent i1 = new Intent(ActivityMain.this, ActivityPreferences.class);
				startActivity(i1);
				break;
			case R.id.nav_suggested_features:
				Intent i2 = new Intent(ActivityMain.this, ActivityFeatures.class);
				startActivity(i2);
				break;
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void showItemList(final int elementType, @StringRes int searchFieldHint, final int targetPageTitle, String masterDataField) {
		DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				if (getSupportActionBar() != null)
					getSupportActionBar().setTitle(sessionInfo.getDisplayName());
				switch (sessionInfo.getElemType()) {
					case "CLASS":
						((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_classes);
						break;
					case "TEACHER":
						((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_teachers);
						break;
					case "ROOM":
						((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_rooms);
						break;
					default:
						((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_personal);
				}
			}
		};

		try {
			final ElementName elementName = new ElementName(elementType).setUserDataList(userDataList);
			LinearLayout content = new LinearLayout(this);
			content.setOrientation(LinearLayout.VERTICAL);

			final List<String> list = new ArrayList<>();
			JSONArray roomList = userDataList.optJSONObject("masterData").optJSONArray(masterDataField);
			for (int i = 0; i < roomList.length(); i++)
				list.add(roomList.getJSONObject(i).getString("name"));
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return s1.compareToIgnoreCase(s2);
				}
			});

			final AdapterItemGridView adapter = new AdapterItemGridView(this, list);
			TextInputLayout titleContainer = new TextInputLayout(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(dp2px(12), dp2px(12), dp2px(12), 0);
			titleContainer.setLayoutParams(params);

			GridView gridView = new GridView(this);
			gridView.setAdapter(adapter);
			gridView.setNumColumns(3);
			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (targetPageTitle == -10)
						setTarget((int) elementName.findFieldByValue("name", list.get(position), "id"),
								elementType,
								elementName.findFieldByValue("name", list.get(position), "firstName") + " " + elementName.findFieldByValue("name", list.get(position), "lastName"));
					else
						setTarget((Integer) elementName.findFieldByValue("name", list.get(position), "id"),
								elementType,
								getString(targetPageTitle, list.get(position)));
				}
			});
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			TextInputEditText searchField = new TextInputEditText(this);
			searchField.setHint(searchFieldHint);
			searchField.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					adapter.getFilter().filter(s.toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});
			titleContainer.addView(searchField);

			content.addView(titleContainer);
			content.addView(gridView);
			builder.setView(content);
			dialog = builder.create();
			dialog.setOnCancelListener(cancelListener);
			dialog.show();
		} catch (JSONException e) {
			Snackbar.make(pagerTable, getString(R.string.snackbar_error) + e.getMessage(), Snackbar.LENGTH_LONG).setAction("OK", null).show();
			swipeRefresh.setRefreshing(false);
			e.printStackTrace();
		}
	}

	@SuppressWarnings("SameParameterValue")
	@Contract(pure = true)
	private int dp2px(int dp) {
		return (int) (dp * scale + 0.5f);
	}

	private void setTarget(int elemId, int elemType, String displayName) {
		sessionInfo.setElemId(elemId);
		sessionInfo.setElemType(SessionInfo.getElemTypeName(elemType));
		sessionInfo.setDisplayName(displayName);
		if (getSupportActionBar() != null)
			getSupportActionBar().setTitle(displayName);
		switch (elemType) {
			case CLASS:
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_classes);
				break;
			case TEACHER:
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_teachers);
				break;
			case ROOM:
				((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(R.id.nav_show_rooms);
				break;
		}
		refresh();
	}

	private boolean checkLoginState() {
		SharedPreferences prefs = getSharedPreferences("login_data", MODE_PRIVATE);
		return !(prefs.getString("url", "N/A").equals("N/A") ||
				prefs.getString("school", "N/A").equals("N/A") ||
				prefs.getString("user", "N/A").equals("N/A") ||
				prefs.getString("key", "N/A").equals("N/A"));
	}

	private void addHour(TimegridUnitManager.UnitData unitData) {
		LinearLayout sidebar = (LinearLayout) findViewById(R.id.hour_view_sidebar);
		@SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.item_hour, null);
		((TextView) v.findViewById(R.id.tvTimeStart)).setText(unitData.getDisplayStartTime());
		((TextView) v.findViewById(R.id.tvTimeEnd)).setText(unitData.getDisplayEndTime());
		((TextView) v.findViewById(R.id.tvHourIndex)).setText(unitData.getIndex());
		sidebar.addView(v);
	}

	public SharedPreferences getLoginData() {
		return getSharedPreferences("login_data", MODE_PRIVATE);
	}

	public void stopRefreshing() {
		swipeRefresh.setRefreshing(false);
	}

	private void checkVersion() {
		Intent i = getIntent();
		if (i.getBooleanExtra("disable_update_check", false))
			return;
		AutoUpdater au = new AutoUpdater() {
			@Override
			public void onAppVersionOutdated() {
				Intent intent = new Intent(getApplicationContext(), ActivityAppUpdate.class);
				startActivity(intent);
				finish();
			}
		};
		au.setVersionURL("https://data.sapuseven.com/BetterUntis/api.php?method=getVersion");
		try {
			au.setPackageInfo(getPackageManager(), getPackageName());
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		au.startAutoUpdate(this);
	}

	private Calendar getStartDateFromWeek(Calendar c) {
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.add(Calendar.DATE, (currentViewPos - 50) * 7);
		return c;
	}

	private int getStartDateFromWeek() {
		return Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.US).format(getStartDateFromWeek(Calendar.getInstance()).getTime()));
	}

	@SuppressWarnings("SameParameterValue")
	private int addDaysToInt(int startDate, int days) {
		try {
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
			c.setTime(sdf.parse(Integer.toString(startDate)));
			c.add(Calendar.DATE, days);
			return Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.US).format(c.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
			return startDate;
		}
	}

	private class CheckForNewFeatures extends AsyncTask<Void, Void, Boolean> {
		ProgressBar pbLoading;

		@Override
		protected void onPreExecute() {
			pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
			pbLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			try {
				SharedPreferences prefs = getSharedPreferences("login_data", MODE_PRIVATE);
				String user = prefs.getString("user", "");
				URL url = new URL("https://data.sapuseven.com/BetterUntis/api.php?method=CheckForNewFeatures&name=" + user);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
				JSONObject list = new JSONObject(readStream(in));
				urlConnection.disconnect();
				return list.optJSONObject("result").optBoolean("newFeatures");
			} catch (JSONException | IOException | NullPointerException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean newFeatureAvailable) {
			if (newFeatureAvailable) {
				Snackbar.make(findViewById(R.id.content_main), R.string.new_feature_planned, Snackbar.LENGTH_INDEFINITE).setAction(R.string.show, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent i = new Intent(ActivityMain.this, ActivityFeatures.class);
						startActivity(i);
					}
				}).show();
			}
		}
	}
}