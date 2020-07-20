package com.example.dbdubug;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.dbdubug.database.CarDBHelper;
import com.example.dbdubug.database.ContactDBHelper;
import com.example.dbdubug.database.ExtTestDBHelper;
import com.example.dbdubug.database.room.User;
import com.example.dbdubug.database.room.UserDBHelper;
import com.example.dbdubug.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "MainActivity";
	@SuppressLint("CommitPrefEdits")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Set<String> stringSet = new HashSet<>();
		stringSet.add("SetOne");
		stringSet.add("SetTwo");
		stringSet.add("SetThree");
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		SharedPreferences prefsOne = getSharedPreferences("countPrefOne", Context.MODE_PRIVATE);
		SharedPreferences prefsTwo = getSharedPreferences("countPrefTwo", Context.MODE_PRIVATE);
		
		sharedPreferences.edit().putString("testOne", "one").commit();
		sharedPreferences.edit().putInt("testTwo", 2).commit();
		sharedPreferences.edit().putLong("testThree", 100000L).commit();
		sharedPreferences.edit().putFloat("testFour", 3.01F).commit();
		sharedPreferences.edit().putBoolean("testFive", true).commit();
		sharedPreferences.edit().putStringSet("testSix", stringSet).commit();
		
		prefsOne.edit().putString("testOneNew", "one").commit();
		
		prefsTwo.edit().putString("testTwoNew", "two").commit();
		
		ContactDBHelper contactDBHelper = new ContactDBHelper(getApplicationContext());
		if (contactDBHelper.count() == 0) {
			for (int i = 0; i < 100; i++) {
				String name = "name_" + i;
				String phone = "phone_" + i;
				String email = "email_" + i;
				String street = "street_" + i;
				String place = "place_" + i;
				contactDBHelper.insertContact(name, phone, email, street, place);
			}
		}
		
		CarDBHelper carDBHelper = new CarDBHelper(getApplicationContext());
		if (carDBHelper.count() == 0) {
			for (int i = 0; i < 50; i++) {
				String name = "name_" + i;
				String color = "RED";
				float mileage = i + 10.45f;
				carDBHelper.insertCar(name, color, mileage);
			}
		}
		
		ExtTestDBHelper extTestDBHelper = new ExtTestDBHelper(getApplicationContext());
		if (extTestDBHelper.count() == 0) {
			for (int i = 0; i < 20; i++) {
				String value = "value_" + i;
				extTestDBHelper.insertTest(value);
			}
		}
		
		// Room database
		UserDBHelper userDBHelper = new UserDBHelper(getApplicationContext());
//		if (userDBHelper.count() == 0) {
//			List<User> userList = new ArrayList<>();
//			for (int i = 0; i < 20; i++) {
//				User user = new User();
//				user.id = (long)(i + 1);
//				user.name = "user_" + i;
//				userList.add(user);
//			}
//			userDBHelper.insertUser(userList);
//		}
		
		// Room inMemory database
		if (userDBHelper.countInMemory() == 0) {
			List<User> userList = new ArrayList<>();
			for (int i = 0; i < 20; i++) {
				User user = new User();
				user.id = (long)(i + 1);
				user.name = "in_memory_user_" + i;
				userList.add(user);
			}
			userDBHelper.insertUserInMemory(userList);
		}
		
		Utils.setCustomDatabaseFiles(getApplicationContext());
		Utils.setInMemoryRoomDatabases(userDBHelper.getInMemoryDatabase());
		
		NetworkUtils.getInstance().init(getApplicationContext());
		NetworkUtils.getInstance().addNetworkStateListener(new NetworkUtils.INetworkStateListener() {
			@Override
			public void onNetworkState(int netState, int type) {
				Log.e(TAG, "onNetworkState: " + netState);
				Log.e(TAG, "onNetworkState: " + type);
			}
		});
	}
	
	public void showDebugDbAddress(View view) {
		Utils.showDebugDBAddressLogToast(getApplicationContext());
	}
}