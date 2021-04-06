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

import android.Manifest;
import android.accessibilityservice.AccessibilityService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import android.service.quicksettings.TileService;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.content.ContextCompat;

public class LockAccessibilityService extends AccessibilityService {
    SharedPref sp;

    @Override
    public void onCreate() {
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                 && Settings.canDrawOverlays(this)) {
            sp = new SharedPref(this);
            sp.setSharedmPrefService(true);
            startForegroundService(new Intent(this, LockScreenService.class));
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
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        sp.setSharedmPrefService(false);
        stopService(new Intent(this, LockScreenService.class));
        super.onDestroy();
    }

    public boolean isAccessibilitySettingsOn(Context context) {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString != null && prefString.contains(context.getPackageName() + "/" + getClass().getName());
    }

}
