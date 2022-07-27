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

package com.taffo.lockscreenear.utils;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;

import androidx.appcompat.app.AlertDialog;

import com.taffo.lockscreenear.activities.DeviceAdminActivity;
import com.taffo.lockscreenear.activities.TestActivity;
import com.taffo.lockscreenear.services.LockAccessibilityService;
import com.taffo.lockscreenear.services.LockscreenEarService;
import com.taffo.lockscreenear.R;

public final class Utils {
    private static boolean isLockscreenEarRunning = false;
    private final LockAccessibilityService las = new LockAccessibilityService();

    public boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return (Settings.System.canWrite(context)
                    && las.isAccessibilitySettingsOn(context)
                    && !new SharedPref(context).getSharedmPrefFirstRunTest());
        else
            return (Settings.System.canWrite(context)
                    && ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE))
                            .isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class))
                    && las.isAccessibilitySettingsOn(context)
                    && !new SharedPref(context).getSharedmPrefFirstRunTest());
    }

    public void askPermissions(Context context) {
        if (!Settings.System.canWrite(context))
            context.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName())));
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P
                && !((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE))
                        .isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)))
            context.startActivity(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)));
        else if (!las.isAccessibilitySettingsOn(context))
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        else if (new SharedPref(context).getSharedmPrefFirstRunTest())
            new AlertDialog.Builder(context)
                    .setIcon(R.mipmap.launcher)
                    .setTitle(context.getString(R.string.test_title))
                    .setMessage(Html.fromHtml(context.getString(R.string.test_message_html), Html.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (new XMLParser().parseXmlNotes(context))
                            context.startActivity(new Intent(context, TestActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show();
    }

    public void startTheService(Context context) {
        new SharedPref(context).setSharedmPrefService(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(new Intent(context, LockscreenEarService.class));
        else
            context.startService(new Intent(context, LockscreenEarService.class));
    }

    public void stopTheService(Context context) {
        context.stopService(new Intent(context, LockscreenEarService.class));
        new SharedPref(context).setSharedmPrefService(false);
    }

    public static void setIsLockscreenEarRunning(boolean b) {
        isLockscreenEarRunning = b;
    }
    public static boolean getIsScreenLocked(Context context) {
        return isLockscreenEarRunning || ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();
    }
    public static boolean getCallAndCallSetting(Context context) {
        return new SharedPref(context).getSharedmPrefCallSettingEnabled()
                && ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).getMode() != AudioManager.MODE_NORMAL;
    }

    public static boolean checkConnectivity(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null)
                return false;
            else {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities == null)
                    return false;
                else {
                    return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
                }
            }

        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }
    }

}
