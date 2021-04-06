/*
Copyright [2021] [Emanuele De Santis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
