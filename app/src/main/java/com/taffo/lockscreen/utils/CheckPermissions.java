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
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.taffo.lockscreen.services.LockAccessibilityService;

import java.util.Objects;

public final class CheckPermissions {
    private final LockAccessibilityService las = new LockAccessibilityService();
    private static boolean isLockScreenRunning = false;

    public boolean checkPermissions(Context context) {
        return (las.isAccessibilitySettingsOn(context)
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && Settings.System.canWrite(context));
    }

    public void askPermissions(Activity activity, Context context) {
        if (!Settings.System.canWrite(context))
            context.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName())));
        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 11);
        else if (!las.isAccessibilitySettingsOn(context))
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    public void setIsLockScreenRunning(boolean b) {
        isLockScreenRunning = b;
    }
    public boolean getIsLockScreenRunning() {
        return isLockScreenRunning;
    }
    public boolean getIsScreenLocked(Context context) {
        return isLockScreenRunning || ((KeyguardManager) Objects.requireNonNull(context.getSystemService(Context.KEYGUARD_SERVICE))).isKeyguardLocked();
    }

}
