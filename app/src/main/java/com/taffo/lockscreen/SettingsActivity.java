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
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.SharedPref;

import java.util.Objects;

public final class SettingsActivity extends AppCompatActivity {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public final static class SettingsFragment extends PreferenceFragmentCompat {
        Context mContext;
        Preference removeAdmin;
        Preference uninstallLockScreen;
        ComponentName admin;
        DevicePolicyManager dpm;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = requireContext();
            admin = new ComponentName(mContext, DeviceAdminActivity.DeviceAdminActivityReceiver.class);
            dpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            ((Preference) Objects.requireNonNull(findPreference(getString(R.string.source_code_key))))
                    .setOnPreferenceClickListener(preference -> {
                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse("https://www.github.com/EmanueleDeSantis/LockScreen")));
                        return true;
                    });

            ((Preference) (Objects.requireNonNull(findPreference(getString(R.string.about_key)))))
                    .setSummary(BuildConfig.VERSION_NAME);

            removeAdmin = findPreference(getString(R.string.deactivate_admin_key));
            uninstallLockScreen = findPreference(getString(R.string.uninstall_lockScreen_key));

            removeAdmin.setOnPreferenceClickListener(preference -> {
                if (dpm.isAdminActive(admin)) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(getString(R.string.warnings_title))
                            .setMessage(getString(R.string.deactivate_admin_message))
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                dpm.removeActiveAdmin(admin);
                                removeAdmin.setEnabled(false);
                                uninstallLockScreen.setEnabled(true);
                            })
                            .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                            .create()
                            .show();
                }
                return true;
            });

            uninstallLockScreen.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_DELETE)
                    .setData(Uri.parse("package:" + mContext.getPackageName())));
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            requireActivity().setTitle(R.string.settings);
            removeAdmin.setEnabled(dpm.isAdminActive(admin));
            uninstallLockScreen.setEnabled(!dpm.isAdminActive(admin));
        }

    }

    public final static class BootSettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.boot_setting, rootKey);
            Context mContext = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(mContext);
            activity.setTitle(R.string.boot_setting_title);
            SwitchPreference switchBootSetting = findPreference(getString(R.string.boot_switch_setting_shared_pref));
            ListPreference listNumberOfNotes = findPreference(getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref));

            if (!new CheckPermissions().checkPermissions(mContext))
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

    public final static class VolumeAdapterSettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.volume_adapter_setting, rootKey);
            Context mContext = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(mContext);
            activity.setTitle(R.string.volume_adapter_setting_title);
            SwitchPreference switchVolumeAdapterSetting = findPreference(getString(R.string.volume_adapter_switch_setting_shared_pref));

            if (!new CheckPermissions().checkPermissions(mContext))
                Objects.requireNonNull(switchVolumeAdapterSetting).setEnabled(false);

            //Asks for permissions
            Objects.requireNonNull(switchVolumeAdapterSetting).setOnPreferenceClickListener(preference -> {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
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

    public final static class QuickSettingFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.quick_setting, rootKey);
            Context mContext = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(mContext);
            activity.setTitle(R.string.quick_setting_title);

            SwitchPreference switchQuickSetting = findPreference(getString(R.string.quick_setting_switch_enabled_shared_pref));

            if (!new CheckPermissions().checkPermissions(mContext))
                Objects.requireNonNull(switchQuickSetting).setEnabled(false);

            //Saves QuickSetting state
            Objects.requireNonNull(switchQuickSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefQuickSettingSwitchEnabled((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }

    }

    public final static class WarningsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.warnings, rootKey);
            requireActivity().setTitle(R.string.warnings_title);
        }
    }

}