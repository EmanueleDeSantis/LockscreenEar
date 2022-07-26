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
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.XMLParser;

public final class DiapasonService extends Service {
    private SharedPref sp;

    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sp = new SharedPref(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startDiapason();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        super.onDestroy();
    }

    private MediaPlayer mediaPlayer;
    private void startDiapason() {
        XMLParser parser = new XMLParser();
        if (parser.parseXmlNotes(this)) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),
                    getResources().getIdentifier(parser.getDocum().getElementById("27") //La2
                            .getElementsByTagName("sound_name").item(0).getTextContent(), "raw", getPackageName()));
            mediaPlayer.start();
            //After a fixed number of execution of this function, the MediaPlayer stops working.
            //This happens because the operating system does not release the media player automatically.
            //Solved using the instruction below, that releases the media player when the sound ends
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        } else {
            sp.setSharedmPrefService(false);
            stopSelf();
        }
    }


}