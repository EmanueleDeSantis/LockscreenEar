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
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.taffo.lockscreen.utils.LockScreenService;
import com.taffo.lockscreen.utils.LockTileService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LockScreenActivity extends AppCompatActivity {
    TextView tv;
    final LockScreenService lss = new LockScreenService();
    final int NOTES = lss.getNotes();
    final int TOTAL_NOTES = lss.getTotalNotes();
    static boolean isLockScreenRunning = false;

    WindowManager.LayoutParams mParams;
    WindowManager mWindowManager;
    View view;

    public boolean isLockScreenRunning() {
        return isLockScreenRunning;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isLockScreenRunning = true;
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));

        play();

        //The following overlay view can't be removed by the user in any way, except with the unlocking function
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT);
        mWindowManager = ((WindowManager) getSystemService(WINDOW_SERVICE));

        view = View.inflate(this, R.layout.activity_lockscreen, null);
        mWindowManager.addView(view, mParams);

        tv = view.findViewById(R.id.title);
        init();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            StateListener phoneStateListener = new StateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            assert telephonyManager != null;
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        super.onCreate(savedInstanceState);
    }

    //Handle calls events
    private static class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_IDLE:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    final List<String> selectedNotesList = new ArrayList<>(NOTES);
    private void init() {
        Button buttonDo = view.findViewById(R.id.buttonDo);
        Button buttonDodie = view.findViewById(R.id.buttonDodie);
        Button buttonRe = view.findViewById(R.id.buttonRe);
        Button buttonRedie = view.findViewById(R.id.buttonRedie);
        Button buttonMi = view.findViewById(R.id.buttonMi);
        Button buttonFa = view.findViewById(R.id.buttonFa);
        Button buttonFadie = view.findViewById(R.id.buttonFadie);
        Button buttonSol = view.findViewById(R.id.buttonSol);
        Button buttonSoldie = view.findViewById(R.id.buttonSoldie);
        Button buttonLa = view.findViewById(R.id.buttonLa);
        Button buttonLadie = view.findViewById(R.id.buttonLadie);
        Button buttonSi = view.findViewById(R.id.buttonSi);

        buttonDo.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("do");
            unlock();
        });
        buttonDodie.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("dodie");
            unlock();
        });
        buttonRe.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("re");
            unlock();
        });
        buttonRedie.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("redie");
            unlock();
        });
        buttonMi.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("mi");
            unlock();
        });
        buttonFa.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("fa");
            unlock();
        });
        buttonFadie.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("fadie");
            unlock();
        });
        buttonSol.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("sol");
            unlock();
        });
        buttonSoldie.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("soldie");
            unlock();
        });
        buttonLa.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("la");
            unlock();
        });
        buttonLadie.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("ladie");
            unlock();
        });
        buttonSi.setOnClickListener(v -> {
            if (selectedNotesList.size() <= NOTES)
                selectedNotesList.add("si");
            unlock();
        });

    }

    //Do nothing on back press
    @Override
    public void onBackPressed() {}

    //Support for the screen overlay: the condition used in onStop to restart the app when the user lock the screen removes the overlay. Solved with the following condition in onPause
    @Override
    protected void onPause() {
        ((ActivityManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE))).moveTaskToFront(getTaskId(), 0);
        super.onPause();
    }

    //Finishes app when user lock the screen when this Lock Screen is running
    @Override
    protected void onStop() {
        if (!isFinishing()) {
            mWindowManager.removeView(view);
            view = null;
            mWindowManager = null;
            finish();
        }
        super.onStop();
    }

    final Random rand = new Random();
    Document doc;
    Element note;
    String noteName;
    String noteSoundName;
    final String[] outputtedNotes = new String[NOTES];

    //Play random notes got from xml file "notes.xml" in "src/main/assets" folder. Notes are stored in "res/raw" folder
    private void play() {
        doc = lss.getDocum();
        for (int i = 0; i < NOTES; i++) {
            int randIdNote = rand.nextInt(TOTAL_NOTES) + 1;
            note = doc.getElementById(String.valueOf(randIdNote));
            noteName = note.getElementsByTagName("name").item(0).getTextContent();
            noteSoundName = note.getElementsByTagName("sound_name").item(0).getTextContent();
            MediaPlayer.create(this, getResources().getIdentifier(noteSoundName, "raw", getPackageName())).start();
            outputtedNotes[i] = noteName;
        }
    }

    //Unlock if user guessed all and only the outputted notes
    final List<String> outputtedNotesList = Arrays.asList(outputtedNotes);
    private void unlock() {
        if (selectedNotesList.containsAll(outputtedNotesList) && outputtedNotesList.containsAll(selectedNotesList)) {
            TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
            isLockScreenRunning = false;
            mWindowManager.removeView(view);
            view = null;
            mWindowManager = null;
            Intent changeNotif = new Intent("changeNotification");
            sendBroadcast(changeNotif);
            finish();
        }
        else if (selectedNotesList.size() >= NOTES) {
            tv.setText(getString(R.string.incorrect_entry));
            tv.setTextColor(Color.parseColor("#FFEA2A2A")); //Red
        }
    }

}