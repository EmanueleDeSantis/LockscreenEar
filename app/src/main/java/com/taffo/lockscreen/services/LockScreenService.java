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
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.service.quicksettings.TileService;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.taffo.lockscreen.DeviceAdminActivity;
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
	private Timer timer;
	private CheckCalls callsListener;
	private TelephonyManager telephony;
	//private static boolean dontStartLockScreenActivity = false;
	private static boolean stopVolumeAdapterService = false;
	private static boolean isVolumeAdapterRunning = false;
	private AudioManager audio;
	private static boolean areHeadPhonesPlugged = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				startLockForeground();
				stopVolumeAdapterService = false;
				if (canVolumeAdapterBeStarted())
					volumeAdapter();
			}
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				/*//If no phone calls arrived that are ringing or waiting, or at least no call exist that are dialing, active, or on hold
				if (!dontStartLockScreenActivity)
					startLockScreenActivity();
				else
					//else unlocks the screen without starting LockScreenActivity, so notification color changes to green
					startLockForeground();*/
				startLockScreenActivity();
				stopVolumeAdapterService = true;
			}
			if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
				int state = intent.getIntExtra("state", -1);
				if (state == 0)
					areHeadPhonesPlugged = false;
				if (state == 1)
					areHeadPhonesPlugged = true;
			}
			if (intent.getAction().equals("changeNotificationColor")
					|| intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION"))
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
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			callsListener = new CheckCalls();
			telephony = (TelephonyManager) Objects.requireNonNull(getSystemService(Context.TELEPHONY_SERVICE));
			telephony.listen(callsListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	//Registers the receiver for lockscreen events
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		filter.addAction("changeNotificationColor"); //Changes color of the notification, green when screen is unlocked, red when is locked
		registerReceiver(mReceiver, filter);

		if (cp.checkPermissions(this) && sp.getSharedmPrefService()) {
			startLockForeground();
			listenerNotes = (prefs, key) -> {
				if (prefs.equals(sp.getmPrefNotes()))
					startLockForeground();
			};
			sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);
		} else
			stopSelf();
		return START_NOT_STICKY;
	}

	//"Foreground" service (with pending notification): Since android 10 (maybe before?) after 20 seconds services go in background. Solved using priorities in the manifest
	private void startLockForeground() {
		if (sp.getSharedmPrefFirstRunAccessibilitySettings())
			sp.setSharedmPrefFirstRunAccessibilitySettings(false);
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
					.setContentText(getString(R.string.service_running))
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
						.setStyle(new NotificationCompat.InboxStyle()
								.addLine(getString(R.string.volume_level) + ": " + audio.getStreamVolume(AudioManager.STREAM_MUSIC)
										+ "/" + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
								.addLine(getString(R.string.service_running)))
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
		sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && telephony != null)
			telephony.listen(callsListener, PhoneStateListener.LISTEN_NONE);
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

	private final int LIST_EL = 5;
	private final List<Double> dbList = new ArrayList<>(LIST_EL);
	private static int dbMean = 0;
	private final int DB_LIMIT = 105;

	private boolean canVolumeAdapterBeStarted() {
		return sp.getSharedmVolumeAdapterServiceSetting()
				&& ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
				&& !isVolumeAdapterRunning;
	}

	private static MediaRecorder mRecorder = null;
	private void volumeAdapter() {
		startLockForeground();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stopVolumeAdapterService
						|| !((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()
						|| Objects.requireNonNull(audio).isMusicActive()
						|| areHeadPhonesPlugged
						|| BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)) {
					if (mRecorder != null) {
						mRecorder.stop();
						mRecorder.release();
					}
					mRecorder = null;
					dbList.clear();
					isVolumeAdapterRunning = false;
					if (stopVolumeAdapterService)
						timer.cancel();
				} else {
					if (mRecorder == null) {
						mRecorder = new MediaRecorder();
						mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						mRecorder.setOutputFile("/dev/null");
						try {
							mRecorder.prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						mRecorder.start();
					}
					isVolumeAdapterRunning = true;
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
						Collections.rotate(dbList, -1);
						dbList.remove(LIST_EL - 1);
					} else {
						for (int i = 0; i < dbList.size(); i++) {
							sum += (1 / dbList.get(i));
							if (i == dbList.size() - 1)  //last element
								dbMean = (int) (dbList.size() / sum);
						}
					}
					Objects.requireNonNull(audio).setStreamVolume(AudioManager.STREAM_MUSIC,
							-2 + audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * dbMean / DB_LIMIT, 0);
				}
				startLockForeground();
			}
		}, 0, 1000); //every second
	}

	//Manages phone calls
	private class CheckCalls extends PhoneStateListener {
		Context context = getApplicationContext();

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
				stopVolumeAdapterService = true; //does not stop the current VolumeAdapterService (see startVolumeAdapterService())
				//dontStartLockScreenActivity = true;
				if (CheckPermissions.getIsScreenLocked(context)) {
					sp.setSharedmPrefService(false);
					stopService(new Intent(context, LockScreenService.class));
					if (CheckPermissions.getIsLockScreenRunning()) {
						new LockScreenActivity().finish();
						if (((DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE))
								.isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)))
							LockAccessibilityService.lockTheScreen();
					}
				}
			} else if (state == TelephonyManager.CALL_STATE_IDLE) {
				stopVolumeAdapterService = false;
				//dontStartLockScreenActivity = false;
				if (cp.checkPermissions(context)) {
					sp.setSharedmPrefService(true);
					startLockForeground();
				}
				if (CheckPermissions.getIsScreenLocked(getApplicationContext()))
					if (canVolumeAdapterBeStarted())
						volumeAdapter();
			}
		}
	}

	public static class IncrementNumberOfNotes extends Service {
		int numIncremented;
		int actualNumNotes;

		@Override
		public IBinder onBind(@Nullable Intent intent) {
			return null;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			new CheckPermissions();
			if (!CheckPermissions.getIsScreenLocked(this)) {
				SharedPref sp = new SharedPref(this);
				actualNumNotes = Integer.parseInt(sp.getSharedmPrefNumberOfNotesToPlay());
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