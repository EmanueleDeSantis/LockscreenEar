package com.taffo.lockscreen;

import android.app.Activity;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.taffo.lockscreen.services.LockScreenService;
import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

public class DeviceAdminActivity extends Activity {

    public static class DeviceAdminActivityReceiver extends DeviceAdminReceiver {
        @Override
        public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
            super.onEnabled(context, intent);
            if (new CheckPermissions().checkPermissions(context)) {
                new SharedPref(context).setSharedmPrefService(true);
                context.startForegroundService(new Intent(context, LockScreenService.class));
            }
        }

        @Override
        public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
            super.onDisabled(context, intent);
            new SharedPref(context).setSharedmPrefService(false);
            context.stopService(new Intent(context, LockScreenService.class));
        }

    }

}
