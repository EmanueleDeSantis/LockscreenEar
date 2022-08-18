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

package com.taffo.lockscreenear.activities;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.taffo.lockscreenear.BuildConfig;
import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.utils.Updater;
import com.taffo.lockscreenear.utils.Utils;
import com.taffo.lockscreenear.utils.SharedPref;

import java.util.Objects;

public final class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_frame_layout, new SettingsFragment())
                    .commit();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(!isTaskRoot());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        setTitle(pref.getTitle());
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.settings_frame_layout, getSupportFragmentManager().getFragmentFactory().instantiate(
                        getClassLoader(),
                        Objects.requireNonNull(pref.getFragment())))
                .addToBackStack(null)
                .commit();
        return true;
    }

    public final static class SettingsFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private SharedPref sp;
        private Preference updateVersion;
        private ListPreference themeChooser;
        private long lastClickMs = 0;
        private int clickCounter = 0;
        private ConnectivityManager connectivityManager;
        private ConnectivityManager.NetworkCallback networkCallback;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = requireContext();
            sp = new SharedPref(mContext);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            updateVersion = findPreference(getString(R.string.version_name_key));
            themeChooser = findPreference(getString(R.string.theme_setting_shared_pref));
            Preference uninstallCategory = findPreference(getString(R.string.uninstall_category_setting_key));

            Objects.requireNonNull(updateVersion).setSummary(BuildConfig.VERSION_NAME);
            updateVersion.setOnPreferenceClickListener(preference -> {
                //Check for updates
                new Updater().update(requireActivity(), mContext, true, preference);

                //Easter egg logic
                if (!Utils.checkConnectivity(mContext) && !sp.getSharedmPrefEasterEggChallengeStarted()) {
                    long currentClick = System.currentTimeMillis();
                    if (lastClickMs == 0 || currentClick - lastClickMs < 250) {
                        clickCounter++;
                        lastClickMs = currentClick;
                        if (clickCounter == 5)
                            Toast.makeText(mContext, getText(R.string.almost_easter_egg_challenge_message), Toast.LENGTH_SHORT).show();
                        else if (clickCounter == 11) {
                            new SharedPref(mContext).setSharedmPrefEasterEggChallengeStarted(true);
                            SpannableString easterEggChallengeStartedTitle = new SpannableString(
                                    getString(R.string.easter_egg_challenge_started_title) + " @");
                            easterEggChallengeStartedTitle.setSpan(
                                    new ImageSpan(mContext, R.drawable.easter_egg_icon),
                                    easterEggChallengeStartedTitle.length() - 1,
                                    easterEggChallengeStartedTitle.length(),
                                    0);
                            new AlertDialog.Builder(mContext)
                                    .setIcon(R.mipmap.launcher)
                                    .setTitle(easterEggChallengeStartedTitle)
                                    .setMessage(Html.fromHtml(getString(R.string.easter_egg_challenge_started_message_html), Html.FROM_HTML_MODE_LEGACY))
                                    .setPositiveButton((android.R.string.ok), (dialog, which) -> dialog.dismiss())
                                    .setOnDismissListener(dialogInterface -> preference.setSelectable(false)) //Prevents multiple clicks
                                    .setCancelable(false)
                                    .create()
                                    .show();
                        }
                    } else {
                        lastClickMs = 0;
                        clickCounter = 0;
                    }
                }
                return true;
            });

            CharSequence[] entryValues = new String[3];
            entryValues[0] = String.valueOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            entryValues[1] = String.valueOf(AppCompatDelegate.MODE_NIGHT_NO);
            entryValues[2] = String.valueOf(AppCompatDelegate.MODE_NIGHT_YES);

            Objects.requireNonNull(themeChooser).setEntries(getResources().getStringArray(R.array.array_themes));
            themeChooser.setEntryValues(entryValues);

            if (themeChooser.getValue() == null)
                themeChooser.setValueIndex(0);

            themeChooser.setOnPreferenceChangeListener((preference, newValue) -> {
                int value = Integer.parseInt(newValue.toString());
                AppCompatDelegate.setDefaultNightMode(value);
                sp.setSharedmPrefThemeSetting(value);
                return true;
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                Objects.requireNonNull(uninstallCategory).setVisible(false);
        }

        @Override
        public void onResume() {
            super.onResume();
            requireActivity().setTitle(R.string.settings);
            AppCompatDelegate.setDefaultNightMode(Integer.parseInt(themeChooser.getValue()));
            updateVersion.setSelectable(Utils.checkConnectivity(mContext) || !sp.getSharedmPrefEasterEggChallengeStarted());
            connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    requireActivity().runOnUiThread(() ->
                            updateVersion.setSelectable(true));
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    requireActivity().runOnUiThread(() ->
                            updateVersion.setSelectable(!sp.getSharedmPrefEasterEggChallengeStarted()));
                }
            };
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }

        @Override
        public void onPause() {
            super.onPause();
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public final static class BootSettingFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private SwitchPreference switchBootSetting;
        private SwitchPreference switchBootSettingVolume;
        private SeekBarPreference seekBarBootSettingVolume;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.boot_setting, rootKey);
            mContext = requireContext();
            SharedPref sp = new SharedPref(mContext);

            switchBootSetting = findPreference(getString(R.string.boot_setting_shared_pref));
            ListPreference listNumberOfNotes = findPreference(getString(R.string.boot_list_setting_number_of_notes_to_play_shared_pref));
            switchBootSettingVolume = findPreference(getString(R.string.boot_setting_volume_shared_pref));
            seekBarBootSettingVolume = findPreference(getString(R.string.boot_setting_volume_level_shared_pref));

            Objects.requireNonNull(seekBarBootSettingVolume)
                    .setMax(((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_MUSIC));

            //Saves BootSetting state
            Objects.requireNonNull(switchBootSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefBootSetting((Boolean.parseBoolean(newValue.toString())));
                return true;
            });

            //Saves BootSettingNumberOfNotesToPlay state
            Objects.requireNonNull(listNumberOfNotes).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefBootListSettingNumberOfNotesToPlay(newValue.toString());
                return true;
            });

            //Saves BootSettingVolume state
            Objects.requireNonNull(switchBootSettingVolume).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefBootSettingVolume((Boolean.parseBoolean(newValue.toString())));
                return true;
            });

            //Saves BootSettingVolumeLevel state
            Objects.requireNonNull(seekBarBootSettingVolume).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefBootSettingVolumeLevel(newValue.toString());
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            switchBootSetting.setEnabled(new Utils().checkPermissions(mContext));
            switchBootSettingVolume.setEnabled(new Utils().checkPermissions(mContext));
            seekBarBootSettingVolume
                    .setMax(((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            if (!new Utils().checkPermissions(mContext)) {
                switchBootSetting.setChecked(false);
                switchBootSettingVolume.setChecked(false);
            }
        }
    }

    public final static class VolumeAdapterSettingFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private SwitchPreference switchVolumeAdjusterSetting;
        private ListPreference listVolumeAdjusterModeSetting;
        private SwitchPreference switchRestorePreviousVolumeLevelSetting;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.volume_adjuster_setting, rootKey);
            mContext = requireContext();
            Activity activity = requireActivity();
            SharedPref sp = new SharedPref(mContext);

            switchVolumeAdjusterSetting = findPreference(getString(R.string.volume_adjuster_setting_shared_pref));
            listVolumeAdjusterModeSetting = findPreference(getString(R.string.volume_adjuster_mode_list_setting_shared_pref));
            switchRestorePreviousVolumeLevelSetting = findPreference(getString(R.string.restore_previous_volume_level_setting_shared_pref));

            //Asks for permissions
            Objects.requireNonNull(switchVolumeAdjusterSetting).setOnPreferenceClickListener(preference -> {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    switchVolumeAdjusterSetting.setChecked(false);
                    switchRestorePreviousVolumeLevelSetting.setChecked(false);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 11);
                    switchVolumeAdjusterSetting.setChecked(false);
                    switchRestorePreviousVolumeLevelSetting.setChecked(false);
                }
                return true;
            });

            //Saves VolumeAdjusterSetting state
            switchVolumeAdjusterSetting.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean value = Boolean.parseBoolean(newValue.toString());
                sp.setSharedmPrefVolumeAdjusterServiceSetting(value);
                return true;
            });

            //Saves VolumeAdjusterMode state
            Objects.requireNonNull(listVolumeAdjusterModeSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                sp.setSharedmPrefVolumeAdjusterModeServiceSetting(newValue.toString());
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
            SharedPref sp = new SharedPref(mContext);

            switchVolumeAdjusterSetting.setEnabled(new Utils().checkPermissions(mContext));
            if (!new Utils().checkPermissions(mContext)) {
                switchVolumeAdjusterSetting.setChecked(false);
                switchRestorePreviousVolumeLevelSetting.setChecked(false);
            } else
                switchVolumeAdjusterSetting.setChecked(sp.getSharedmPrefVolumeAdjusterServiceSetting());
            listVolumeAdjusterModeSetting.setValue(sp.getSharedmPrefVolumeAdjusterModeServiceSetting());
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED)
                switchVolumeAdjusterSetting.setChecked(false);
        }

    }

    public final static class QuickSettingFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private SwitchPreference switchQuickSetting;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.quick_setting, rootKey);
            mContext = requireContext();

            switchQuickSetting = findPreference(getString(R.string.quick_setting_enabled_shared_pref));

            //Saves QuickSetting state
            Objects.requireNonNull(switchQuickSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                new SharedPref(mContext).setSharedmPrefQuickSettingEnabled((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            switchQuickSetting.setEnabled(new Utils().checkPermissions(mContext));
            if (!new Utils().checkPermissions(mContext))
                switchQuickSetting.setChecked(false);
        }

    }

    public final static class CallSettingFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private SwitchPreference switchCallSetting;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.call_setting, rootKey);
            mContext = requireContext();

            switchCallSetting = findPreference(getString(R.string.call_setting_enabled_shared_pref));

            //Saves CallSetting state
            Objects.requireNonNull(switchCallSetting).setOnPreferenceChangeListener((preference, newValue) -> {
                new SharedPref(mContext).setSharedmPrefCallSettingEnabled((Boolean.parseBoolean(newValue.toString())));
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            switchCallSetting.setEnabled(new Utils().checkPermissions(mContext));
            if (!new Utils().checkPermissions(mContext))
                switchCallSetting.setChecked(true);
        }

    }

    public final static class WarningsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.warnings, rootKey);
        }

    }

    //Only for android lower than 9
    public final static class UninstallSettingFragment extends PreferenceFragmentCompat {
        private Context mContext;
        private Preference removeAdmin;
        private Preference uninstallLockscreenEar;
        private DevicePolicyManager dpm;
        private ComponentName admin;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.uninstall_setting, rootKey);

            mContext = requireContext();
            dpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            admin = new ComponentName(mContext, DeviceAdminActivity.DeviceAdminActivityReceiver.class);
            removeAdmin = findPreference(getString(R.string.deactivate_admin_key));
            uninstallLockscreenEar = findPreference(getString(R.string.uninstall_lockScreen_key));

            removeAdmin.setOnPreferenceClickListener(preference -> {
                if (dpm.isAdminActive(admin)) {
                    new AlertDialog.Builder(mContext)
                            .setIcon(R.mipmap.launcher)
                            .setTitle(getString(R.string.warnings_title))
                            .setMessage(getString(R.string.deactivate_admin_message))
                            .setPositiveButton((android.R.string.ok), (dialog, which) -> dpm.removeActiveAdmin(admin))
                            .setNegativeButton(android.R.string.cancel, null)
                            //This seems the only way to update the fragment from here
                            //This fragment must be updated in order to check if the removal of the admin has been successful,
                            //because after the initial boot of the device,
                            //apparently the admin cannot be managed for the first 2 minutes more or less
                            .setOnDismissListener(dialogInterface -> {
                                getParentFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.settings_frame_layout, this)
                                        .remove(this)
                                        .addToBackStack(null)
                                        .commit();
                                getParentFragmentManager().popBackStack();
                            })
                            .create()
                            .show();
                }
                return true;
            });

            uninstallLockscreenEar.setOnPreferenceClickListener(preference -> {
                if (!dpm.isAdminActive(admin)) //Just for precaution
                    startActivity(new Intent(Intent.ACTION_DELETE)
                            .setData(Uri.parse("package:" + mContext.getPackageName())));
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            removeAdmin.setEnabled(dpm.isAdminActive(admin));
            uninstallLockscreenEar.setEnabled(!dpm.isAdminActive(admin));
        }

    }

}