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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.services.LockScreenService;
import com.taffo.lockscreen.services.LockTileService;
import com.taffo.lockscreen.utils.SharedPref;
import com.taffo.lockscreen.services.EarTrainingService;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    CheckPermissions cp = new CheckPermissions();
    SharedPref sp;
    @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchStart;
    EditText numberInput;
    Button buttonTraining;
    static SharedPreferences.OnSharedPreferenceChangeListener listenerService;
    static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        switchStart = findViewById(R.id.switchStart);
        numberInput = findViewById(R.id.numberInput);
        buttonTraining = findViewById(R.id.buttonTraining);

        //Sets the launcher icon into the action bar
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.launcher);

        sp = new SharedPref(this);

        if (!sp.getSharedmPrefRun()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.risk_warning)
                    .setNeutralButton(getString(R.string.ok), (dialog, which) -> sp.setSharedmPrefRun(true))
                    .create()
                    .show();
        }

        //Registers the listeners
        listenerNotes = (prefs, key) -> {
            if (prefs.equals(sp.getmPrefNotes()))
                numberInput.setText(sp.getSharedmPrefNotes());
        };
        sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);

        listenerService = (prefs, key) -> {
            if (cp.checkPermissions(this) && prefs.equals(sp.getmPrefService()))
                switchStart.setChecked(sp.getSharedmPrefService());
        };
        sp.getmPrefService().registerOnSharedPreferenceChangeListener(listenerService);

        //Checks the permissions
        switchStart.setOnClickListener(v -> cp.askPermissions(this));

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
            TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        });

        //Redirect to the accessibility settings's page
        switchStart.setOnLongClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return true;
        });

        numberInput.setText(sp.getSharedmPrefNotes());

        numberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            //Updates the number of notes in shared preferences
            @Override
            public void afterTextChanged(Editable s) {
                if (!numberInput.getText().toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) < 1 || Integer.parseInt(s.toString()) > 8)
                        Toast.makeText(getApplicationContext(), "Inserisci un intero da 1 a 8", Toast.LENGTH_SHORT).show();
                    else
                        sp.setSharedmPrefNotes(s.toString());
                }
            }
        });

        buttonTraining.setOnClickListener(v -> sendBroadcast(new Intent("earTraining")));

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        startService(new Intent(this, EarTrainingService.class));
        if (cp.checkPermissions(this))
            switchStart.setChecked(sp.getSharedmPrefService());
        else
            switchStart.setChecked(false);
        super.onStart();
    }

    @Override
    protected void onResume() {
        startService(new Intent(this, EarTrainingService.class));
        if (cp.checkPermissions(this))
            switchStart.setChecked(sp.getSharedmPrefService());
        else
            switchStart.setChecked(false);
        super.onResume();
    }

}