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

public final class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
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

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        setTitle(pref.getTitle());
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.settings_frame_layout, getSupportFragmentManager().getFragmentFactory().instantiate(
                        getClassLoader(),
                        pref.getFragment()))
                .addToBackStack(null)
                .commit();
        return true;
    }



    public final static class SettingsFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private Preference removeAdmin;
        private Preference uninstallLockScreen;
        private ComponentName admin;
        private DevicePolicyManager dpm;

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

            ((Preference) (Objects.requireNonNull(findPreference(getString(R.string.about_key)))))
                    .setSummary(BuildConfig.VERSION_NAME);

            removeAdmin = findPreference(getString(R.string.deactivate_admin_key));
            uninstallLockScreen = findPreference(getString(R.string.uninstall_lockScreen_key));

            removeAdmin.setOnPreferenceClickListener(preference -> {
                if (dpm.isAdminActive(admin)) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(getString(R.string.warnings_title))
                            .setMessage(getString(R.string.deactivate_admin_message))
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> dpm.removeActiveAdmin(admin))
                            .setNegativeButton(getString(R.string.no), null)
                            //This seems the only way to update the fragment
                            //This fragment must be updated in order to check if the removal of the admin has been successful,
                            //because after the initial boot of the device, apparently the admin cannot be managed for the first 2 minutes more or less
                            .setOnDismissListener(dialogInterface -> getParentFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.settings_frame_layout, new SettingsFragment())
                                    .commit())
                            .create()
                            .show();
                }
                return true;
            });

            uninstallLockScreen.setOnPreferenceClickListener(preference -> {
                if (!dpm.isAdminActive(admin)) //Just for precaution
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
        private SwitchPreference switchBootSetting;
        private Context mContext;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.boot_setting, rootKey);
            mContext = requireContext();
            SharedPref sp = new SharedPref(mContext);

            switchBootSetting = findPreference(getString(R.string.boot_switch_setting_shared_pref));
            ListPreference listNumberOfNotes = findPreference(getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref));

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

        @Override
        public void onResume() {
            super.onResume();
            switchBootSetting.setEnabled(new CheckPermissions().checkPermissions(mContext));
            if (!new CheckPermissions().checkPermissions(mContext))
                switchBootSetting.setChecked(false);
        }
    }

    public final static class VolumeAdapterSettingFragment extends PreferenceFragmentCompat {
        private SwitchPreference switchVolumeAdapterSetting;
        SwitchPreference switchRestorePreviousVolumeLevelSetting;
        private Context mContext;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.volume_adapter_setting, rootKey);
            mContext = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(mContext);

            switchVolumeAdapterSetting = findPreference(getString(R.string.volume_adapter_switch_setting_shared_pref));
            switchRestorePreviousVolumeLevelSetting = findPreference(getString(R.string.restore_previous_volume_level_switch_setting_shared_pref));

            //Asks for permissions
            Objects.requireNonNull(switchVolumeAdapterSetting).setOnPreferenceClickListener(preference -> {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    switchVolumeAdapterSetting.setChecked(false);
                }
                return true;
            });

            //Saves VolumeAdapterSetting state
            switchVolumeAdapterSetting.setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmVolumeAdapterServiceSetting(Boolean.parseBoolean(newValue.toString()));
                if (!Boolean.parseBoolean(newValue.toString()))
                    switchRestorePreviousVolumeLevelSetting.setChecked(false);
                return true;
            });

            //Saves RestorePreviousVolumeLevelSetting state
            Objects.requireNonNull(switchRestorePreviousVolumeLevelSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmRestorePreviousVolumeServiceSetting((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            switchVolumeAdapterSetting.setEnabled(new CheckPermissions().checkPermissions(mContext));
            if (!new CheckPermissions().checkPermissions(mContext)) {
                switchVolumeAdapterSetting.setChecked(false);
                switchRestorePreviousVolumeLevelSetting.setChecked(false);
            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED)
                switchVolumeAdapterSetting.setChecked(false);
        }
    }

    public final static class QuickSettingFragment extends PreferenceFragmentCompat {
        private SwitchPreference switchQuickSetting;
        private Context mContext;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.quick_setting, rootKey);
            mContext = requireContext();
            SharedPref sp = new SharedPref(mContext);

            switchQuickSetting = findPreference(getString(R.string.quick_setting_switch_enabled_shared_pref));

            //Saves QuickSetting state
            Objects.requireNonNull(switchQuickSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefQuickSettingSwitchEnabled((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            switchQuickSetting.setEnabled(new CheckPermissions().checkPermissions(mContext));
            if (!new CheckPermissions().checkPermissions(mContext))
                switchQuickSetting.setChecked(false);
        }

    }

    public final static class WarningsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.warnings, rootKey);
        }

    }

}