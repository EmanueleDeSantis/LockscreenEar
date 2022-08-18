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

import android.accessibilityservice.AccessibilityService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.os.UserManagerCompat;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.activities.DeviceAdminActivity;
import com.taffo.lockscreenear.activities.LockscreenEarActivity;
import com.taffo.lockscreenear.activities.MainActivity;
import com.taffo.lockscreenear.utils.Utils;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.XMLParser;

public final class LockAccessibilityService extends AccessibilityService {
    private static LockAccessibilityService instance;
    private SharedPref sp;
    private Utils ut;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private Context deviceContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
                //This intent requires android:directBootAware="true" in the manifest
                //in addition to <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />.
                //This action is used in order to let LockscreenEar knows when the device is booted,
                //but SharedPreferences are not available with device encrypted storage, therefore:
                deviceContext = context.createDeviceProtectedStorageContext();
                deviceContext.moveSharedPreferencesFrom(context, context.getString(R.string.accessibility_settings_started_on_boot));
                deviceContext.getSharedPreferences(deviceContext.getString(R.string.accessibility_settings_started_on_boot), Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(deviceContext.getString(R.string.accessibility_settings_started_on_boot), true)
                        .apply();
            }
            //Then, when the user unlocks the device, both credential and device encrypted storages are available
            if (intent.getAction().equals(Intent.ACTION_USER_UNLOCKED)) {
                context.moveSharedPreferencesFrom(deviceContext, deviceContext.getString(R.string.service_shared_pref));
                context.moveSharedPreferencesFrom(deviceContext, deviceContext.getString(R.string.accessibility_settings_started_on_boot));
                sp = new SharedPref(context);
                if (ut.checkPermissions(context)) {
                    if (sp.getSharedmPrefBootSetting()) {
                        ut.startTheService(context);
                        sp.setSharedmPrefNumberOfNotesToPlay(sp.getSharedmPrefBootListSettingNumberOfNotesToPlay());
                        if (sp.getSharedmPrefBootSettingVolume())
                            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(
                                    AudioManager.STREAM_MUSIC, Integer.parseInt(sp.getSharedmPrefBootSettingVolumeLevel()), 0);

                        XMLParser parser = new XMLParser();
                        if (parser.parseXmlNotes(context)) {
                            parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
                            if (!Utils.getCallAndCallSetting(context)) { //Just in case...
                                startActivity(new Intent(context, LockscreenEarActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        } else {
                            startActivity(new Intent(context, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            ut.stopTheService(context);
                        }
                    } else if (sp.getSharedmPrefService())
                        ut.startTheService(context);
                }
                //Updates the tile
                TileService.requestListeningState(context, new ComponentName(context, LockTileService.class));
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (sp == null && UserManagerCompat.isUserUnlocked(this))
            sp = new SharedPref(this);
        ut = new Utils();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_USER_UNLOCKED);
        registerReceiver(mReceiver, filter);

        if (instance == null)
            instance = this;

        if (sp != null) {
            //Auto-starts the service
            if (ut.checkPermissions(this) && !sp.getSharedmPrefAccessibilitySettingsStartedOnBoot())
                ut.startTheService(this);

            if (sp.getSharedmPrefAccessibilitySettingsStartedOnBoot())
                sp.setSharedmPrefAccessibilitySettingsStartedOnBoot(false);

            //Updates the tile
            TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        ut.stopTheService(this);
        unregisterReceiver(mReceiver);
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
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

    //Locks the screen (used in LockTileService)
    public static void lockTheScreen(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
        else
            DeviceAdminActivity.adminLockTheScreen(context);
    }

}
