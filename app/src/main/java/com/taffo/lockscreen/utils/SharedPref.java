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

import com.taffo.lockscreen.R;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private final Context mContext;
    private final SharedPreferences mPrefNotes;
    private final SharedPreferences mPrefService;
    private final SharedPreferences mPrefRun;

    public SharedPref(Context context) {
        mContext = context;
        mPrefNotes = context.getSharedPreferences(mContext.getString(R.string.numb_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefService = context.getSharedPreferences(mContext.getString(R.string.start_service_shared_pref), Context.MODE_PRIVATE);
        mPrefRun = context.getSharedPreferences(mContext.getString(R.string.first_run_shared_pref), Context.MODE_PRIVATE);
    }

    public SharedPreferences getmPrefNotes() {
        return mPrefNotes;
    }
    public SharedPreferences getmPrefService() {
        return mPrefService;
    }
    public SharedPreferences getmPrefRun() {
        return mPrefRun;
    }

    public String getSharedmPrefNotes() {
        return mPrefNotes.getString(mContext.getString(R.string.numb_of_notes_to_play_shared_pref), "3");
    }
    public boolean getSharedmPrefService() {
        return mPrefService.getBoolean(mContext.getString(R.string.start_service_shared_pref), false);
    }
    public boolean getSharedmPrefRun() {
        return mPrefService.getBoolean(mContext.getString(R.string.first_run_shared_pref), false);
    }

    public void setSharedmPrefNotes(String string) {
        SharedPreferences.Editor editor = mPrefNotes.edit();
        editor.putString(mContext.getString(R.string.numb_of_notes_to_play_shared_pref), string);
        editor.apply();
    }
    public void setSharedmPrefService(Boolean b) {
        SharedPreferences.Editor editor = mPrefService.edit();
        editor.putBoolean(mContext.getString(R.string.start_service_shared_pref), b);
        editor.apply();
    }
    public void setSharedmPrefRun(Boolean b) {
        SharedPreferences.Editor editor = mPrefService.edit();
        editor.putBoolean(mContext.getString(R.string.first_run_shared_pref), b);
        editor.apply();
    }

}
