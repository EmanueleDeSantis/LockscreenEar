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

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;

import com.taffo.lockscreen.R;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

public final class LockTileService extends TileService {
    Tile tile;
    SharedPref sp;
    CheckPermissions cp;

    @Override
    public IBinder onBind(@Nullable Intent intent) {
        //Updates the tile
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        return super.onBind(intent);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateTileService();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileService();
    }

    //Starts/finishes the service
    @Override
    public void onClick() {
        super.onClick();
        tile = getQsTile();
        if (tile.getState() == Tile.STATE_ACTIVE) {
            tile.setState(Tile.STATE_INACTIVE);
            sp.setSharedmPrefService(false);
            stopService(new Intent(this, LockScreenService.class));
        } else if (tile.getState() == Tile.STATE_INACTIVE) {
            tile.setState(Tile.STATE_ACTIVE);
            sp.setSharedmPrefService(true);
            startForegroundService(new Intent(this, LockScreenService.class));
            //Locks the screen when the service is started via quick setting tile
            if (sp.getSharedmPrefQuickSettingSwitchEnabled())
                new LockAccessibilityService().lockTheDevice();
        }
        tile.updateTile();
        updateTileService();
    }

    //Sets the correct state of the tile
    private void updateTileService() {
        sp = new SharedPref(this);
        cp = new CheckPermissions();
        tile = getQsTile();
        if (cp.getIsScreenLocked(this)) {
            tile.setLabel(getString(R.string.app_name) + getString(R.string.on) + sp.getSharedmPrefNumberOfNotesToPlay());
            tile.setState(Tile.STATE_UNAVAILABLE);
        } else {
            if (!cp.checkPermissions(this)) {
                tile.setLabel(getString(R.string.app_name) + getString(R.string.unavailable));
                tile.setState(Tile.STATE_UNAVAILABLE);
            } else {
                if (sp.getSharedmPrefService()) {
                    tile.setLabel(getString(R.string.app_name) + getString(R.string.on) + sp.getSharedmPrefNumberOfNotesToPlay());
                    tile.setState(Tile.STATE_ACTIVE);
                } else {
                    tile.setLabel(getString(R.string.app_name) + getString(R.string.off) + sp.getSharedmPrefNumberOfNotesToPlay());
                    tile.setState(Tile.STATE_INACTIVE);
                }
            }
        }
        tile.updateTile();
    }

}