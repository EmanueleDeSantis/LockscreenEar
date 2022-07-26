package com.taffo.lockscreenear.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.Utils;

public class IncrementVolumeLevel extends Service {
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (!(Utils.getIsScreenLocked(this) && new SharedPref(this).getSharedmPrefVolumeAdapterServiceSetting())) {
            int maxVolLev = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int actualVolLev = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int levIncremented;

            if (maxVolLev - actualVolLev >= 3)
                levIncremented = actualVolLev + 3;
            else
                levIncremented = 3;

            audio.setStreamVolume(AudioManager.STREAM_MUSIC, levIncremented, 0);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
