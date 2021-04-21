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

package com.taffo.lockscreen.utils;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.taffo.lockscreen.LockScreenActivity;

import java.util.Objects;

public class IncrementNumberOfNotes extends BroadcastReceiver {
    int numIncremented;
    int actualNumNotes;
    boolean isLockScreenRunning;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LockScreenActivity lsa = new LockScreenActivity();
            isLockScreenRunning = lsa.isLockScreenRunning();

        } catch (Exception e) {
            isLockScreenRunning = false;
        }

        if (!isLockScreenRunning && !((KeyguardManager) Objects.requireNonNull(context.getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()) {
            SharedPref sp = new SharedPref(context);
            actualNumNotes = Integer.parseInt(sp.getSharedmPrefNotes());
            if (actualNumNotes < 6)
                numIncremented = actualNumNotes + 1;
            else
                numIncremented = 1;
            sp.setSharedmPrefNotes(String.valueOf(numIncremented));
        }
    }
}
