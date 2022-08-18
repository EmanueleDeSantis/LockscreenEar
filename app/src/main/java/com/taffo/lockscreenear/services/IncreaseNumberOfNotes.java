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

public final class IncreaseNumberOfNotes extends Service {
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPref sp = new SharedPref(this);

        if (!Utils.getIsScreenLocked(this)) {
            int maxNumNotes;
            int actualNumNotes = Integer.parseInt(sp.getSharedmPrefNumberOfNotesToPlay());
            int numIncreased;

            //Increase logic
            if (sp.getSharedmPrefEasterEggChallengeStarted())
                maxNumNotes = getResources().getStringArray(R.array.array_start_service_array_number_of_notes).length;
            else
                maxNumNotes = getResources().getStringArray(R.array.array_lock_screen_on_boot_array_number_of_notes).length;

            if (actualNumNotes < maxNumNotes)
                numIncreased = actualNumNotes + 1;
            else
                numIncreased = 1;
            sp.setSharedmPrefNumberOfNotesToPlay(String.valueOf(numIncreased));
        }
        return super.onStartCommand(intent, flags, startId);
    }

}