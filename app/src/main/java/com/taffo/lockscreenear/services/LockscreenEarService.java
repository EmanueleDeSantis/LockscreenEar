/*
   LockscreenEar, an Android lockscreen for people with perfect pitch
   Copyright (C) 2021  Emanuele De Santis

   LockscreenEar is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   LockscreenEar is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with LockscreenEar.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.taffo.lockscreenear.services;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.taffo.lockscreenear.activities.LockscreenEarActivity;
import com.taffo.lockscreenear.activities.MainActivity;
import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.utils.Utils;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.XMLParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class LockscreenEarService extends Service {
	private static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;
	private static LockscreenEarService instance;
	private static int previousVolume;
	private static boolean areHeadPhonesPlugged = false;
	private SharedPref sp;
	private ContentObserver volumeObserver;
	private AudioManager audio;
	private ScheduledExecutorService scheduler;
	private MediaRecorder mRecorder = null;
	private final int LIST_EL = 10;
	private final List<Double> dbList = new ArrayList<>(LIST_EL);
	private static int dbMean = 0;
	private int DB_MIN_VALUE;
	private int DB_MAX_VALUE;

	//From android 10 (maybe before?), after 20 seconds this registered background receiver goes in background. Solved using Accessibility Service
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				startLockForeground();
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				startLockscreenEarActivity();
				startLockForeground();
			}
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				areHeadPhonesPlugged = intent.getIntExtra("state", -1) > 0;
				startLockForeground();
			}
			if (sp.getSharedmRestorePreviousVolumeServiceSetting()
					&& intent.getAction().equals("com.taffo.lockscreen.action.LockscreenEarActivityFinished"))
				audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
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
		filter.addAction("com.taffo.lockscreen.action.LockscreenEarActivityFinished");
		registerReceiver(mReceiver, filter);

		if (instance == null)
			instance = this;

		//Not tested yet
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			audio.addOnModeChangedListener(Executors.newSingleThreadExecutor(), i -> {
				if (Utils.getCallAndCallSetting(this))
					startLockForeground();
			});
		}

		if (new Utils().checkPermissions(this) && sp.getSharedmPrefService()) {
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
		instance = null;
		unregisterReceiver(mReceiver);
		if (volumeObserver != null)
			getContentResolver().unregisterContentObserver(volumeObserver);
		sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
		if (sp.getSharedmRestorePreviousVolumeServiceSetting()
				&& ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked())
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
		releaseMediaRecorder();
		if ((scheduler != null && !scheduler.isShutdown()))
			scheduler.shutdownNow();
		//Updates the tile
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
	}

	private void startLockscreenEarActivity() {
		XMLParser parser = new XMLParser();
		if (parser.parseXmlNotes(this)) {
			parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
			if (!Utils.getCallAndCallSetting(this)) {
				startActivity(new Intent(this, LockscreenEarActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			} else
				startLockForeground();
		} else {
			startActivity(new Intent(this, MainActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			sp.setSharedmPrefService(false);
			stopSelf();
		}
	}

	//Foreground service (with pending intent):
	private void startLockForeground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
					getString(R.string.app_name),
					getString(R.string.app_name),
					NotificationManager.IMPORTANCE_HIGH);
			channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }

		PendingIntent pendingIntent = PendingIntent
				.getActivity(
						this,
						0,
						new Intent(this, MainActivity.class)
								.addCategory(Intent.CATEGORY_LAUNCHER)
								.setAction(Intent.ACTION_MAIN),
						PendingIntent.FLAG_IMMUTABLE);

		NotificationCompat.Action incrementNotes = new NotificationCompat.Action.Builder(
				0,
				getString(R.string.increase_number_of_notes),
				PendingIntent.getService(
						this,
						0,
						new Intent(this, IncreaseNumberOfNotes.class),
						PendingIntent.FLAG_IMMUTABLE)).build();

		NotificationCompat.Action increaseVolumeLevel = new NotificationCompat.Action.Builder(
				0,
				getString(R.string.increase_volume_level),
				PendingIntent.getService(
						this,
						0,
						new Intent(this, IncreaseVolumeLevel.class),
						PendingIntent.FLAG_IMMUTABLE)).build();

		NotificationCompat.Action startVolumeAdjuster = new NotificationCompat.Action.Builder(
				0,
				getString(R.string.start_volume_adjuster),
				PendingIntent.getService(
						this,
						0,
						new Intent(this, StartVolumeAdjuster.class),
						PendingIntent.FLAG_IMMUTABLE)).build();

		NotificationCompat.Action stopVolumeAdjuster = new NotificationCompat.Action.Builder(
				0,
				getString(R.string.stop_volume_adjuster),
				PendingIntent.getService(
						this,
						0,
						new Intent(this, StopVolumeAdjuster.class),
						PendingIntent.FLAG_IMMUTABLE)).build();

		NotificationCompat.Action changeVolumeAdjusterMode = new NotificationCompat.Action.Builder(
				0,
				getString(R.string.mode) + volumeAdjusterMode().get(0),
				PendingIntent.getService(
						this,
						0,
						new Intent(this, IncreaseVolumeAdjusterMode.class),
						PendingIntent.FLAG_IMMUTABLE)).build();

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_name));

		if (!((KeyguardManager) instance.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
				&& !Utils.getIsLockscreenEarRunning()
				|| Utils.getCallAndCallSetting(this)) { //Check if microphone is (not) available + call state
			//Green Notification panel when the screen is unlocked or a call is in progress and the user don't want LockscreenEar to start
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setChannelId(getString(R.string.app_name))
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(getColor(R.color.custom_color_accent))
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
					.setContentText(getString(R.string.media_volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(incrementNotes)
					.addAction(increaseVolumeLevel)
					.build();
			startForeground(5, notification);
		} else if (!((KeyguardManager) instance.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
				&& Utils.getIsLockscreenEarRunning()) {
			//Red Notification panel when LockscreenEar is running
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setChannelId(getString(R.string.app_name))
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(getColor(R.color.custom_notification_color))
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
					.setContentText(getString(R.string.media_volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.build();
			startForeground(5, notification);
		} else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
				|| mustVolumeAdjusterStop()) {
			//Red Notification panel when the screen is locked and there is no record audio permission
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setChannelId(getString(R.string.app_name))
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(getColor(R.color.custom_notification_color))
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
					.setContentText(getString(R.string.media_volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(increaseVolumeLevel)
					.build();
			startForeground(5, notification);
		} else if (!sp.getSharedmPrefVolumeAdjusterServiceSetting()) {
			//Red Notification panel when the screen is locked without volumeAdjuster running
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setChannelId(getString(R.string.app_name))
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(getColor(R.color.custom_notification_color))
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
					.setContentText(getString(R.string.media_volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(startVolumeAdjuster)
					.addAction(increaseVolumeLevel)
					.build();
			startForeground(5, notification);
		} else {
			//Red Notification panel when the screen is locked with volumeAdjuster running
			Notification notification = notificationBuilder
					.setOngoing(true)
					.setOnlyAlertOnce(true)
					.setChannelId(getString(R.string.app_name))
					.setSmallIcon(R.drawable.locked_icon)
					.setColor(getColor(R.color.custom_notification_color))
					.setContentTitle(getString(R.string.number_of_notes_to_play) + ": " + sp.getSharedmPrefNumberOfNotesToPlay())
					.setContentText(getString(R.string.media_volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
							+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
					.setPriority(NotificationManager.IMPORTANCE_HIGH)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setContentIntent(pendingIntent)
					.addAction(stopVolumeAdjuster)
					.addAction(changeVolumeAdjusterMode)
					.build();
			startForeground(5, notification);
		}

		if (ActivityCompat.checkSelfPermission(getApplicationContext(),
				Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
				&& (scheduler == null || scheduler.isShutdown())
				&& !mustVolumeAdjusterStop()
				&& sp.getSharedmPrefVolumeAdjusterServiceSetting()
				&& ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked())
			volumeAdjuster(this);

		//Updates the tile
		TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
	}

	private List<String> volumeAdjusterMode() {
		String MODE;
		String DB_MIN;
		String DB_MAX;
		try {
			JSONObject object = new JSONObject(sp.getSharedmPrefVolumeAdjusterModeServiceSetting());
			MODE = object.getString("mode");
			DB_MIN = object.getString("db_min");
			DB_MAX = object.getString("db_max");
		} catch (JSONException ignored) {
			MODE = getString(R.string.array_item_volume_adjuster_mode_normal); //Default
			DB_MIN = "25"; //mode
			DB_MAX = "100"; //value
		}
		switch (MODE) {
			case "quiet":
				MODE = getString(R.string.array_item_volume_adjuster_mode_quiet);
				break;
			case "normal":
				MODE = getString(R.string.array_item_volume_adjuster_mode_normal);
				break;
			case "loud":
				MODE = getString(R.string.array_item_volume_adjuster_mode_loud);
				break;
		}
		return new ArrayList<>(Arrays.asList(MODE, DB_MIN, DB_MAX));
	}

	private void volumeAdjuster(Context context) {
		previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(() -> {
			if (mustVolumeAdjusterStop() || !sp.getSharedmPrefVolumeAdjusterServiceSetting()) {
				releaseMediaRecorder();
				if (!(((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
						&& sp.getSharedmPrefVolumeAdjusterServiceSetting()))
					scheduler.shutdownNow();
			} else if (mRecorder == null) {
				try {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
						mRecorder = new MediaRecorder(context); //Not tested yet
					else
						mRecorder = new MediaRecorder();
					mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
						mRecorder.setOutputFile(getExternalCacheDir() + "/test.3gp");
					else
						mRecorder.setOutputFile("dev/null");
					mRecorder.prepare();
					mRecorder.start();
					startLockForeground();
				} catch (IOException | RuntimeException ignored) {
					releaseMediaRecorder();
				}
			} else {
				DB_MIN_VALUE = Integer.parseInt(volumeAdjusterMode().get(1));
				DB_MAX_VALUE = Integer.parseInt(volumeAdjusterMode().get(2));
				double sum = 0;
				double db = 20 * Math.log10(mRecorder.getMaxAmplitude());
				if (db > DB_MIN_VALUE && db < DB_MAX_VALUE && dbList.size() < LIST_EL)
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
					dbList.remove(LIST_EL - 1); //remove last value from the list (it will be added in the next cycle)
				} else {
					for (int i = 0; i < dbList.size(); i++) {
						sum += (1 / dbList.get(i));
						if (i == dbList.size() - 1)  //Last element
							dbMean = (int) Math.round(dbList.size() / sum); //Harmonic mean
					}
				}
				audio.setStreamVolume(AudioManager.STREAM_MUSIC,
						1 + (((audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1) //Experimental
								* (dbMean - DB_MIN_VALUE)) / (DB_MAX_VALUE - DB_MIN_VALUE)), 0); //formula
			}
		}, 0, 1, TimeUnit.SECONDS); //Every second
	}

	public static boolean mustVolumeAdjusterStop() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			if (ActivityCompat.checkSelfPermission(instance, Manifest.permission.BLUETOOTH_CONNECT)
					== PackageManager.PERMISSION_GRANTED)
				return !((KeyguardManager) instance.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
						|| instance.audio.isMusicActive()
						|| instance.audio.getMode() != AudioManager.MODE_NORMAL //Check if microphone is (not) available + call state
						|| areHeadPhonesPlugged
						|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) instance.getSystemService(Context.BLUETOOTH_SERVICE))
								.getAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)
						|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) instance.getSystemService(Context.BLUETOOTH_SERVICE))
								.getAdapter().getProfileConnectionState(BluetoothProfile.HEARING_AID);
			else
				return true;
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			return !((KeyguardManager) instance.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
					|| instance.audio.isMusicActive()
					|| instance.audio.getMode() != AudioManager.MODE_NORMAL
					|| areHeadPhonesPlugged
					|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) instance.getSystemService(Context.BLUETOOTH_SERVICE))
							.getAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)
					|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) instance.getSystemService(Context.BLUETOOTH_SERVICE))
							.getAdapter().getProfileConnectionState(BluetoothProfile.HEARING_AID);
		else
			return !((KeyguardManager) instance.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()
					|| instance.audio.isMusicActive()
					|| instance.audio.getMode() != AudioManager.MODE_NORMAL
					|| areHeadPhonesPlugged
					|| BluetoothProfile.STATE_CONNECTED == ((BluetoothManager) instance.getSystemService(Context.BLUETOOTH_SERVICE))
							.getAdapter().getProfileConnectionState(BluetoothProfile.HEADSET);
	}

	private void releaseMediaRecorder() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		dbList.clear();
	}

}