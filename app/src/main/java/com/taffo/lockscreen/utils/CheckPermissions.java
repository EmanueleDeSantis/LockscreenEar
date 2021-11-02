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
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.Html;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.taffo.lockscreen.DeviceAdminActivity;
import com.taffo.lockscreen.R;
import com.taffo.lockscreen.services.LockAccessibilityService;

public final class CheckPermissions {
    private final LockAccessibilityService las = new LockAccessibilityService();
    private static boolean isLockScreenRunning = false;
    private static boolean isCallLive = false;

    public boolean checkPermissions(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && Settings.System.canWrite(context)
                && ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE))
                            .isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class))
                && las.isAccessibilitySettingsOn(context)
                && !new SharedPref(context).getSharedmPrefFirstRunMain()
                && !isCallLive);
    }

    public void askPermissions(AppCompatActivity activity, Context context) {
        SharedPref sp = new SharedPref(context);
        if (!Settings.System.canWrite(context))
            context.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName())));
        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 11);
        else if (!((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE))
                    .isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)))
            context.startActivity(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)));
        else if (!las.isAccessibilitySettingsOn(context))
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        else if (sp.getSharedmPrefFirstRunMain())
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.warnings_title))
                    .setMessage(Html.fromHtml(context.getString(R.string.warnings_message_html), Html.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> sp.setSharedmPrefFirstRunMain(false))
                    .setOnDismissListener(dialogInterface -> sp.setSharedmPrefService(checkPermissions(context)))
                    .create()
                    .show();
    }

    public static void setIsCallLive(boolean b) {
        isCallLive = b;
    }
    public static void setIsLockScreenRunning(boolean b) {
        isLockScreenRunning = b;
    }
    public static boolean getIsLockScreenRunning() {
        return isLockScreenRunning;
    }
    public static boolean getIsScreenLocked(Context context) {
        return isLockScreenRunning || ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();
    }

}
