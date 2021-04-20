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
                    tile.setIcon(Icon.createWithResource(this, R.drawable.locked_icon));
                    tile.setState(Tile.STATE_ACTIVE);
                }
                else {
                    tile.setLabel(getString(R.string.app_name) + " off");
                    tile.setIcon(Icon.createWithResource(this, R.drawable.unlocked_icon));
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