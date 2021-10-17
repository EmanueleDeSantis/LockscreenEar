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
import android.bluetooth.BluetoothManager;
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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.taffo.lockscreen.LockScreenActivity;
import com.taffo.lockscreen.MainActivity;
import com.taffo.lockscreen.R;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;
import com.taffo.lockscreen.utils.XMLParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public final class LockScreenService extends Service {
	private static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;
	private SharedPref sp;
	private final CheckPermissions cp = new CheckPermissions();
	private static boolean isVolumeAdapterRunning = false;
	private ContentObserver volumeObserver;
	private AudioManager audio;
	private static int previousVolume;
	private static boolean areHeadPhonesPlugged = false;
	private static boolean dontStartEarActivities = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction().equals("changeNotificationColor"))
				startLockForeground();
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
				startLockScreenActivity();
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				if (state == 0)
					areHeadPhonesPlugged = false;
				if (state == 1)
					areHeadPhonesPlugged = true;
			}
			if (intent.getAction().equals("finishedLockScreenActivity")
					&& sp.getSharedmRestorePreviousVolumeServiceSetting()
					|| (intent.getAction().equals(Intent.ACTION_REBOOT)
							|| intent.getAction().equals(Intent.ACTION_SHUTDOWN)
							&& ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()))
				audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
			if (intent.getAction().equals("parsingError"))
				dontStartEarActivities = true;
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
		filter.addAction(Intent.ACTION_REBOOT);
		filter.addAction(Intent.ACTION_SHUTDOWN);
		//Used to change color of the notification, green when screen is unlocked, red when is locked
		filter.addAction("changeNotificationColor");
		//Used instead of "changeNotificationColor" to set previousVolume avoiding increasing volume while LockScreenActivity finishes
		filter.addAction("finishedLockScreenActivity");
		//Used to prevent Ear Training activities from starting when a parsing error occurs
		filter.addAction("parsingError");
		registerReceiver(mReceiver, filter);

		if (cp.checkPermissions(this) && sp.getSharedmPrefService()) {
			startLockForeground();
			listenerNotes = (prefs, key) -> {
				if (prefs.equals(sp.getmPrefNotes()))
					startLockForeground();
			};
			sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);
			volumeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
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

	//Unregisters the receiver
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		getContentResolver().unregisterContentObserver(volumeObserver);
		sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
		if (sp.getSharedmRestorePreviousVolumeServiceSetting()
				&& ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked())
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
		releaseMediaRecorder();
		//Updates the tile
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
	}

	private void startLockScreenActivity() {
		XMLParser parser = new XMLParser();
		parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
		parser.parseXmlNotes(this);
		if (dontStartEarActivities) {
			Toast.makeText(this, getString(R.string.cant_start_service), Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, MainActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			sp.setSharedmPrefService(false);
			stopSelf();
		} else
			startActivity(new Intent(this, LockScreenActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	//Foreground service (with pending intent):
	//Since android 10 (maybe before?) after 20 seconds services go in background. Solved using accessibility service
	private void startLockForeground() {
		if (sp.getSharedmPrefFirstRunAccessibilitySettings())
			sp.setSharedmPrefFirstRunAccessibilitySettings(false);

		NotificationChannel chan = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(chan);

		Intent notificationIntent = new Intent(this, MainActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent
				.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		Intent broadcastIntent = new Intent(this, IncrementNumberOfNotes.class);
		PendingIntent actionIncrementNotes = PendingIntent
				.getService(this, 0, broadcastIntent, PendingIntent.FLAG_IMMUTABLE);
		NotificationCompat.Action increaseNotes = new NotificationCompat.Action.Builder(
				0, getString(R.string.notes_increase), actionIncrementNotes).build();

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
					.setContentText(getString(R.string.volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
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
					.setContentText(getString(R.string.volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(increaseNotes)
					.build();
			startForeground(5, notification);
		}

		if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
				&& sp.getSharedmPrefVolumeAdapterServiceSetting()
				&& !isVolumeAdapterRunning
				&& ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked())
			volumeAdapter(this);

		//Updates the tile
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
	}

	private MediaRecorder mRecorder = null;
	private final int LIST_EL = 10;
	private final List<Double> dbList = new ArrayList<>(LIST_EL);
	private static int dbMean = 0;
	private int DB_MIN;
	private int DB_MAX;

	private void volumeAdapter(Context context) {
		previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		try {
			JSONObject object = new JSONObject(sp.getSharedmPrefVolumeAdjustmentLevelAdapterServiceSetting());
			DB_MIN = Integer.parseInt(object.getString("db_min"));
			DB_MAX = Integer.parseInt(object.getString("db_max"));
		} catch (JSONException e) {
			DB_MIN = 25; //Normal
			DB_MAX = 100; //level
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mustVolumeAdapterStop()) {
					releaseMediaRecorder();
					dbList.clear();
					if (!((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()) {
						timer.cancel();
						cancel();
					}
				} else {
					if (mRecorder == null)
						try {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
								mRecorder = new MediaRecorder(context); //Not tested yet
							else
								mRecorder = new MediaRecorder();
							mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
							mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
							mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
							mRecorder.setOutputFile("/dev/null");
							mRecorder.prepare();
							mRecorder.start();
							isVolumeAdapterRunning = true;
						} catch (IOException | RuntimeException e) {
							releaseMediaRecorder();
						}
					else {
						double sum = 0;
						double db = 20 * Math.log10(mRecorder.getMaxAmplitude());
						if (db > DB_MIN && db < DB_MAX)
							dbList.add(db);
						if (dbList.size() == LIST_EL) {
							for (int i = 0; i < LIST_EL; i++) {
								//Mean does not include max and min registered db sounds
								if (dbList.get(i).equals(Collections.max(dbList)) || (dbList.get(i).equals(Collections.min(dbList))))
									continue;
								sum += (1 / dbList.get(i));
							}
							dbMean = (int) Math.round((LIST_EL - 2) / sum); //Harmonic mean
							Collections.rotate(dbList, -1); //List left rotation to
							dbList.remove(LIST_EL - 1); //remove last value from the list  (it will be added in the next cycle)
						} else {
							for (int i = 0; i < dbList.size(); i++) {
								sum += (1 / dbList.get(i));
								if (i == dbList.size() - 1)  //Last element
									dbMean = (int) Math.round(dbList.size() / sum); //Harmonic mean
							}
						}
						audio.setStreamVolume(AudioManager.STREAM_MUSIC,
							1 + (((audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1) //Experimental
									* (dbMean - DB_MIN)) / (DB_MAX - DB_MIN)), 0); //formula
					}
				}
			}
		}, 0, 1000); //Every second
	}

	private boolean mustVolumeAdapterStop() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			return !((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
					|| audio.isMusicActive()
					|| audio.getMode() != AudioManager.MODE_NORMAL //Check if microphone is (not) available
					|| areHeadPhonesPlugged
					|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
							.getAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)
					|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
							.getAdapter().getProfileConnectionState(BluetoothProfile.HEARING_AID);
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			return !((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
					|| audio.isMusicActive()
					|| audio.getMode() != AudioManager.MODE_NORMAL
					|| areHeadPhonesPlugged
					|| BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter()
							.getProfileConnectionState(BluetoothProfile.HEADSET)
					|| BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter()
							.getProfileConnectionState(BluetoothProfile.HEARING_AID);
		else
			return !((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
					|| audio.isMusicActive()
					|| audio.getMode() != AudioManager.MODE_NORMAL
					|| areHeadPhonesPlugged
					|| BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter()
							.getProfileConnectionState(BluetoothProfile.HEADSET);
	}

	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		isVolumeAdapterRunning = false;
	}

	public final static class IncrementNumberOfNotes extends Service {
		@Override
		public IBinder onBind(@Nullable Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
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