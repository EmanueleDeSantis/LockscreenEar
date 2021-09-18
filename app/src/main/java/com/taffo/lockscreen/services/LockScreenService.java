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

package com.taffo.lockscreen.services;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.taffo.lockscreen.LockScreenActivity;
import com.taffo.lockscreen.MainActivity;
import com.taffo.lockscreen.R;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;
import com.taffo.lockscreen.utils.XMLParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public final class LockScreenService extends Service {
	private static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;
	private SharedPref sp;
	private final CheckPermissions cp = new CheckPermissions();
	private static boolean stopVolumeAdapterService = false;
	private static boolean isVolumeAdapterRunning = false;
	private ContentObserver volumeObserver;
	private AudioManager audio;
	private static boolean areHeadPhonesPlugged = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				stopVolumeAdapterService = false;
				startLockForeground();
			}
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				stopVolumeAdapterService = true;
				startLockScreenActivity();
			}
			if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
				int state = intent.getIntExtra("state", -1);
				if (state == 0)
					areHeadPhonesPlugged = false;
				if (state == 1)
					areHeadPhonesPlugged = true;
			}
			if (intent.getAction().equals("changeNotificationColor"))
				startLockForeground();
		}
	};

	@Override
	public IBinder onBind(@Nullable Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		sp = new SharedPref(this);
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	//Registers the receiver for lockscreen events
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction("changeNotificationColor"); //Changes color of the notification, green when screen is unlocked, red when is locked
		registerReceiver(mReceiver, filter);

		if (cp.checkPermissions(this) && sp.getSharedmPrefService()) {
			startLockForeground();
			listenerNotes = (prefs, key) -> {
				if (prefs.equals(sp.getmPrefNotes()))
					startLockForeground();
			};
			sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);
			volumeObserver = new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					super.onChange(selfChange);
					startLockForeground();
				}
			};
			getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver);
		} else
			stopSelf();
		return START_NOT_STICKY;
	}

	//"Foreground" service (with pending notification): Since android 10 (maybe before?) after 20 seconds services go in background. Solved using priorities in the manifest
	private void startLockForeground() {
		if (sp.getSharedmPrefFirstRunAccessibilitySettings())
			sp.setSharedmPrefFirstRunAccessibilitySettings(false);

		if (canVolumeAdapterBeStarted())
			volumeAdapter();

		NotificationChannel chan = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(chan);

		Intent notificationIntent = new Intent(this, MainActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		Intent broadcastIntent = new Intent(this, IncrementNumberOfNotes.class);
		PendingIntent actionIncrementNotes = PendingIntent.getService(this, 0, broadcastIntent, PendingIntent.FLAG_IMMUTABLE);
		NotificationCompat.Action increaseNotes = new NotificationCompat.Action.Builder(0, getString(R.string.notes_increase), actionIncrementNotes).build();

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name));
		if (!CheckPermissions.getIsScreenLocked(this)) {
			//Notification panel when the screen is unlocked
			//Green
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setChannelId(getString(R.string.app_name))
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(Color.GREEN)
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
					.setContentText(getString(R.string.volume_level) + ": " + (audio.getStreamVolume(AudioManager.STREAM_MUSIC)  * 100
							/ audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) + "%")
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(increaseNotes)
					.build();
			startForeground(5, notification);
		} else {
			//Notification panel when the screen is locked
			//Red
			Notification notification = notificationBuilder
						.setOngoing(true)
						.setOnlyAlertOnce(true)
						.setChannelId(getString(R.string.app_name))
						.setSmallIcon(R.drawable.locked_icon)
						.setColor(Color.RED)
						.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
						.setContentText(getString(R.string.volume_level) + ": " + (audio.getStreamVolume(AudioManager.STREAM_MUSIC)  * 100
								/ audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) + "%")
						.setPriority(NotificationManager.IMPORTANCE_HIGH)
						.setCategory(Notification.CATEGORY_SERVICE)
						.setContentIntent(pendingIntent)
						.addAction(increaseNotes)
						.build();
			startForeground(5, notification);
		}
		//Updates the tile
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
	}

	//Unregisters the receiver
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		getContentResolver().unregisterContentObserver(volumeObserver);
		sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
		//Updates the tile
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
	}

	private void startLockScreenActivity() {
		XMLParser parser = new XMLParser();
		parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
		parser.parseXmlNotes(this);
		startActivity(new Intent(this, LockScreenActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	private boolean canVolumeAdapterBeStarted() {
		return sp.getSharedmVolumeAdapterServiceSetting()
				&& ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
				&& !isVolumeAdapterRunning
				&& !stopVolumeAdapterService;
	}

	private final int LIST_EL = 5;
	private final List<Double> dbList = new ArrayList<>(LIST_EL);
	private static int dbMean = 0;
	private final int DB_LIMIT = 110;
	private MediaRecorder mRecorder = null;

	private void volumeAdapter() {
		mRecorder = null;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stopVolumeAdapterService
						|| !((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()
						|| Objects.requireNonNull(audio).isMusicActive()
						|| areHeadPhonesPlugged
						|| BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter()
								.getProfileConnectionState(BluetoothProfile.HEADSET)) {
					if (stopVolumeAdapterService)
						timer.cancel();
					if (mRecorder != null) {
						mRecorder.release();
						mRecorder = null;
						isVolumeAdapterRunning = false;
					}
					//isVolumeAdapterRunning = false;
					dbList.clear();
				} else {
					if (mRecorder == null) {
						try {
							mRecorder = new MediaRecorder();
							mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
							mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
							mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
							mRecorder.setOutputFile("/dev/null");
							mRecorder.prepare();
							mRecorder.start();
							isVolumeAdapterRunning = true;
						} catch (IOException e) {
							e.printStackTrace();
							mRecorder.release();
							mRecorder = null;
							isVolumeAdapterRunning = false;
						}
					} else {
						double sum = 0;
						double db = 20 * Math.log10(mRecorder.getMaxAmplitude());
						if (db > 20 && db < DB_LIMIT)
							dbList.add(db);
						if (dbList.size() == LIST_EL) {
							for (int i = 0; i < LIST_EL; i++) {
								//Mean does not include max and min registered db sounds
								if (Collections.max(dbList).equals(dbList.get(i)) || Collections.min(dbList).equals(dbList.get(i)))
									continue;
								sum += (1 / dbList.get(i));
							}
							dbMean = (int) ((LIST_EL - 2) / sum); //Harmonic mean
							Collections.rotate(dbList, -1); //List rotation
							dbList.remove(LIST_EL - 1); //Remove last value from the list
						} else {
							for (int i = 0; i < dbList.size(); i++) {
								sum += (1 / dbList.get(i));
								if (i == dbList.size() - 1)  //last element
									dbMean = (int) (dbList.size() / sum);
							}
						}
						Objects.requireNonNull(audio).setStreamVolume(AudioManager.STREAM_MUSIC,
								-1 + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * dbMean / DB_LIMIT, 0); //Experimental formula
					}
				}
			}
		}, 0, 1000); //every second
	}

	public static class IncrementNumberOfNotes extends Service {
		@Override
		public IBinder onBind(@Nullable Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			new CheckPermissions();
			if (!CheckPermissions.getIsScreenLocked(this)) {
				SharedPref sp = new SharedPref(this);
				int actualNumNotes = Integer.parseInt(sp.getSharedmPrefNumberOfNotesToPlay());
				int numIncremented;
				if (actualNumNotes < 8)
					numIncremented = actualNumNotes + 1;
				else
					numIncremented = 1;
				sp.setSharedmPrefNumberOfNotesToPlay(String.valueOf(numIncremented));
			}
			return super.onStartCommand(intent, flags, startId);
		}

	}

}