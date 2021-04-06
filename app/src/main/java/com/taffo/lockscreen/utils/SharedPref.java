/*
Copyright [2021] [Emanuele De Santis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.taffo.lockscreen.utils;

import com.taffo.lockscreen.R;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private final Context mContext;
    private final SharedPreferences mPrefNotes;
    private final SharedPreferences mPrefService;

    public SharedPref(Context context) {
        mContext = context;
        mPrefNotes = context.getSharedPreferences(mContext.getString(R.string.numb_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefService = context.getSharedPreferences(mContext.getString(R.string.start_service_shared_pref), Context.MODE_PRIVATE);
    }

    public SharedPreferences getmPrefNotes() {
        return mPrefNotes;
    }
    public SharedPreferences getmPrefService() {
        return mPrefService;
    }

    public String getSharedmPrefNotes() {
        return mPrefNotes.getString(mContext.getString(R.string.numb_of_notes_to_play_shared_pref), mContext.getResources().getString(R.string.numb_of_notes_to_play_shared_pref_default_value));
    }
    public boolean getSharedmPrefService() {
        return mPrefService.getBoolean(mContext.getString(R.string.start_service_shared_pref), mContext.getResources().getBoolean(R.bool.start_service_shared_pref_default_value));
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

}
