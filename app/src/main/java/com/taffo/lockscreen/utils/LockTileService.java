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
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.core.content.ContextCompat;

import com.taffo.lockscreen.LockScreenActivity;
import com.taffo.lockscreen.R;

import java.util.Objects;

public class LockTileService extends TileService {
    SharedPref sp;
    Tile tile;
    static boolean isLockScreenRunning;

    @Override
    public IBinder onBind(Intent intent) {
        //Update tile
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        return super.onBind(intent);
    }

    @Override
    public void onStartListening() {
        updateTileService();
    }

    @Override
    public void onClick() {
        tileOnOff();
        super.onClick();
    }

    private void updateTileService() {
        sp = new SharedPref(this);
        try {
            LockScreenActivity lsa = new LockScreenActivity();
            isLockScreenRunning = lsa.isLockScreenRunning();

        } catch (Exception e) {
            isLockScreenRunning = false;
        }
        LockAccessibilityService las = new LockAccessibilityService();

        tile = getQsTile();
        if (!isLockScreenRunning && !((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()) {
            tile.setIcon(Icon.createWithResource(this, R.drawable.unlocked_icon));
            if (!las.isAccessibilitySettingsOn(this)
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || !Settings.canDrawOverlays(this)) {
                tile.setLabel(getString(R.string.app_name) + " off");
                tile.setState(Tile.STATE_UNAVAILABLE);
            } else {
                if (sp.getSharedmPrefService()) {
                    tile.setLabel(getString(R.string.app_name) + " on");
                    tile.setState(Tile.STATE_ACTIVE);
                }
                else {
                    tile.setLabel(getString(R.string.app_name) + " off");
                    tile.setState(Tile.STATE_INACTIVE);
                }
            }
        } else {
            tile.setIcon(Icon.createWithResource(this, R.drawable.locked_icon));
            tile.setState(Tile.STATE_UNAVAILABLE);
        }
        tile.updateTile();
    }

    private void tileOnOff() {
        if (tile.getState() == Tile.STATE_ACTIVE) {
            tile.setState(Tile.STATE_INACTIVE);
            sp.setSharedmPrefService(false);
            stopService(new Intent(this, LockScreenService.class));
        } else if (tile.getState() == Tile.STATE_INACTIVE) {
            tile.setState(Tile.STATE_ACTIVE);
            sp.setSharedmPrefService(true);
            startForegroundService(new Intent(this, LockScreenService.class));
        }
        tile.updateTile();
    }

}