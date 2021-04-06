/*
Copyright [2021] [Emanuele De Santis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.taffo.lockscreen.utils;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.IBinder;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.taffo.lockscreen.LockScreenActivity;
import com.taffo.lockscreen.MainActivity;
import com.taffo.lockscreen.R;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LockScreenService extends Service {
	private static final int NOTIFICATION_ID = 5;
	static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;
	SharedPref sp;
	boolean isLockScreenRunning;

	private static String val;
	public int getNotes() {
		return Integer.parseInt(val);
	}

	private static int totalVal;
	public int getTotalNotes() {
		return totalVal;
	}
	private static Document docum;
	public Document getDocum() {
		return docum;
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (context != null && intent.getAction() != null) {
				if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
					val = sp.getSharedmPrefNotes();
					parseXmlNotes(context);
					startLockScreenActivity();
				}
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction().equals("changeNotification"))
					startLockForeground();
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		sp = new SharedPref(this);
		LockAccessibilityService las = new LockAccessibilityService();
		if (las.isAccessibilitySettingsOn(this)
				&& ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
				&& sp.getSharedmPrefService()
				&& Settings.canDrawOverlays(this)) {
			startLockForeground();
			listenerNotes = (prefs, key) -> {
				if (prefs.equals(sp.getmPrefNotes()))
					startLockForeground();
			};
			sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);
		}
	}

	//Register receiver when screen goes off
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction("changeNotification"); //Change color of the notification, green when screen is unlocked, red when locked
		registerReceiver(mReceiver, filter);
		return START_NOT_STICKY;
	}

	//"Foreground" service: By android 10 (maybe before?) after 20 seconds this service goes in background. Solved using the accessibility service
	public void startLockForeground() {
		Intent notificationIntent =  new Intent(this, MainActivity.class);
		notificationIntent.setAction(Intent.ACTION_VIEW);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Intent broadcastIntent = new Intent(this, IncrementNumberOfNotes.class);
		PendingIntent actionIncrementNotes = PendingIntent.getBroadcast(this,
				0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Action increaseNotes = new NotificationCompat.Action.Builder(0, getString(R.string.notes_incr), actionIncrementNotes)
				.build();

		try {
			LockScreenActivity lsa = new LockScreenActivity();
			isLockScreenRunning = lsa.isLockScreenRunning();

		} catch (Exception e) {
			isLockScreenRunning = false;
		}
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name));
		if (!isLockScreenRunning && !((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()) {
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setSmallIcon(R.drawable.unlocked_icon)
					.setColor(Color.GREEN)
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNotes())
					.setContentText(getString(R.string.service_running))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(increaseNotes)
					.build();
			startForeground(NOTIFICATION_ID, notification);
		}
		else {
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(Color.RED)
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNotes())
					.setContentText(getString(R.string.service_running))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(increaseNotes)
					.build();
			startForeground(NOTIFICATION_ID, notification);
		}

		NotificationChannel chan = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
		chan.setLightColor(Color.GREEN);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		assert manager != null;
		manager.createNotificationChannel(chan);
	}

	// Unregister receiver
	@Override
	public void onDestroy() {
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
		unregisterReceiver(mReceiver);
		sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
		super.onDestroy();
	}

	private void parseXmlNotes(Context cntx) {
		try {
			InputStream is = cntx.getAssets().open("notes.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			docum = dBuilder.parse(is);
			docum.getDocumentElement().normalize();
			totalVal = docum.getElementsByTagName("note").getLength();
		} catch (ParserConfigurationException | SAXException | IOException ignored) {}
	}

	private void startLockScreenActivity() {
        Intent startLockScreenActIntent = new Intent(this, LockScreenActivity.class);
        startLockScreenActIntent.setAction(Intent.ACTION_VIEW);
        startLockScreenActIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startLockScreenActIntent);
    }

}