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
    private final SharedPreferences mPrefStartService;
    private final SharedPreferences mPrefFirstRunMain;
    private final SharedPreferences mPrefFirstRunAccessibilitySettings;
    private final SharedPreferences mPrefBootSwitchSetting;
    private final SharedPreferences mPrefBootListSettingNumberOfNotesToPlay;
    private final SharedPreferences mPrefVolumeAdapterServiceSetting;
    private final SharedPreferences mPrefRestorePreviousVolumeServiceSetting;
    private final SharedPreferences mPrefQuickSettingSwitchEnabled;

    public SharedPref(Context context) {
        mContext = context;
        mPrefNumberOfNotesToPlay = context.getSharedPreferences(mContext.getString(R.string.number_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefStartService = context.getSharedPreferences(mContext.getString(R.string.start_service_shared_pref), Context.MODE_PRIVATE);
        mPrefFirstRunMain = context.getSharedPreferences(mContext.getString(R.string.first_run_main_shared_pref), Context.MODE_PRIVATE);
        mPrefFirstRunAccessibilitySettings = context.getSharedPreferences(mContext.getString(R.string.first_run_accessibility_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefBootSwitchSetting = context.getSharedPreferences(mContext.getString(R.string.boot_switch_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefBootListSettingNumberOfNotesToPlay = context.getSharedPreferences(mContext.getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefVolumeAdapterServiceSetting = context.getSharedPreferences(mContext.getString(R.string.volume_adapter_switch_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefRestorePreviousVolumeServiceSetting = context.getSharedPreferences(mContext.getString(R.string.restore_previous_volume_level_switch_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefQuickSettingSwitchEnabled = context.getSharedPreferences(mContext.getString(R.string.quick_setting_switch_enabled_shared_pref), Context.MODE_PRIVATE);
    }

    //Used for listeners
    public SharedPreferences getmPrefNotes() {
        return mPrefNumberOfNotesToPlay;
    }  //used for listeners
    public SharedPreferences getmPrefService() {
        return mPrefStartService;
    }

    public String getSharedmPrefNumberOfNotesToPlay() {
        return mPrefNumberOfNotesToPlay.getString(mContext.getString(R.string.number_of_notes_to_play_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmPrefService() {
        return mPrefStartService.getBoolean(mContext.getString(R.string.start_service_shared_pref), false);
    }
    public boolean getSharedmPrefFirstRunMain() {
        return mPrefFirstRunMain.getBoolean(mContext.getString(R.string.first_run_main_shared_pref), true);
    }
    public boolean getSharedmPrefFirstRunAccessibilitySettings() {
        return mPrefFirstRunAccessibilitySettings.getBoolean(mContext.getString(R.string.first_run_accessibility_setting_shared_pref), true);
    }
    public boolean getSharedmPrefBootSwitchSetting() {
        return mPrefBootSwitchSetting.getBoolean(mContext.getString(R.string.boot_switch_setting_shared_pref), false);
    }
    public String getSharedmPrefBootListSettingNumberOfNotesToPlay() {
        return mPrefBootListSettingNumberOfNotesToPlay.getString(mContext.getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmVolumeAdapterServiceSetting() {
        return mPrefVolumeAdapterServiceSetting.getBoolean(mContext.getString(R.string.volume_adapter_switch_setting_shared_pref), false);
    }
    public boolean getSharedmRestorePreviousVolumeServiceSetting() {
        return mPrefRestorePreviousVolumeServiceSetting.getBoolean(mContext.getString(R.string.restore_previous_volume_level_switch_setting_shared_pref), false);
    }
    public boolean getSharedmPrefQuickSettingSwitchEnabled() {
        return mPrefQuickSettingSwitchEnabled.getBoolean(mContext.getString(R.string.quick_setting_switch_enabled_shared_pref), false);
    }

    public void setSharedmPrefNumberOfNotesToPlay(String s) {
        mPrefNumberOfNotesToPlay
                .edit()
                .putString(mContext.getString(R.string.number_of_notes_to_play_shared_pref), s)
                .apply();
    }
    public void setSharedmPrefService(Boolean b) {
        mPrefStartService
                .edit()
                .putBoolean(mContext.getString(R.string.start_service_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefFirstRunMain(Boolean b) {
        mPrefFirstRunMain
                .edit()
                .putBoolean(mContext.getString(R.string.first_run_main_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefFirstRunAccessibilitySettings(Boolean b) {
        mPrefFirstRunAccessibilitySettings
                .edit()
                .putBoolean(mContext.getString(R.string.first_run_accessibility_setting_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefBootSetting(Boolean b) {
        mPrefBootSwitchSetting
                .edit()
                .putBoolean(mContext.getString(R.string.boot_switch_setting_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefBootListSettingNumberOfNotesToPlay(String s) {
        mPrefBootListSettingNumberOfNotesToPlay
                .edit()
                .putString(mContext.getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref), s)
                .apply();
    }
    public void setSharedmVolumeAdapterServiceSetting(Boolean b) {
        mPrefVolumeAdapterServiceSetting
                .edit()
                .putBoolean(mContext.getString(R.string.volume_adapter_switch_setting_shared_pref), b)
                .apply();
    }
    public void setSharedmRestorePreviousVolumeServiceSetting(Boolean b) {
       mPrefRestorePreviousVolumeServiceSetting
               .edit()
               .putBoolean(mContext.getString(R.string.restore_previous_volume_level_switch_setting_shared_pref), b)
               .apply();
    }
    public void setSharedmPrefQuickSettingSwitchEnabled(Boolean b) {
        mPrefQuickSettingSwitchEnabled
                .edit()
                .putBoolean(mContext.getString(R.string.quick_setting_switch_enabled_shared_pref), b)
                .apply();
    }

}
