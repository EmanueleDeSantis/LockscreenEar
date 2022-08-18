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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.XMLParser;

import java.io.IOException;

public final class DiapasonService extends Service {
    private MediaPlayer mediaPlayerSingleNote;

    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPref sp = new SharedPref(this);
        XMLParser parser = new XMLParser();
        if (parser.parseXmlNotes(this))
            mediaPlayerSingleNote = MediaPlayer.create(this,
                    getResources().getIdentifier(parser.getDocum().getElementById(String.valueOf(27)) //La4
                            .getElementsByTagName("sound_name").item(0).getTextContent(), "raw", getPackageName()));
        else {
            sp.setSharedmPrefService(false);
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startDiapason();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayerSingleNote != null) {
            mediaPlayerSingleNote.stop();
            mediaPlayerSingleNote.release();
        }
        super.onDestroy();
    }

    private void startDiapason() {
        if (mediaPlayerSingleNote != null) {
            mediaPlayerSingleNote.stop();

            try {
                mediaPlayerSingleNote.prepare();
            } catch (IOException ignored) {
                stopSelf();
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }

            mediaPlayerSingleNote.start();
        }
    }

}