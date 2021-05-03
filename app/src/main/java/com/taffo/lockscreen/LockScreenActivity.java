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
import androidx.core.text.HtmlCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
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
    LockScreenService lss = new LockScreenService();
    final int NOTES = lss.getNotes();
    final int TOTAL_NOTES = lss.getTotalNotes();
    final Document doc = lss.getDocum();
    static boolean isLockScreenRunning = false;

    int systemScreenOffTimeoutDefaultValue;
    final int customScreenOffTimeoutValue = 10000; //10 seconds
    TextView tv;
    WindowManager.LayoutParams mParams;
    WindowManager mWindowManager;
    View view;
    StateListener stateListener;
    TelephonyManager telephony;

    Button buttonDo;
    Button buttonDodie;
    Button buttonRe;
    Button buttonRedie;
    Button buttonMi;
    Button buttonFa;
    Button buttonFadie;
    Button buttonSol;
    Button buttonSoldie;
    Button buttonLa;
    Button buttonLadie;
    Button buttonSi;

    List<String> selectedNotesList = new ArrayList<>(NOTES);

    public boolean isLockScreenRunning() {
        return isLockScreenRunning;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isLockScreenRunning = true;
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));

        //Saves the default screen off timeout to restore it when the screen is unlocked
        try {
            systemScreenOffTimeoutDefaultValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //Sets the screen off timeout to 10 seconds
        if (Settings.System.canWrite(this))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, customScreenOffTimeoutValue);

        play();

        //The following overlay view can't be removed by the user in any way, except with the unlocking function
        if (Settings.canDrawOverlays(this)) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                    PixelFormat.TRANSLUCENT);

            mWindowManager = ((WindowManager) getSystemService(WINDOW_SERVICE));
            view = View.inflate(this, R.layout.activity_lockscreen, null);
            mWindowManager.addView(view, mParams);
        } else
            unlockAndRemoveView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            stateListener = new StateListener();
            telephony = (TelephonyManager) Objects.requireNonNull(getSystemService(Context.TELEPHONY_SERVICE));
            telephony.listen(stateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        //Initialization of view's elements
        tv = view.findViewById(R.id.title);
        buttonDo = view.findViewById(R.id.buttonDo);
        buttonDodie = view.findViewById(R.id.buttonDodie);
        buttonRe = view.findViewById(R.id.buttonRe);
        buttonRedie = view.findViewById(R.id.buttonRedie);
        buttonMi = view.findViewById(R.id.buttonMi);
        buttonFa = view.findViewById(R.id.buttonFa);
        buttonFadie = view.findViewById(R.id.buttonFadie);
        buttonSol = view.findViewById(R.id.buttonSol);
        buttonSoldie = view.findViewById(R.id.buttonSoldie);
        buttonLa = view.findViewById(R.id.buttonLa);
        buttonLadie = view.findViewById(R.id.buttonLadie);
        buttonSi = view.findViewById(R.id.buttonSi);

        buttonDo.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("do")) {
                    selectedNotesList.remove("do");
                    buttonDo.setSelected(false);
                } else {
                    selectedNotesList.add("do");
                    buttonDo.setSelected(true);
                }
                unlock();
            }
        });
        buttonDodie.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("dodie")) {
                    selectedNotesList.remove("dodie");
                    buttonDodie.setSelected(false);
                } else {
                    selectedNotesList.add("dodie");
                    buttonDodie.setSelected(true);
                }
                unlock();
            }
        });
        buttonRe.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("re")) {
                    selectedNotesList.remove("re");
                    buttonRe.setSelected(false);
                } else {
                    selectedNotesList.add("re");
                    buttonRe.setSelected(true);
                }
                unlock();
            }
        });
        buttonRedie.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("redie")) {
                    selectedNotesList.remove("redie");
                    buttonRedie.setSelected(false);
                } else {
                    selectedNotesList.add("redie");
                    buttonRedie.setSelected(true);
                }
                unlock();
            }
        });
        buttonMi.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("mi")) {
                    selectedNotesList.remove("mi");
                    buttonMi.setSelected(false);
                } else {
                    selectedNotesList.add("mi");
                    buttonMi.setSelected(true);
                }
                unlock();
            }
        });
        buttonFa.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("fa")) {
                    selectedNotesList.remove("fa");
                    buttonFa.setSelected(false);
                } else {
                    selectedNotesList.add("fa");
                    buttonFa.setSelected(true);
                }
                unlock();
            }
        });
        buttonFadie.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("fadie")) {
                    selectedNotesList.remove("fadie");
                    buttonFadie.setSelected(false);
                } else {
                    selectedNotesList.add("fadie");
                    buttonFadie.setSelected(true);
                }
                unlock();
            }
        });
        buttonSol.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("sol")) {
                    selectedNotesList.remove("sol");
                    buttonSol.setSelected(false);
                } else {
                    selectedNotesList.add("sol");
                    buttonSol.setSelected(true);
                }
                unlock();
            }
        });
        buttonSoldie.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("soldie")) {
                    selectedNotesList.remove("soldie");
                    buttonSoldie.setSelected(false);
                } else {
                    selectedNotesList.add("soldie");
                    buttonSoldie.setSelected(true);
                }
                unlock();
            }
        });
        buttonLa.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("la")) {
                    selectedNotesList.remove("la");
                    buttonLa.setSelected(false);
                } else {
                    selectedNotesList.add("la");
                    buttonLa.setSelected(true);
                }
                unlock();
            }
        });
        buttonLadie.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("ladie")) {
                    selectedNotesList.remove("ladie");
                    buttonLadie.setSelected(false);
                } else {
                    selectedNotesList.add("ladie");
                    buttonLadie.setSelected(true);
                }
                unlock();
            }
        });
        buttonSi.setOnClickListener(v -> {
            if (selectedNotesList.size() < NOTES) {
                if (selectedNotesList.contains("si")) {
                    selectedNotesList.remove("si");
                    buttonSi.setSelected(false);
                } else {
                    selectedNotesList.add("si");
                    buttonSi.setSelected(true);
                }
                unlock();
            }
        });
        
        super.onCreate(savedInstanceState);
    }

    //Finishes if a call arrived and is ringing or waiting
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING)
                unlockAndRemoveView();
        }
    }

    //Finishes the activity before starting a new one when the screen gets locked
    @Override
    protected void onRestart() {
        removeView();
        super.onRestart();
    }

    /*When using a screen overlay, the system shows a notification allowing the user to remove it.
    This condition, on some devices, bypasses this occurrence by keeping the activity in foreground
    If it does not work on your device, consider to manually disable notifications of LockScreen*/
    @Override
    protected void onPause() {
        ((ActivityManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE))).moveTaskToFront(getTaskId(), 0);
        super.onPause();
    }

    //Does nothing on back press
    @Override
    public void onBackPressed() {}

    final Random rand = new Random();
    int randIdNote;
    int lastRandIdNote;
    Element note;
    String noteName;
    String noteSoundName;
    String[] outputtedNotes = new String[NOTES];

    //Plays random notes got from the xml document "notes.xml" in "src/main/assets" folder. Notes are stored in "res/raw" folder
    private void play() {
        for (int i = 0; i < NOTES; i++) {
            randIdNote = rand.nextInt(TOTAL_NOTES) + 1;
            note = doc.getElementById(String.valueOf(randIdNote));
            noteName = note.getElementsByTagName("name").item(0).getTextContent();
            noteSoundName = note.getElementsByTagName("sound_name").item(0).getTextContent();
            MediaPlayer.create(this, getResources().getIdentifier(noteSoundName, "raw", getPackageName())).start();
            if (i == 0) {
                lastRandIdNote = randIdNote;
                outputtedNotes[i] = noteName;
            } else {
                if (randIdNote < lastRandIdNote) {
                    outputtedNotes[i] = outputtedNotes[i-1];
                    outputtedNotes[i-1] = noteName;
                    lastRandIdNote = randIdNote;
                } else
                    outputtedNotes[i] = noteName;
            }
        }
    }

    //Unlocks if user guessed all and only the outputted notes
    List<String> outputtedNotesList = Arrays.asList(outputtedNotes);
    List<Integer> notesColor = new ArrayList<>(NOTES);
    StringBuilder coloredStringTv = new StringBuilder();
    private void unlock() {
        if (selectedNotesList.containsAll(outputtedNotesList) && outputtedNotesList.containsAll(selectedNotesList))
            unlockAndRemoveView();
        else if (selectedNotesList.size() >= NOTES) {
            //Colors in red the buttons of the wrong notes
            for (String snl : selectedNotesList) {
                switch (snl) {
                    case "do":
                        if (!outputtedNotesList.contains(snl))
                            buttonDo.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "dodie":
                        if (!outputtedNotesList.contains(snl))
                            buttonDodie.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "re":
                        if (!outputtedNotesList.contains(snl))
                            buttonRe.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "redie":
                        if (!outputtedNotesList.contains(snl))
                            buttonRedie.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "mi":
                        if (!outputtedNotesList.contains(snl))
                            buttonMi.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "fa":
                        if (!outputtedNotesList.contains(snl))
                            buttonFa.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "fadie":
                        if (!outputtedNotesList.contains(snl))
                            buttonFadie.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "sol":
                        if (!outputtedNotesList.contains(snl))
                            buttonSol.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "soldie":
                        if (!outputtedNotesList.contains(snl))
                            buttonSoldie.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "la":
                        if (!outputtedNotesList.contains(snl))
                            buttonLa.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "ladie":
                        if (!outputtedNotesList.contains(snl))
                            buttonLadie.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                    case "si":
                        if (!outputtedNotesList.contains(snl))
                            buttonSi.setBackground(ContextCompat.getDrawable(this, R.drawable.wrong_round));
                        break;
                }
            }

            /*Converts the name of the outputted notes based on the chosen language instead of the name present in the xml file and
            Colors the outputted notes in the textview and the buttons of this notes:
            green if the entry is correct,
            yellow if the entry is missing in the outputted notes*/
            for (String onl : outputtedNotesList) {
                switch (onl) {
                    case "do":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Do));
                        if (selectedNotesList.contains(onl)) {
                            buttonDo.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonDo.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "dodie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Dodie));
                        if (selectedNotesList.contains(onl)) {
                            buttonDodie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonDodie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "re":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Re));
                        if (selectedNotesList.contains(onl)) {
                            buttonRe.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonRe.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "redie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Redie));
                        if (selectedNotesList.contains(onl)) {
                            buttonRedie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonRedie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "mi":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Mi));
                        if (selectedNotesList.contains(onl)) {
                            buttonMi.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonMi.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "fa":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Fa));
                        if (selectedNotesList.contains(onl)) {
                            buttonFa.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonFa.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "fadie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Fadie));
                        if (selectedNotesList.contains(onl)) {
                            buttonFadie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonFadie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "sol":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Sol));
                        if (selectedNotesList.contains(onl)) {
                            buttonSol.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonSol.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "soldie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Soldie));
                        if (selectedNotesList.contains(onl)) {
                            buttonSoldie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonSoldie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "la":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.La));
                        if (selectedNotesList.contains(onl)) {
                            buttonLa.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonLa.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "ladie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Ladie));
                        if (selectedNotesList.contains(onl)) {
                            buttonLadie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonLadie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                    case "si":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Si));
                        if (selectedNotesList.contains(onl)) {
                            buttonSi.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_green));
                        } else {
                            buttonSi.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_yellow));
                        }
                        break;
                }
            }

            //Prints all the outputted notes on the textview, since the user inserted an incorrect entry (see also the functions above)
            for (int i = 0; i < NOTES; i++) {
                if (i != NOTES-1)
                    coloredStringTv.append("<font color='").append(notesColor.get(i)).append("'>").append(outputtedNotesList.get(i)).append(" ").append("</font>");
                else
                    coloredStringTv.append("<font color='").append(notesColor.get(i)).append("'>").append(outputtedNotesList.get(i)).append("</font>");
            }
            tv.setText(HtmlCompat.fromHtml(coloredStringTv.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

    }

    private void removeView() {
        mWindowManager.removeView(view);
        view = null;
        mWindowManager = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
            telephony.listen(stateListener, PhoneStateListener.LISTEN_NONE);
        //Sets the screen off timeout to the default value
        if (Settings.System.canWrite(this))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, systemScreenOffTimeoutDefaultValue);
        finish();
    }

    private void unlockAndRemoveView() {
        isLockScreenRunning = false;
        sendBroadcast(new Intent("changeNotification"));
        TileService.requestListeningState(this, new ComponentName(this, LockTileService.class));
        removeView();
    }

}