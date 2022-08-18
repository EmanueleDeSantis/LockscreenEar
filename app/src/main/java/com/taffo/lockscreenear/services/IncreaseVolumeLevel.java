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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.Utils;

public class IncreaseVolumeLevel extends Service {
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (!(Utils.getIsScreenLocked(this) && new SharedPref(this).getSharedmPrefVolumeAdjusterServiceSetting())) {
            int maxVolLev = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int actualVolLev = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int increase = maxVolLev / 5;
            int levIncreased;

            //Increase logic
            if (maxVolLev - actualVolLev >= increase)
                levIncreased = actualVolLev + increase;
            else
                levIncreased = increase;

            audio.setStreamVolume(AudioManager.STREAM_MUSIC, levIncreased, 0);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
