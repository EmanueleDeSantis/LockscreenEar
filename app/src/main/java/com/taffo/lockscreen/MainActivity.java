/*
Copyright [2021] [Emanuele De Santis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.taffo.lockscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.taffo.lockscreen.utils.LockAccessibilityService;
import com.taffo.lockscreen.utils.LockScreenService;
import com.taffo.lockscreen.utils.LockTileService;
import com.taffo.lockscreen.utils.SharedPref;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switchGrant;
    EditText numberInput;
    SharedPref sp;
    LockAccessibilityService las;
    static SharedPreferences.OnSharedPreferenceChangeListener listenerService;
    static SharedPreferences.OnSharedPreferenceChangeListener listenerNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        setContentView(R.layout.activity_main);
        switchGrant = findViewById(R.id.switchGrant);
        numberInput = findViewById(R.id.notes_to_output);

        sp = new SharedPref(this);
        las = new LockAccessibilityService();

        //Register listeners
        listenerNotes = (prefs, key) -> {
            if (prefs.equals(sp.getmPrefNotes()))
                numberInput.setText(sp.getSharedmPrefNotes());
        };
        sp.getmPrefNotes().registerOnSharedPreferenceChangeListener(listenerNotes);

        listenerService = (prefs, key) -> {
            if (las.isAccessibilitySettingsOn(this)
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && Settings.canDrawOverlays(this)) {
                if (prefs.equals(sp.getmPrefService()))
                    switchGrant.setChecked(sp.getSharedmPrefService());
            }
        };
        sp.getmPrefService().registerOnSharedPreferenceChangeListener(listenerService);

        switchGrant.setOnClickListener(v -> {
            switchGrant.setChecked(sp.getSharedmPrefService());
            checkPermissions();
        });

        switchGrant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (las.isAccessibilitySettingsOn(this)
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && Settings.canDrawOverlays(this)) {
                if (buttonView == switchGrant)
                    sp.setSharedmPrefService(isChecked);
                checkService();
            } else
                switchGrant.setChecked(false);
        });

        switchGrant.setOnLongClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
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
            //Update the number of notes in shared preferences
            @Override
            public void afterTextChanged(Editable s) {
                if (!numberInput.getText().toString().isEmpty()) {
                    if (Integer.parseInt(s.toString()) < 1 || Integer.parseInt(s.toString()) > 6)
                        Toast.makeText(getApplicationContext(), "Inserisci un intero da 1 a 6", Toast.LENGTH_SHORT).show();
                    else {
                        sp.setSharedmPrefNotes(s.toString());
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        if (las.isAccessibilitySettingsOn(this)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && Settings.canDrawOverlays(this))
            switchGrant.setChecked(sp.getSharedmPrefService());
        else
            switchGrant.setChecked(false);
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (las.isAccessibilitySettingsOn(this)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && Settings.canDrawOverlays(this))
            switchGrant.setChecked(sp.getSharedmPrefService());
        else
            switchGrant.setChecked(false);
        super.onResume();
    }

    //Unregister listeners
    @Override
    protected void onDestroy() {
        sp.getmPrefService().unregisterOnSharedPreferenceChangeListener(listenerService);
        sp.getmPrefNotes().unregisterOnSharedPreferenceChangeListener(listenerNotes);
        super.onDestroy();
    }

    //Invoked by listeners
    private void checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            switchGrant.setChecked(false);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 44);
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            switchGrant.setChecked(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 11);
        } else if (!las.isAccessibilitySettingsOn(this)) {
            switchGrant.setChecked(false);
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    //Invoked by switchGrant.setOnCheckedChangeListener()
    private void checkService() {
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        if (las.isAccessibilitySettingsOn(this)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && Settings.canDrawOverlays(this)) {
            if (sp.getSharedmPrefService()) {
                startForegroundService(new Intent(this, LockScreenService.class));
            } else
                stopService(new Intent(this, LockScreenService.class));
        }
    }

}