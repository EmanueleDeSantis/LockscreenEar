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

package com.taffo.lockscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_frame_layout, new SettingsFragment())
                    .commit();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            navigateBack();
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ((Preference) (Objects.requireNonNull(findPreference(getString(R.string.about))))).setSummary(BuildConfig.VERSION_NAME);
        }

        @Override
        public void onResume() {
            super.onResume();
            requireActivity().setTitle(R.string.settings);
        }
    }

    public static class BootSettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.boot_setting, rootKey);
            Context context = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(context);
            activity.setTitle(R.string.boot_setting_title);
            SwitchPreference switchBootSetting = findPreference(getString(R.string.boot_switch_setting_shared_pref));
            ListPreference listNumberOfNotes = findPreference(getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref));

            if (!new CheckPermissions().checkPermissions(context))
                Objects.requireNonNull(switchBootSetting).setEnabled(false);

            //Saves OnRestartSetting state
            Objects.requireNonNull(switchBootSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefBootSetting((Boolean.parseBoolean(newValue.toString())));
                return true;
            });

            //Saves OnRestartNumberOfNotesToPlay
            Objects.requireNonNull(listNumberOfNotes).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefBootListSettingNumberOfNotesToPlay(newValue.toString());
                return true;
            });
        }
    }

    public static class VolumeAdapterSettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Context context = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(context);
            activity.setTitle(R.string.volume_adapter_setting_title);
            setPreferencesFromResource(R.xml.volume_adapter_setting, rootKey);
            SwitchPreference switchVolumeAdapterSetting = findPreference(getString(R.string.volume_adapter_switch_setting_shared_pref));

            if (!new CheckPermissions().checkPermissions(context))
                Objects.requireNonNull(switchVolumeAdapterSetting).setEnabled(false);

            //Asks for permissions
            Objects.requireNonNull(switchVolumeAdapterSetting).setOnPreferenceClickListener(preference -> {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    switchVolumeAdapterSetting.setChecked(false);
                }
                return true;
            });

            //Saves VolumeAdapterService state
            switchVolumeAdapterSetting.setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmVolumeAdapterServiceSetting((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }
    }

    public static class QuickSettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Context context = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(context);
            activity.setTitle(R.string.quick_setting_title);
            setPreferencesFromResource(R.xml.quick_setting, rootKey);
            SwitchPreference switchQuickSetting = findPreference(getString(R.string.quick_setting_switch_enabled_shared_pref));

            if (!new CheckPermissions().checkPermissions(context))
                Objects.requireNonNull(switchQuickSetting).setEnabled(false);

            //Saves QuickSetting state
            Objects.requireNonNull(switchQuickSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefQuickSettingSwitchEnabled((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }
    }

    public static class WarningsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            requireActivity().setTitle(R.string.warnings_title);
            setPreferencesFromResource(R.xml.warnings, rootKey);
        }
    }

    private void navigateBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else
            super.onBackPressed();
    }

}