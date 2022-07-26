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

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.utils.Utils;
import com.taffo.lockscreenear.utils.SharedPref;

public final class LockTileService extends TileService {
    private Tile tile;
    private SharedPref sp;
    private Utils ut;

    @Override
    public IBinder onBind(Intent intent) {
        requestListeningState(this, new ComponentName(this, getClass()));
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = new SharedPref(this);
        ut = new Utils();
        requestListeningState(this, new ComponentName(this, getClass()));
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateTileService();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        updateTileService();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileService();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        updateTileService();
    }

    //Starts/finishes the service
    @Override
    public void onClick() {
        super.onClick();
        tile = getQsTile();
        if (tile != null) {
            if (tile.getState() == Tile.STATE_ACTIVE)
                ut.stopTheService(this);
            else if (tile.getState() == Tile.STATE_INACTIVE) {
                ut.startTheService(this);
                //Locks the screen when the service is started via quick setting tile
                if (sp.getSharedmPrefQuickSettingEnabled())
                    LockAccessibilityService.lockTheScreen(this);
            }
        }
        updateTileService();
    }

    private void updateTileService() {
        tile = getQsTile();
        if (tile != null) {
            if (!ut.checkPermissions(this)) {
                tile.setLabel(getString(R.string.app_name) + getString(R.string.unavailable));
                tile.setState(Tile.STATE_UNAVAILABLE);
            } else {
                if (sp.getSharedmPrefService()) {
                    if (Utils.getIsScreenLocked(this)) {
                        tile.setLabel(getString(R.string.app_name) + getString(R.string.on) + sp.getSharedmPrefNumberOfNotesToPlay());
                        tile.setState(Tile.STATE_UNAVAILABLE);
                    } else {
                        tile.setLabel(getString(R.string.app_name) + getString(R.string.on) + sp.getSharedmPrefNumberOfNotesToPlay());
                        tile.setState(Tile.STATE_ACTIVE);
                    }
                } else {
                    tile.setLabel(getString(R.string.app_name) + getString(R.string.off) + sp.getSharedmPrefNumberOfNotesToPlay());
                    tile.setState(Tile.STATE_INACTIVE);
                }
            }
            tile.updateTile();
        }
    }

}