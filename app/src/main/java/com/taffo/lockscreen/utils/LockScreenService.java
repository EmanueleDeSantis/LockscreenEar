/*
   LockScreen, an Android lockscreen for people with perfect pitch
   Copyright (C) 2021  Emanuele De Santis

   LockScreen is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   LockScreen is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with LockScreen.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.taffo.lockscreen.utils;

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
import android.graphics.Color;
import android.os.IBinder;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
	CheckPermissions cp;
	SharedPref sp;
	static boolean isLockScreenRunning;

	//The actual number of notes to play (used in LockScreenActivity)
	private static String val;
	public int getNotes() {
		return Integer.parseInt(val);
	}

	//The actual number total number of stored notes in "res/raw" folder got from the xml document "notes.xml" in "src/main/assets" folder (used in LockScreenActivity)
	private static int totalVal;
	public int getTotalNotes() {
		return totalVal;
	}

	//The xml document is parsed here for optimizing time (used in LockScreenActivity)
	private static Document docum;
	public Document getDocum() {
		return docum;
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (context != null && intent.getAction() != null) {
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction().equals("changeNotification"))
					startLockForeground();
				if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
					val = sp.getSharedmPrefNotes();
					parseXmlNotes(context);
					startLockScreenActivity();
				}
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
		cp = new CheckPermissions();
		sp = new SharedPref(this);
		if (cp.checkPermissions(this)) {
			startLockForeground();
			listenerNotes = (prefs, key) -> {
				if (prefs.equals(sp.getmPrefNotes()))
					startLockForeground();
			};
			sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);
		}
	}

	//Registers the receiver for lock screen events
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
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
		NotificationCompat.Action increaseNotes = new NotificationCompat.Action.Builder(0, getString(R.string.notes_incr), actionIncrementNotes).build();

		try {
			LockScreenActivity lsa = new LockScreenActivity();
			isLockScreenRunning = lsa.isLockScreenRunning();

		} catch (Exception e) {
			isLockScreenRunning = false;
		}

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name));
		if (!(isLockScreenRunning || ((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked())) {
			//Notification panel when the screen is unlocked
			//Green
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(Color.GREEN)
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNotes())
					.setContentText(getString(R.string.service_running))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(increaseNotes)
					.build();
			startForeground(NOTIFICATION_ID, notification);
		} else {
			//Notification panel when the screen is locked
			//Red
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
		((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(chan);
	}

	//Unregisters the receiver
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
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
		startActivity(new Intent(this, LockScreenActivity.class)
        		.setAction(Intent.ACTION_VIEW)
        		.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

}