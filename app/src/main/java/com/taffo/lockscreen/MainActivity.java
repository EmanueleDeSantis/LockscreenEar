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

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.taffo.lockscreen.utils.CheckPermissions;
import com.taffo.lockscreen.utils.LockScreenService;
import com.taffo.lockscreen.utils.LockTileService;
import com.taffo.lockscreen.utils.SharedPref;

public class MainActivity extends AppCompatActivity {
    final CheckPermissions cp = new CheckPermissions();
    SharedPref sp;
    @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchGrant;
    EditText numberInput;
    static SharedPreferences.OnSharedPreferenceChangeListener listenerService;
    static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        switchGrant = findViewById(R.id.switchGrant);
        numberInput = findViewById(R.id.notesToOutput);

        sp = new SharedPref(this);

        //Registers the listeners

        listenerNotes = (prefs, key) -> {
            if (prefs.equals(sp.getmPrefNotes()))
                numberInput.setText(sp.getSharedmPrefNotes());
        };
        sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);

        listenerService = (prefs, key) -> {
            if (cp.checkPermissions(this) && prefs.equals(sp.getmPrefService()))
                switchGrant.setChecked(sp.getSharedmPrefService());
        };
        sp.getmPrefService().registerOnSharedPreferenceChangeListener(listenerService);

        //Checks the permissions
        switchGrant.setOnClickListener(v -> cp.askPermissions(this));

        //Starts/finishes the service
        switchGrant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cp.checkPermissions(this)) {
                sp.setSharedmPrefService(isChecked);
                if (sp.getSharedmPrefService())
                    startForegroundService(new Intent(this, LockScreenService.class));
                else
                    stopService(new Intent(this, LockScreenService.class));
            } else
                switchGrant.setChecked(false);
            TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        });

        //Redirect to the accessibility settings's page
        switchGrant.setOnLongClickListener(v -> {
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
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (cp.checkPermissions(this))
            switchGrant.setChecked(sp.getSharedmPrefService());
        else
            switchGrant.setChecked(false);
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (cp.checkPermissions(this))
            switchGrant.setChecked(sp.getSharedmPrefService());
        else
            switchGrant.setChecked(false);
        super.onResume();
    }

}