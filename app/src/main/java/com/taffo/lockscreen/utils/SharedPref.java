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

public final class SharedPref {
    private final Context mContext;
    private final SharedPreferences mPrefNumberOfNotesToPlay;
    private final SharedPreferences mPrefService;
    private final SharedPreferences mPrefFirstRunMain;
    private final SharedPreferences mPrefFirstRunAccessibilitySettings;
    private final SharedPreferences mPrefOnRestartSwitchSetting;
    private final SharedPreferences mPrefOnRestartListSettingNumberOfNotesToPlay;
    private final SharedPreferences mPrefVolumeAdapterServiceSetting;

    public SharedPref(Context context) {
        mContext = context;
        mPrefNumberOfNotesToPlay = context.getSharedPreferences(mContext.getString(R.string.number_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefService = context.getSharedPreferences(mContext.getString(R.string.start_service_shared_pref), Context.MODE_PRIVATE);
        mPrefFirstRunMain = context.getSharedPreferences(mContext.getString(R.string.first_run_main_shared_pref), Context.MODE_PRIVATE);
        mPrefFirstRunAccessibilitySettings = context.getSharedPreferences(mContext.getString(R.string.first_run_accessibility_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefOnRestartSwitchSetting = context.getSharedPreferences(mContext.getString(R.string.on_restart_switch_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefOnRestartListSettingNumberOfNotesToPlay = context.getSharedPreferences(mContext.getString(R.string.on_restart_list_setting_number_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefVolumeAdapterServiceSetting = context.getSharedPreferences(mContext.getString(R.string.volume_adapter_switch_setting_shared_pref), Context.MODE_PRIVATE);
    }

    //Used for listeners
    public SharedPreferences getmPrefNotes() {
        return mPrefNumberOfNotesToPlay;
    }  //used for listeners
    public SharedPreferences getmPrefService() {
        return mPrefService;
    }

    public String getSharedmPrefNumberOfNotesToPlay() {
        return mPrefNumberOfNotesToPlay.getString(mContext.getString(R.string.number_of_notes_to_play_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmPrefService() {
        return mPrefService.getBoolean(mContext.getString(R.string.start_service_shared_pref), false);
    }
    public boolean getSharedmPrefFirstRunMain() {
        return mPrefFirstRunMain.getBoolean(mContext.getString(R.string.first_run_main_shared_pref), true);
    }
    public boolean getSharedmPrefFirstRunAccessibilitySettings() {
        return mPrefFirstRunAccessibilitySettings.getBoolean(mContext.getString(R.string.first_run_accessibility_setting_shared_pref), true);
    }
    public boolean getSharedmPrefOnRestartSwitchSetting() {
        return mPrefOnRestartSwitchSetting.getBoolean(mContext.getString(R.string.on_restart_switch_setting_shared_pref), false);
    }
    public String getSharedmPrefOnRestartListSettingNumberOfNotesToPlay() {
        return mPrefOnRestartListSettingNumberOfNotesToPlay.getString(mContext.getString(R.string.on_restart_list_setting_number_of_notes_to_play_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmVolumeAdapterServiceSetting() {
        return mPrefVolumeAdapterServiceSetting.getBoolean(mContext.getString(R.string.volume_adapter_switch_setting_shared_pref), false);
    }

    public void setSharedmPrefNumberOfNotesToPlay(String s) {
        SharedPreferences.Editor editor = mPrefNumberOfNotesToPlay.edit();
        editor.putString(mContext.getString(R.string.number_of_notes_to_play_shared_pref), s);
        editor.apply();
    }
    public void setSharedmPrefService(Boolean b) {
        SharedPreferences.Editor editor = mPrefService.edit();
        editor.putBoolean(mContext.getString(R.string.start_service_shared_pref), b);
        editor.apply();
    }
    public void setSharedmPrefFirstRunMain(Boolean b) {
        SharedPreferences.Editor editor = mPrefFirstRunMain.edit();
        editor.putBoolean(mContext.getString(R.string.first_run_main_shared_pref), b);
        editor.apply();
    }
    public void setSharedmPrefFirstRunAccessibilitySettings(Boolean b) {
        SharedPreferences.Editor editor = mPrefFirstRunAccessibilitySettings.edit();
        editor.putBoolean(mContext.getString(R.string.first_run_accessibility_setting_shared_pref), b);
        editor.apply();
    }
    public void setSharedmPrefOnRestartSetting(Boolean b) {
        SharedPreferences.Editor editor = mPrefOnRestartSwitchSetting.edit();
        editor.putBoolean(mContext.getString(R.string.on_restart_switch_setting_shared_pref), b);
        editor.apply();
    }
    public void setSharedmPrefOnRestartListSettingNumberOfNotesToPlay(String s) {
        SharedPreferences.Editor editor = mPrefOnRestartListSettingNumberOfNotesToPlay.edit();
        editor.putString(mContext.getString(R.string.on_restart_list_setting_number_of_notes_to_play_shared_pref), s);
        editor.apply();
    }
    public void setSharedmVolumeAdapterServiceSetting(Boolean b) {
        SharedPreferences.Editor editor = mPrefVolumeAdapterServiceSetting.edit();
        editor.putBoolean(mContext.getString(R.string.volume_adapter_switch_setting_shared_pref), b);
        editor.apply();
    }

}
