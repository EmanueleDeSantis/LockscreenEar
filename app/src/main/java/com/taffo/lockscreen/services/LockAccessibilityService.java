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

import android.accessibilityservice.AccessibilityService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;

import android.service.quicksettings.TileService;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.taffo.lockscreen.LockScreenActivity;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

public class LockAccessibilityService extends AccessibilityService {
    SharedPref sp;
    public static LockAccessibilityService instance;

    //Locks the screen
    public void lockTheScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && instance != null)
            instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
    }

    @Override
    protected void onServiceConnected() {
        if (instance == null)
            instance = this;
        if (new CheckPermissions().checkPermissions(this)) {
            sp = new SharedPref(this);
            //Auto-starts the service on boot if the accessibility service is running
            sp.setSharedmPrefService(true);
            startForegroundService(new Intent(this, LockScreenService.class));
            TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
            lockTheScreen();
        }
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        sp.setSharedmPrefService(false);
        stopService(new Intent(this, LockScreenService.class));
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    public boolean isAccessibilitySettingsOn(Context context) {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(context.getPackageName() + "/" + getClass().getName());
    }

}
