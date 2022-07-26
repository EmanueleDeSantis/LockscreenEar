package com.taffo.lockscreenear.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.Utils;

public final class IncrementNumberOfNotes extends Service {
    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPref sp = new SharedPref(this);

        if (!Utils.getIsScreenLocked(this)) {
            int maxNumNotes;
            int actualNumNotes = Integer.parseInt(sp.getSharedmPrefNumberOfNotesToPlay());
            int numIncremented;

            if (sp.getSharedmPrefEasterEggChallengeStarted())
                maxNumNotes = getResources().getStringArray(R.array.array_start_service_array_number_of_notes).length;
            else
                maxNumNotes = getResources().getStringArray(R.array.array_lock_screen_on_boot_array_number_of_notes).length;

            if (actualNumNotes < maxNumNotes)
                numIncremented = actualNumNotes + 1;
            else
                numIncremented = 1;
            sp.setSharedmPrefNumberOfNotesToPlay(String.valueOf(numIncremented));
        }
        return super.onStartCommand(intent, flags, startId);
    }

}