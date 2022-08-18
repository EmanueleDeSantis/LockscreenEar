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
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class IncreaseVolumeAdjusterMode extends Service {
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPref sp = new SharedPref(this);

        //Increase logic
        try {
            JSONObject object = new JSONObject(sp.getSharedmPrefVolumeAdjusterModeServiceSetting());
            switch (object.getString("mode")) {
                case "quiet":
                    sp.setSharedmPrefVolumeAdjusterModeServiceSetting(getResources().getStringArray(R.array.array_volume_adjuster_mode_values)[1]);
                    break;
                case "normal":
                    sp.setSharedmPrefVolumeAdjusterModeServiceSetting(getResources().getStringArray(R.array.array_volume_adjuster_mode_values)[2]);
                    break;
                case "loud":
                    sp.setSharedmPrefVolumeAdjusterModeServiceSetting(getResources().getStringArray(R.array.array_volume_adjuster_mode_values)[0]);
                    break;
            }
        } catch (JSONException ignored) {
            //Default mode
            sp.setSharedmPrefVolumeAdjusterModeServiceSetting(getResources().getStringArray(R.array.array_volume_adjuster_mode_values)[2]);
        }

        new Utils().startTheService(this); //Updates mode

        return super.onStartCommand(intent, flags, startId);
    }
}
