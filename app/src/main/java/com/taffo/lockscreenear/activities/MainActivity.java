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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.taffo.lockscreenear.BuildConfig;
import com.taffo.lockscreenear.fragments.EarTrainingFragment;
import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.services.DiapasonService;
import com.taffo.lockscreenear.utils.Utils;
import com.taffo.lockscreenear.services.LockscreenEarService;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.Updater;
import com.taffo.lockscreenear.utils.XMLParser;

import java.util.Objects;

public final class MainActivity extends AppCompatActivity {
    private Utils ut;
    private SharedPref sp;
    private SwitchCompat startSwitch;
    private AutoCompleteTextView numberInput;
    private Button buttonTraining;
    private SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;
    private SharedPreferences.OnSharedPreferenceChangeListener listenerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ut = new Utils();
        sp = new SharedPref(this);

        if (savedInstanceState == null) {
            //Dialog showed until user presses ok (necessary condition in order to start the service)
            //See also Utils
            if (sp.getSharedmPrefFirstRunMain()) {
                sp.setSharedmPrefLastUpdateVersionCode(BuildConfig.VERSION_CODE);
                //Enable device administrator component if android's user is lower than 9
                //in order to lock the screen instead of using accessibility settings
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                    getPackageManager().setComponentEnabledSetting(
                            new ComponentName(this, DeviceAdminActivity.DeviceAdminActivityReceiver.class),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                //This dialog will be displayed when this activity is created only
                //if "OK" was not pressed
                //and if there is are no saved instances of this activity
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.launcher)
                        .setTitle(getString(R.string.warnings_title))
                        .setMessage(Html.fromHtml(getString(R.string.warnings_message_html), Html.FROM_HTML_MODE_LEGACY))
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> sp.setSharedmPrefFirstRunMain(false))
                        .setCancelable(false)
                        .create()
                        .show();
            }

            if (sp.getSharedmPrefLastUpdateVersionCode() < BuildConfig.VERSION_CODE) {
                //If there has been an update, display a dialog with its features
                sp.setSharedmPrefLastUpdateVersionCode(BuildConfig.VERSION_CODE);
                View alertView = View.inflate(this, R.layout.update_features, null);
                TextView versionNameTextView = alertView.findViewById(R.id.versionNameTextView);
                versionNameTextView.setText(
                        Html.fromHtml("<b>" + getString(R.string.ver) + BuildConfig.VERSION_NAME + "</b>", Html.FROM_HTML_MODE_LEGACY));
                TextView updateFeaturesTextView = alertView.findViewById(R.id.updateFeaturesTextView);
                if (!getString(R.string.update_features).isEmpty()) {
                    updateFeaturesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    updateFeaturesTextView.setText(getString(R.string.update_features));
                }
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.launcher)
                        .setTitle(getString(R.string.update_features_title))
                        .setView(alertView)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .create()
                        .show();
            }
            //Checks for updates
            new Updater().update(this, false, null);
        }

        startSwitch = findViewById(R.id.startSwitch);
        numberInput = findViewById(R.id.numberInput);
        buttonTraining = findViewById(R.id.trainingButton);
        Button buttonDiapason = findViewById(R.id.diapasonButton);

        //Sets the back button into the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        //Registers listeners
        listenerNotes = (prefs, key) -> {
            if (prefs.equals(sp.getmPrefNotes())) {
                numberInput.setText(sp.getSharedmPrefNumberOfNotesToPlay());
                //The array of notes must also be set here, otherwise it would collapse to 1 item
                initializeAdapter();
            }
        };
        sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);

        listenerService = (prefs, key) -> {
            if (ut.checkPermissions(this) && prefs.equals(sp.getmPrefService()))
                startSwitch.setChecked(sp.getSharedmPrefService());
        };
        sp.getmPrefService().registerOnSharedPreferenceChangeListener(listenerService);

        //Checks permissions
        startSwitch.setOnClickListener(v -> ut.askPermissions(this));

        //Starts/finishes the service
        startSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (ut.checkPermissions(this)) {
                sp.setSharedmPrefService(isChecked);
                if (sp.getSharedmPrefService())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        startForegroundService(new Intent(this, LockscreenEarService.class));
                    else
                        startService(new Intent(this, LockscreenEarService.class));
                else
                    stopService(new Intent(this, LockscreenEarService.class));
            } else
                startSwitch.setChecked(false);
        });

        //Redirects to accessibility setting's page
        startSwitch.setOnLongClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return true;
        });

        //Sets initial value (see also onResume)
        numberInput.setText(sp.getSharedmPrefNumberOfNotesToPlay());
        numberInput.setInputType(InputType.TYPE_NULL);
        numberInput.setOnItemClickListener((parent, view, position, id) ->
                sp.setSharedmPrefNumberOfNotesToPlay(String.valueOf(position + 1))); //First position is 0, but it must start with 1

        buttonTraining.setOnClickListener(v -> {
            XMLParser parser = new XMLParser();
            if (parser.parseXmlNotes(this)) {
                buttonTraining.setClickable(false);
                parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out_fast)
                        .replace(R.id.activity_main, new EarTrainingFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        buttonDiapason.setOnClickListener(v -> startService(new Intent(this, DiapasonService.class)));

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService(new Intent(this, DiapasonService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //The array of notes must also (see above) be set here, otherwise it would not show
        initializeAdapter();
        if (ut.checkPermissions(this))
            startSwitch.setChecked(sp.getSharedmPrefService());
        else
            startSwitch.setChecked(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, DiapasonService.class));
        sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
        sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerService);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings)
            startActivity(new Intent(this, SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return super.onOptionsItemSelected(item);
    }

    public void initializeAdapter() {
        if (sp.getSharedmPrefEasterEggChallengeCompleted())
            numberInput.setAdapter(new customAdapter(this, R.layout.dropdown_text_input_layout,
                    getResources().getStringArray(R.array.array_start_service_array_number_of_notes))); //8
        else
            numberInput.setAdapter(new customAdapter(this, R.layout.dropdown_text_input_layout,
                    getResources().getStringArray(R.array.array_lock_screen_on_boot_array_number_of_notes))); //5
    }

    public void setButtonTrainingClickable() {
        buttonTraining.setClickable(true);
    }

    //A custom class is needed to override the getView method
    private class customAdapter extends ArrayAdapter<String> {
        private final Context mContext;

        public customAdapter(Context context, int dropdown_text_input_layout, String[] stringArray) {
            super(context, dropdown_text_input_layout, stringArray);
            mContext = context;
        }

        //Shows the current number of notes to play colored in green
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView item = view.findViewById(R.id.item);

            if (Integer.parseInt(item.getText().toString())
                    > getResources().getStringArray(R.array.array_lock_screen_on_boot_array_number_of_notes).length) {
                SpannableString itemPlusEasterEgg = new SpannableString(item.getText());
                itemPlusEasterEgg.setSpan(
                        new ImageSpan(mContext, R.drawable.easter_egg_icon),
                        item.getText().length() - 1,
                        item.getText().length(),
                        0);
                item.append(itemPlusEasterEgg);
            }
            if (item.getText().toString().contains(sp.getSharedmPrefNumberOfNotesToPlay())) {
                item.setTextColor((getColor(R.color.white)));
                item.setBackgroundColor(getColor(R.color.custom_launcher_background_green));
            } else if (item.getText().toString().contains("1")) { //To avoid the double coloring of the right textView and the one with text "1"
                item.setTextColor((getColor(R.color.black)));
                item.setBackgroundColor(getColor(R.color.white));
            }
            return view;
        }

    }

}