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
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.service.quicksettings.TileService;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.taffo.lockscreen.utils.SharedPref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public final class VolumeAdapterService extends Service {
    private SharedPref sp;
    private AudioManager audio;
    private static boolean areHeadPhonesPlugged = false;
    private static boolean isVolumeAdapterRunning = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent.getAction() != null) {
                if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                    int state = intent.getIntExtra("state", -1);
                    if (state == 0)
                        areHeadPhonesPlugged = false;
                    if (state == 1)
                        areHeadPhonesPlugged = true;
                }
            }
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

    //Registers the receiver for lock screen events
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, filter);
        if (sp.getSharedmVolumeAdapterServiceSetting()
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && !isVolumeAdapterRunning
                && !(!((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()
                        || Objects.requireNonNull(audio).isMusicActive()
                        || areHeadPhonesPlugged
                        || BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)))
            volumeAdapter();
        else
            stopSelf();
        return START_NOT_STICKY;
    }

    //Unregisters the receiver
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
    }

    private final int LIST_EL = 5;
    private final List<Double> dbList = new ArrayList<>(LIST_EL);
    private static int dbMean = 0;
    private final int DB_LIMIT = 120;

    private void volumeAdapter() {
        isVolumeAdapterRunning = true;
        MediaRecorder mRecorder = new MediaRecorder();
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

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()
                        || Objects.requireNonNull(audio).isMusicActive()
                        || areHeadPhonesPlugged
                        || BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    timer.cancel();
                    mRecorder.stop();
                    dbList.clear();
                    isVolumeAdapterRunning = false;
                    stopSelf();
                } else {
                    double sum = 0;
                    double db = 20 * Math.log10(mRecorder.getMaxAmplitude());
                    if (db > 0 && db < DB_LIMIT)
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
                    Objects.requireNonNull(audio).setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * dbMean / DB_LIMIT, 0);
                }
            }
        }, 0, 1000); //every second
    }

}