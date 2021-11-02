package com.taffo.lockscreen;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.taffo.lockscreen.services.LockScreenService;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

public final class DeviceAdminActivity extends Activity {

    public final static class DeviceAdminActivityReceiver extends DeviceAdminReceiver {
        @Override
        public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
            super.onEnabled(context, intent);
            if (new CheckPermissions().checkPermissions(context)) {
                new SharedPref(context).setSharedmPrefService(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(new Intent(context, LockScreenService.class));
                else
                    context.startService(new Intent(context, LockScreenService.class));
            }
        }

        @Override
        public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
            super.onDisabled(context, intent);
            new SharedPref(context).setSharedmPrefService(false);
            context.stopService(new Intent(context, LockScreenService.class));
        }

    }

    public static void adminLockTheScreen(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm.isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)))
            dpm.lockNow();
    }

}
