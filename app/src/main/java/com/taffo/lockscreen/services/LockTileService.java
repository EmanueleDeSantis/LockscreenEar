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

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.taffo.lockscreen.R;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

import java.util.Objects;

public class LockTileService extends TileService {
    CheckPermissions cp = new CheckPermissions();
    SharedPref sp;
    Tile tile;

    @Override
    public IBinder onBind(Intent intent) {
        //Updates the tile
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        return super.onBind(intent);
    }

    @Override
    public void onTileAdded() {
        updateTileService();
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        updateTileService();
        super.onStartListening();
    }

    //Starts/finishes the service
    @Override
    public void onClick() {
        tile = getQsTile();
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
        updateTileService();
        super.onClick();
    }

    //Sets the correct state of the tile
    private void updateTileService() {
        sp = new SharedPref(this);
        tile = getQsTile();
        if (cp.getIsLockScreenRunning() || ((KeyguardManager) Objects.requireNonNull(getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked()) {
            tile.setLabel(getString(R.string.app_name) + " on");
            tile.setIcon(Icon.createWithResource(this, R.drawable.locked_icon));
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else {
            if (!cp.checkPermissions(this)) {
                tile.setLabel(getString(R.string.app_name) + " off");
                tile.setIcon(Icon.createWithResource(this, R.drawable.unlocked_icon));
                tile.setState(Tile.STATE_UNAVAILABLE);
            } else {
                if (sp.getSharedmPrefService()) {
                    tile.setLabel(getString(R.string.app_name) + " on");
                    tile.setIcon(Icon.createWithResource(this, R.drawable.locked_icon));
                    tile.setState(Tile.STATE_ACTIVE);
                } else {
                    tile.setLabel(getString(R.string.app_name) + " off");
                    tile.setIcon(Icon.createWithResource(this, R.drawable.unlocked_icon));
                    tile.setState(Tile.STATE_INACTIVE);
                }
            }
        }
        tile.updateTile();
    }

}