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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.services.LockScreenService;
import com.taffo.lockscreen.utils.SharedPref;
import com.taffo.lockscreen.services.EarTrainingService;

import java.util.Objects;

public final class MainActivity extends AppCompatActivity {
    private final CheckPermissions cp = new CheckPermissions();
    private SharedPref sp;
    private SwitchCompat switchStart;
    private AutoCompleteTextView numberInput;
    //Apparently listeners must be declared here (and not in onCreate) in order to work, so please ignore warnings
    private SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;
    private SharedPreferences.OnSharedPreferenceChangeListener listenerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchStart = findViewById(R.id.switchStart);
        numberInput = findViewById(R.id.numberInput);
        Button buttonTraining = findViewById(R.id.buttonTraining);

        //Sets the launcher icon into the action bar
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.launcher);

        sp = new SharedPref(this);

        //Dialog showed only at first boot
        if (sp.getSharedmPrefFirstRunMain()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.warnings_title))
                    .setMessage(Html.fromHtml(getString(R.string.warnings_message_html), Html.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> sp.setSharedmPrefFirstRunMain(false))
                    .create()
                    .show();
        }

        //Registers listeners
        listenerNotes = (prefs, key) -> {
            if (prefs.equals(sp.getmPrefNotes())) {
                numberInput.setText(sp.getSharedmPrefNumberOfNotesToPlay());
                //The array of notes must also be set here, otherwise it would collapse to 1 item
                numberInput.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_tex_tinput_layout,
                        getResources().getStringArray(R.array.number_of_notes)));
            }
        };
        sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);

        listenerService = (prefs, key) -> {
            if (cp.checkPermissions(this) && prefs.equals(sp.getmPrefService()))
                switchStart.setChecked(sp.getSharedmPrefService());
        };
        sp.getmPrefService().registerOnSharedPreferenceChangeListener(listenerService);

        //Checks permissions
        switchStart.setOnClickListener(v -> cp.askPermissions(this, this));

        //Starts/finishes the service
        switchStart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cp.checkPermissions(this)) {
                sp.setSharedmPrefService(isChecked);
                if (sp.getSharedmPrefService())
                    startForegroundService(new Intent(this, LockScreenService.class));
                else
                    stopService(new Intent(this, LockScreenService.class));
            } else
                switchStart.setChecked(false);
        });

        //Redirect to accessibility setting's page
        switchStart.setOnLongClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return true;
        });

        //Set initial value (see also onResume)
        numberInput.setText(sp.getSharedmPrefNumberOfNotesToPlay());
        numberInput.setInputType(InputType.TYPE_NULL);
        numberInput.setOnItemClickListener((parent, view, position, id) -> sp.setSharedmPrefNumberOfNotesToPlay(String.valueOf(id + 1))); //First id is 0

        buttonTraining.setOnClickListener(v -> startService(new Intent(this, EarTrainingService.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //The array of notes must also (see above) be set here, otherwise it would not show
        numberInput.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_tex_tinput_layout, getResources().getStringArray(R.array.number_of_notes)));
        if (cp.checkPermissions(this))
            switchStart.setChecked(sp.getSharedmPrefService());
        else
            switchStart.setChecked(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

}