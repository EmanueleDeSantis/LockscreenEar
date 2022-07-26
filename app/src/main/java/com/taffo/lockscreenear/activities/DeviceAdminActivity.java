package com.taffo.lockscreenear.activities;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.TileService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.taffo.lockscreenear.services.LockTileService;
import com.taffo.lockscreenear.utils.Utils;

public final class DeviceAdminActivity extends AppCompatActivity {
    public final static class DeviceAdminActivityReceiver extends DeviceAdminReceiver {
        @Override
        public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
            super.onEnabled(context, intent);
            Utils ut = new Utils();
            if (ut.checkPermissions(context))
                ut.startTheService(context);
        }

        @Override
        public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
            super.onDisabled(context, intent);
            new Utils().stopTheService(context);
            TileService.requestListeningState(context, new ComponentName(context, LockTileService.class));
        }

    }

    public static void adminLockTheScreen(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm.isAdminActive(new ComponentName(context, DeviceAdminActivity.DeviceAdminActivityReceiver.class)))
            dpm.lockNow();
    }

}
