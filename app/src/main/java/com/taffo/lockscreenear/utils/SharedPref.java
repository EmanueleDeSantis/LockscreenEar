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

import com.taffo.lockscreenear.R;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class SharedPref {
    private final Context mContext;
    private final SharedPreferences mPrefNumberOfNotesToPlay;
    private final SharedPreferences mPrefStartService;
    private final SharedPreferences mPrefFirstRunMain;
    private final SharedPreferences mPrefAccessibilitySettingsStartedOnBoot;
    private final SharedPreferences mPrefFirstRunTest;
    private final SharedPreferences mPrefCancelUpdateAndDontAskUntilNextUpdate;
    private final SharedPreferences mPrefLastUpdateVersionCode;
    private final SharedPreferences mPrefBootSetting;
    private final SharedPreferences mPrefBootSettingNumberOfNotesToPlay;
    private final SharedPreferences mPrefBootSettingVolume;
    private final SharedPreferences mPrefBootSettingVolumeLevel;
    private final SharedPreferences mPrefVolumeAdjusterServiceSetting;
    private final SharedPreferences mPrefVolumeAdjusterModeServiceSetting;
    private final SharedPreferences mPrefRestorePreviousVolumeServiceSetting;
    private final SharedPreferences mPrefQuickSettingEnabled;
    private final SharedPreferences mPrefCallSettingEnabled;
    private final SharedPreferences mPrefThemeSetting;
    private final SharedPreferences mPrefEasterEggChallengeStarted;
    private final SharedPreferences mPrefEasterEggChallengeNotCompleted;
    private final SharedPreferences mPrefEasterEggChallengeGuesses;

    public SharedPref(Context context) {
        mContext = context;
        mPrefNumberOfNotesToPlay = context.getSharedPreferences(
                mContext.getString(R.string.number_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefStartService = context.getSharedPreferences(
                mContext.getString(R.string.service_shared_pref), Context.MODE_PRIVATE);
        mPrefFirstRunMain = context.getSharedPreferences(
                mContext.getString(R.string.first_run_main_shared_pref), Context.MODE_PRIVATE);
        mPrefAccessibilitySettingsStartedOnBoot = context.getSharedPreferences(
                mContext.getString(R.string.accessibility_settings_started_on_boot), Context.MODE_PRIVATE);
        mPrefFirstRunTest = context.getSharedPreferences(
                mContext.getString(R.string.first_run_test_shared_pref), Context.MODE_PRIVATE);
        mPrefCancelUpdateAndDontAskUntilNextUpdate = context.getSharedPreferences(
                mContext.getString(R.string.cancel_update_and_dont_ask_until_next_update_shared_pref), Context.MODE_PRIVATE);
        mPrefBootSetting = context.getSharedPreferences(
                mContext.getString(R.string.boot_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefBootSettingNumberOfNotesToPlay = context.getSharedPreferences(
                mContext.getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref), Context.MODE_PRIVATE);
        mPrefLastUpdateVersionCode = context.getSharedPreferences(
                mContext.getString(R.string.last_update_version_code_shared_pref), Context.MODE_PRIVATE);
        mPrefBootSettingVolume = context.getSharedPreferences(
                mContext.getString(R.string.boot_setting_volume_shared_pref), Context.MODE_PRIVATE);
        mPrefBootSettingVolumeLevel = context.getSharedPreferences(
                mContext.getString(R.string.boot_setting_volume_level_shared_pref), Context.MODE_PRIVATE);
        mPrefVolumeAdjusterServiceSetting = context.getSharedPreferences(
                mContext.getString(R.string.volume_adjuster_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefVolumeAdjusterModeServiceSetting = context.getSharedPreferences(
                mContext.getString(R.string.volume_adjuster_mode_list_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefRestorePreviousVolumeServiceSetting = context.getSharedPreferences(
                mContext.getString(R.string.restore_previous_volume_level_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefQuickSettingEnabled = context.getSharedPreferences(
                mContext.getString(R.string.quick_setting_enabled_shared_pref), Context.MODE_PRIVATE);
        mPrefCallSettingEnabled = context.getSharedPreferences(
                mContext.getString(R.string.call_setting_enabled_shared_pref), Context.MODE_PRIVATE);
        mPrefThemeSetting = context.getSharedPreferences(
                mContext.getString(R.string.theme_setting_shared_pref), Context.MODE_PRIVATE);
        mPrefEasterEggChallengeStarted = context.getSharedPreferences(
                mContext.getString(R.string.easter_egg_challenge_started_shared_pref), Context.MODE_PRIVATE);
        mPrefEasterEggChallengeNotCompleted = context.getSharedPreferences(
                mContext.getString(R.string.easter_egg_challenge_not_completed_shared_pref), Context.MODE_PRIVATE);
        mPrefEasterEggChallengeGuesses = context.getSharedPreferences(
                mContext.getString(R.string.easter_egg_challenge_guesses_shared_pref), Context.MODE_PRIVATE);
    }

    //Used for listeners
    public SharedPreferences getmPrefNotes() {
        return mPrefNumberOfNotesToPlay;
    }  //used for listeners
    public SharedPreferences getmPrefService() {
        return mPrefStartService;
    }

    public String getSharedmPrefNumberOfNotesToPlay() {
        return mPrefNumberOfNotesToPlay
                .getString(mContext.getString(R.string.number_of_notes_to_play_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmPrefService() {
        return mPrefStartService
                .getBoolean(mContext.getString(R.string.service_shared_pref), false);
    }
    public boolean getSharedmPrefFirstRunMain() {
        return mPrefFirstRunMain
                .getBoolean(mContext.getString(R.string.first_run_main_shared_pref), true);
    }
    public boolean getSharedmPrefAccessibilitySettingsStartedOnBoot() {
        return mPrefAccessibilitySettingsStartedOnBoot
                .getBoolean(mContext.getString(R.string.accessibility_settings_started_on_boot), false);
    }
    public boolean getSharedmPrefFirstRunTest() {
        return mPrefFirstRunTest
                .getBoolean(mContext.getString(R.string.first_run_test_shared_pref), true);
    }
    public String getSharedmPrefCancelUpdateAndDontAskUntilNextUpdate() {
        return mPrefCancelUpdateAndDontAskUntilNextUpdate
                .getString(mContext.getString(R.string.cancel_update_and_dont_ask_until_next_update_shared_pref),
                        mContext.getString(R.string.cancel_update_and_dont_ask_until_next_update_shared_pref));
    }
    public int getSharedmPrefLastUpdateVersionCode() {
        return mPrefLastUpdateVersionCode
                .getInt(mContext.getString(R.string.last_update_version_code_shared_pref), 1);
    }
    public boolean getSharedmPrefBootSetting() {
        return mPrefBootSetting
                .getBoolean(mContext.getString(R.string.boot_setting_shared_pref), false);
    }
    public String getSharedmPrefBootListSettingNumberOfNotesToPlay() {
        return mPrefBootSettingNumberOfNotesToPlay
                .getString(mContext.getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmPrefBootSettingVolume() {
        return mPrefBootSettingVolume
                .getBoolean(mContext.getString(R.string.boot_setting_volume_shared_pref), false);
    }
    public String getSharedmPrefBootSettingVolumeLevel() {
        return mPrefBootSettingVolumeLevel
                .getString(mContext.getString(R.string.boot_setting_volume_level_shared_pref), mContext.getString(R.string._3));
    }
    public boolean getSharedmPrefVolumeAdjusterServiceSetting() {
        return mPrefVolumeAdjusterServiceSetting
                .getBoolean(mContext.getString(R.string.volume_adjuster_setting_shared_pref), false);
    }
    public String getSharedmPrefVolumeAdjusterModeServiceSetting() {
        return mPrefVolumeAdjusterModeServiceSetting
                .getString(mContext.getString(R.string.volume_adjuster_mode_list_setting_shared_pref),
                        mContext.getString(R.string.array_item_volume_adjuster_mode_normal_string_value));
    }
    public boolean getSharedmRestorePreviousVolumeServiceSetting() {
        return mPrefRestorePreviousVolumeServiceSetting
                .getBoolean(mContext.getString(R.string.restore_previous_volume_level_setting_shared_pref), false);
    }
    public boolean getSharedmPrefQuickSettingEnabled() {
        return mPrefQuickSettingEnabled
                .getBoolean(mContext.getString(R.string.quick_setting_enabled_shared_pref), false);
    }
    public boolean getSharedmPrefCallSettingEnabled() {
        return mPrefCallSettingEnabled
                .getBoolean(mContext.getString(R.string.call_setting_enabled_shared_pref), true);
    }
    public int getSharedmPrefThemeSetting() {
        return mPrefThemeSetting
                .getInt(mContext.getString(R.string.theme_setting_shared_pref), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
    public boolean getSharedmPrefEasterEggChallengeStarted() {
        return mPrefEasterEggChallengeStarted
                .getBoolean(mContext.getString(R.string.easter_egg_challenge_started_shared_pref), false);
    }
    public boolean getSharedmPrefEasterEggChallengeNotCompleted() {
        return mPrefEasterEggChallengeNotCompleted
                .getBoolean(mContext.getString(R.string.easter_egg_challenge_not_completed_shared_pref), true);
    }
    public int getSharedmPrefEasterEggChallengeGuesses() {
        return mPrefEasterEggChallengeGuesses
                .getInt(mContext.getString(R.string.easter_egg_challenge_guesses_shared_pref), 100);
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
                .putBoolean(mContext.getString(R.string.service_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefFirstRunMain(Boolean b) {
        mPrefFirstRunMain
                .edit()
                .putBoolean(mContext.getString(R.string.first_run_main_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefAccessibilitySettingsStartedOnBoot(Boolean b) {
        mPrefAccessibilitySettingsStartedOnBoot
                .edit()
                .putBoolean(mContext.getString(R.string.accessibility_settings_started_on_boot), b)
                .apply();
    }
    public void setSharedmPrefFirstRunTest(Boolean b) {
        mPrefFirstRunTest
                .edit()
                .putBoolean(mContext.getString(R.string.first_run_test_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefCancelUpdateAndDontAskUntilNextUpdate(String s) {
        mPrefCancelUpdateAndDontAskUntilNextUpdate
                .edit()
                .putString(mContext.getString(R.string.cancel_update_and_dont_ask_until_next_update_shared_pref), s)
                .apply();
    }
    public void setSharedmPrefLastUpdateVersionCode(int i) {
        mPrefLastUpdateVersionCode
                .edit()
                .putInt(mContext.getString(R.string.last_update_version_code_shared_pref), i)
                .apply();
    }
    public void setSharedmPrefBootSetting(Boolean b) {
        mPrefBootSetting
                .edit()
                .putBoolean(mContext.getString(R.string.boot_setting_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefBootListSettingNumberOfNotesToPlay(String s) {
        mPrefBootSettingNumberOfNotesToPlay
                .edit()
                .putString(mContext.getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref), s)
                .apply();
    }
    public void setSharedmPrefBootSettingVolume(Boolean b) {
        mPrefBootSettingVolume
                .edit()
                .putBoolean(mContext.getString(R.string.boot_setting_volume_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefBootSettingVolumeLevel(String s) {
        mPrefBootSettingVolumeLevel
                .edit()
                .putString(mContext.getString(R.string.boot_setting_volume_level_shared_pref), s)
                .apply();
    }
    public void setSharedmPrefVolumeAdjusterServiceSetting(Boolean b) {
        mPrefVolumeAdjusterServiceSetting
                .edit()
                .putBoolean(mContext.getString(R.string.volume_adjuster_setting_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefVolumeAdjusterModeServiceSetting(String s) {
        mPrefVolumeAdjusterModeServiceSetting
                .edit()
                .putString(mContext.getString(R.string.volume_adjuster_mode_list_setting_shared_pref), s)
                .apply();
    }
    public void setSharedmRestorePreviousVolumeServiceSetting(Boolean b) {
       mPrefRestorePreviousVolumeServiceSetting
               .edit()
               .putBoolean(mContext.getString(R.string.restore_previous_volume_level_setting_shared_pref), b)
               .apply();
    }
    public void setSharedmPrefQuickSettingEnabled(Boolean b) {
        mPrefQuickSettingEnabled
                .edit()
                .putBoolean(mContext.getString(R.string.quick_setting_enabled_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefCallSettingEnabled(Boolean b) {
        mPrefCallSettingEnabled
                .edit()
                .putBoolean(mContext.getString(R.string.call_setting_enabled_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefThemeSetting(int i) {
        mPrefThemeSetting
                .edit()
                .putInt(mContext.getString(R.string.theme_setting_shared_pref), i)
                .apply();
    }
    public void setSharedmPrefEasterEggChallengeStarted(Boolean b) {
        mPrefEasterEggChallengeStarted
                .edit()
                .putBoolean(mContext.getString(R.string.easter_egg_challenge_started_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefEasterEggChallengeNotCompleted(Boolean b) {
        mPrefEasterEggChallengeNotCompleted
                .edit()
                .putBoolean(mContext.getString(R.string.easter_egg_challenge_not_completed_shared_pref), b)
                .apply();
    }
    public void setSharedmPrefEasterEggChallengeGuesses(int i) {
        mPrefEasterEggChallengeGuesses
                .edit()
                .putInt(mContext.getString(R.string.easter_egg_challenge_guesses_shared_pref), i)
                .apply();
    }

}
