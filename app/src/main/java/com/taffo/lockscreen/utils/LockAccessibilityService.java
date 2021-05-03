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

import android.accessibilityservice.AccessibilityService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import android.service.quicksettings.TileService;
import android.view.accessibility.AccessibilityEvent;

public class LockAccessibilityService extends AccessibilityService {
    SharedPref sp;

    @Override
    public void onCreate() {
        CheckPermissions cp = new CheckPermissions();
        if (cp.checkPermissions(this)) {
            sp = new SharedPref(this);
            //Auto-starts the service on boot if the accessibility service is running and redirect to the MainActivity
            sp.setSharedmPrefService(true);
            startForegroundService(new Intent(this, LockScreenService.class));
            TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        sp.setSharedmPrefService(false);
        stopService(new Intent(this, LockScreenService.class));
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        super.onDestroy();
    }

    public boolean isAccessibilitySettingsOn(Context context) {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(context.getPackageName() + "/" + getClass().getName());
    }

}
