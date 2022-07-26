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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.services.LockAccessibilityService;
import com.taffo.lockscreenear.utils.Utils;
import com.taffo.lockscreenear.utils.XMLParser;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class LockscreenEarActivity extends AppCompatActivity {
    private final XMLParser parser = new XMLParser();
    private final int NOTES = parser.getNotes();
    private int UNIQUE_NOTES = NOTES; //Just to initialize it, but will always be replaced
    private final int TOTAL_NOTES = parser.getTotalNotes();
    private final Document doc = parser.getDocum();

    private static boolean unlocked = false;
    private int systemScreenOffTimeoutDefaultValue;
    private AudioManager audio;
    private Timer timer;

    private TextView textViewNotes;
    private Button buttonDo;
    private Button buttonDodie;
    private Button buttonRe;
    private Button buttonRedie;
    private Button buttonMi;
    private Button buttonFa;
    private Button buttonFadie;
    private Button buttonSol;
    private Button buttonSoldie;
    private Button buttonLa;
    private Button buttonLadie;
    private Button buttonSi;

    private final MediaPlayer[] mediaPlayer = new MediaPlayer[NOTES];
    private final List<String> outputtedNotesList = new ArrayList<>(NOTES);
    private final List<String> selectedNotesList = new ArrayList<>(NOTES);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen_activity);

        Utils.setIsLockscreenEarRunning(true);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Saves the default screen off timeout to restore it when the screen is unlocked
        try {
            systemScreenOffTimeoutDefaultValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //Sets the screen off timeout to 10 seconds
        if (Settings.System.canWrite(this))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 10000);

        textViewNotes = findViewById(R.id.textViewNotes);
        buttonDo = findViewById(R.id.buttonDo);
        buttonDodie = findViewById(R.id.buttonDodie);
        buttonRe = findViewById(R.id.buttonRe);
        buttonRedie = findViewById(R.id.buttonRedie);
        buttonMi = findViewById(R.id.buttonMi);
        buttonFa = findViewById(R.id.buttonFa);
        buttonFadie = findViewById(R.id.buttonFadie);
        buttonSol = findViewById(R.id.buttonSol);
        buttonSoldie = findViewById(R.id.buttonSoldie);
        buttonLa = findViewById(R.id.buttonLa);
        buttonLadie = findViewById(R.id.buttonLadie);
        buttonSi = findViewById(R.id.buttonSi);

        buttonDo.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("do")) {
                    selectedNotesList.remove("do");
                    buttonDo.setSelected(false);
                } else {
                    selectedNotesList.add("do");
                    buttonDo.setSelected(true);
                }
                check();
            }
        });
        buttonDodie.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("dodie")) {
                    selectedNotesList.remove("dodie");
                    buttonDodie.setSelected(false);
                } else {
                    selectedNotesList.add("dodie");
                    buttonDodie.setSelected(true);
                }
                check();
            }
        });
        buttonRe.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("re")) {
                    selectedNotesList.remove("re");
                    buttonRe.setSelected(false);
                } else {
                    selectedNotesList.add("re");
                    buttonRe.setSelected(true);
                }
                check();
            }
        });
        buttonRedie.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("redie")) {
                    selectedNotesList.remove("redie");
                    buttonRedie.setSelected(false);
                } else {
                    selectedNotesList.add("redie");
                    buttonRedie.setSelected(true);
                }
                check();
            }
        });
        buttonMi.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("mi")) {
                    selectedNotesList.remove("mi");
                    buttonMi.setSelected(false);
                } else {
                    selectedNotesList.add("mi");
                    buttonMi.setSelected(true);
                }
                check();
            }
        });
        buttonFa.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("fa")) {
                    selectedNotesList.remove("fa");
                    buttonFa.setSelected(false);
                } else {
                    selectedNotesList.add("fa");
                    buttonFa.setSelected(true);
                }
                check();
            }
        });
        buttonFadie.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("fadie")) {
                    selectedNotesList.remove("fadie");
                    buttonFadie.setSelected(false);
                } else {
                    selectedNotesList.add("fadie");
                    buttonFadie.setSelected(true);
                }
                check();
            }
        });
        buttonSol.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("sol")) {
                    selectedNotesList.remove("sol");
                    buttonSol.setSelected(false);
                } else {
                    selectedNotesList.add("sol");
                    buttonSol.setSelected(true);
                }
                check();
            }
        });
        buttonSoldie.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("soldie")) {
                    selectedNotesList.remove("soldie");
                    buttonSoldie.setSelected(false);
                } else {
                    selectedNotesList.add("soldie");
                    buttonSoldie.setSelected(true);
                }
                check();
            }
        });
        buttonLa.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("la")) {
                    selectedNotesList.remove("la");
                    buttonLa.setSelected(false);
                } else {
                    selectedNotesList.add("la");
                    buttonLa.setSelected(true);
                }
                check();
            }
        });
        buttonLadie.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("ladie")) {
                    selectedNotesList.remove("ladie");
                    buttonLadie.setSelected(false);
                } else {
                    selectedNotesList.add("ladie");
                    buttonLadie.setSelected(true);
                }
                check();
            }
        });
        buttonSi.setOnClickListener(v -> {
            if (selectedNotesList.size() < UNIQUE_NOTES) {
                if (selectedNotesList.contains("si")) {
                    selectedNotesList.remove("si");
                    buttonSi.setSelected(false);
                } else {
                    selectedNotesList.add("si");
                    buttonSi.setSelected(true);
                }
                check();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        unlocked = false;
        prepareToPlay();

        //Not tested yet
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audio.addOnModeChangedListener(runnable -> {}, i -> {
                if (Utils.getCallAndCallSetting(getApplicationContext()))
                    unlockAndFinish();
            });
        } else {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                //For API <= 30 there is no listener when a change in AudioManager mode occurs
                //Solved with a timer
                @Override
                public void run() {
                    if (((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked()) {
                        timer.cancel();
                        cancel();
                    } else if (Utils.getCallAndCallSetting(getApplicationContext()))
                        unlockAndFinish();
                }
            }, 0, 1000); //Every second
        }
    }

    //Any action that takes this activity away does not work, except, sometimes, for home and recent tasks intents
    @Override
    protected void onPause() {
        super.onPause();
        ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).moveTaskToFront(getTaskId(), 0);
    }

    //Does nothing on back press
    @Override
    public void onBackPressed() {}

    //To prevent user from exiting (without properly unlocking) this activity, when it loses focus the screen locks again
    @Override
    protected void onStop() {
        super.onStop();
        resetAndRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (MediaPlayer mPlayer : mediaPlayer)
            mPlayer.release();
        if (timer != null)
            timer.cancel();
        //Sets the screen off timeout to the default value
        if (Settings.System.canWrite(this))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, systemScreenOffTimeoutDefaultValue);
        if (!unlocked)
            LockAccessibilityService.lockTheScreen(this);
        sendBroadcast(new Intent("com.taffo.lockscreen.action.LockscreenEarActivityFinished"));
        Utils.setIsLockscreenEarRunning(false);
    }

    private final Thread[] threads = new Thread[NOTES];
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(NOTES + 1);
    private final List<Integer> randomNotesList = new ArrayList<>(NOTES);
    //Plays random notes got from the xml document "notes.xml" in "src/main/assets" folder. Notes are stored in "res/raw" folder
    private void prepareToPlay() {
        for (int i = 0; i < NOTES; i++) {
            //To avoid same note/same octave repetitions, for example
            //La2 and La3 are ok, so they both will be played
            //La2 and La2 are NOT ok, so the second instance of La2 will be replaced by another note
            int randomId;
            do {
                randomId = new Random().nextInt(TOTAL_NOTES) + 1;
            } while (randomNotesList.contains(randomId));
            //Binary Insertion Sort
            //so that if the guess of the user is wrong he will see the ordered outputted list of notes
            int index = Math.abs(Collections.binarySearch(randomNotesList, randomId) + 1);
            if (index < 0)
                index = -(index + 1);
            randomNotesList.add(index, randomId);
            outputtedNotesList.add(index, doc.getElementById(String.valueOf(randomId))
                    .getElementsByTagName("name").item(0).getTextContent());
            mediaPlayer[i] = MediaPlayer.create(this,
                    getResources().getIdentifier(doc.getElementById(String.valueOf(randomId))
                            .getElementsByTagName("sound_name").item(0).getTextContent(), "raw", getPackageName()));
        }

        play();

        UNIQUE_NOTES = (int) outputtedNotesList.stream().distinct().count();
    }

    private void play() {
        for (int i = 0; i < NOTES; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                mediaPlayer[finalI].start();
                try {
                    cyclicBarrier.await(); //Blocks the threads...
                } catch (BrokenBarrierException | InterruptedException e) {
                    unlockAndFinish();
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start(); //...so that when they all are ready...
        }

        try {
            cyclicBarrier.await(); //...they start together!
        } catch (BrokenBarrierException | InterruptedException e) {
            unlockAndFinish();
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }
    }

    private final List<Integer> notesColor = new ArrayList<>(NOTES);
    private final StringBuilder coloredStringTextViewNotes = new StringBuilder();
    //Unlocks and finishes this activity if user guessed all and only the outputted notes
    private void check() {
        if (selectedNotesList.containsAll(outputtedNotesList) && outputtedNotesList.containsAll(selectedNotesList))
            unlockAndFinish();
        else if (selectedNotesList.size() >= UNIQUE_NOTES) {
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

            //Converts the name of the outputted notes based on the chosen language instead of the name present in the xml file and
            //Colors the outputted notes in the text view and the buttons of this notes:
            //green if the entry is correct,
            //yellow if the entry is missing in the outputted notes
            for (String onl : outputtedNotesList) {
                switch (onl) {
                    case "do":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Do));
                        if (selectedNotesList.contains(onl)) {
                            buttonDo.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonDo.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "dodie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Dodie));
                        if (selectedNotesList.contains(onl)) {
                            buttonDodie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonDodie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "re":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Re));
                        if (selectedNotesList.contains(onl)) {
                            buttonRe.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonRe.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "redie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Redie));
                        if (selectedNotesList.contains(onl)) {
                            buttonRedie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonRedie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "mi":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Mi));
                        if (selectedNotesList.contains(onl)) {
                            buttonMi.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonMi.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "fa":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Fa));
                        if (selectedNotesList.contains(onl)) {
                            buttonFa.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonFa.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "fadie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Fadie));
                        if (selectedNotesList.contains(onl)) {
                            buttonFadie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonFadie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "sol":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Sol));
                        if (selectedNotesList.contains(onl)) {
                            buttonSol.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonSol.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "soldie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Soldie));
                        if (selectedNotesList.contains(onl)) {
                            buttonSoldie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonSoldie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "la":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.La));
                        if (selectedNotesList.contains(onl)) {
                            buttonLa.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonLa.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "ladie":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Ladie));
                        if (selectedNotesList.contains(onl)) {
                            buttonLadie.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonLadie.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                    case "si":
                        outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Si));
                        if (selectedNotesList.contains(onl)) {
                            buttonSi.setBackground(ContextCompat.getDrawable(this, R.drawable.right_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_right_round_green));
                        } else {
                            buttonSi.setBackground(ContextCompat.getDrawable(this, R.drawable.missed_round));
                            notesColor.add(ContextCompat.getColor(this, R.color.custom_missed_round_yellow));
                        }
                        break;
                }
            }

            //Prints all the outputted notes on the text view, since the user inserted an incorrect entry (see also the functions above)
            for (int i = 0; i < NOTES; i++) {
                if (i != NOTES-1)
                    coloredStringTextViewNotes.append("<font color='")
                            .append(notesColor.get(i)).append("'>").append(outputtedNotesList.get(i)).append(" ")
                            .append("</font>");
                else
                    //String does not have the final whitespace
                    coloredStringTextViewNotes.append("<font color='")
                            .append(notesColor.get(i)).append("'>").append(outputtedNotesList.get(i))
                            .append("</font>");
            }
            textViewNotes.setText(HtmlCompat.fromHtml(coloredStringTextViewNotes.toString(),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    private void unlockAndFinish() {
        unlocked = true;
        finish();
    }

    private void resetAndRestart() {
        for (MediaPlayer mPlayer : mediaPlayer)
            mPlayer.release();

        outputtedNotesList.clear();
        selectedNotesList.clear();
        randomNotesList.clear();
        notesColor.clear();
        coloredStringTextViewNotes.setLength(0);

        textViewNotes.setText(R.string.insert_notes);
        textViewNotes.setTextColor(getColor(R.color.custom_right_round_green));

        buttonDo.setSelected(false);
        buttonDo.setBackgroundResource(R.drawable.round);
        buttonDodie.setSelected(false);
        buttonDodie.setBackgroundResource(R.drawable.round);
        buttonRe.setSelected(false);
        buttonRe.setBackgroundResource(R.drawable.round);
        buttonRedie.setSelected(false);
        buttonRedie.setBackgroundResource(R.drawable.round);
        buttonMi.setSelected(false);
        buttonMi.setBackgroundResource(R.drawable.round);
        buttonFa.setSelected(false);
        buttonFa.setBackgroundResource(R.drawable.round);
        buttonFadie.setSelected(false);
        buttonFadie.setBackgroundResource(R.drawable.round);
        buttonSol.setSelected(false);
        buttonSol.setBackgroundResource(R.drawable.round);
        buttonSoldie.setSelected(false);
        buttonSoldie.setBackgroundResource(R.drawable.round);
        buttonLa.setSelected(false);
        buttonLa.setBackgroundResource(R.drawable.round);
        buttonLadie.setSelected(false);
        buttonLadie.setBackgroundResource(R.drawable.round);
        buttonSi.setSelected(false);
        buttonSi.setBackgroundResource(R.drawable.round);
    }

}