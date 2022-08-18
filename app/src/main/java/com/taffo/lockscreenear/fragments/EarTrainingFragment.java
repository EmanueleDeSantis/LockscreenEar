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

package com.taffo.lockscreenear.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.taffo.lockscreenear.R;
import com.taffo.lockscreenear.activities.MainActivity;
import com.taffo.lockscreenear.utils.SharedPref;
import com.taffo.lockscreenear.utils.XMLParser;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class EarTrainingFragment extends Fragment {
    private final XMLParser parser = new XMLParser();
    private int NOTES = parser.getNotes();
    private int UNIQUE_NOTES = NOTES; //Just to initialize it, but will always be replaced
    private final int TOTAL_NOTES = parser.getTotalNotes();
    private final int TOTAL_NUMBER_OF_NOTES = 12;
    private final Document doc = parser.getDocum();

    private Context mContext;
    private SharedPref sp;
    private MainActivity activity;
    private ActionBar actionBar;
    private boolean easterEggChallenge = false;

    private ConstraintLayout backgroundLayout;
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

    private Thread[] threads = new Thread[NOTES];
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(NOTES + 1);
    private List<Integer> randomNotesList = new ArrayList<>(NOTES);
    private MediaPlayer[] mediaPlayer = new MediaPlayer[NOTES];
    private final MediaPlayer[] mediaPlayerSingleNote = new MediaPlayer[TOTAL_NUMBER_OF_NOTES];
    private List<String> outputtedNotesList = new ArrayList<>(NOTES);
    private List<String> selectedNotesList = new ArrayList<>(NOTES);private List<Integer> notesColor = new ArrayList<>(NOTES);
    private final StringBuilder coloredStringTextViewNotes = new StringBuilder();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = requireContext();
        activity = (MainActivity) requireActivity();
        sp = new SharedPref(mContext);
        actionBar = Objects.requireNonNull(activity.getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ear_training_fragment, container, false);

        activity.getWindow().setStatusBarColor(mContext.getColor(R.color.custom_background_ear_training_deep_blue));
        actionBar.setBackgroundDrawable(new ColorDrawable(mContext.getColor(R.color.custom_background_ear_training_deep_blue)));

        //Initializes the view's elements
        backgroundLayout = rootView.findViewById(R.id.backgroundLayout);
        textViewNotes = rootView.findViewById(R.id.textViewNotes);
        buttonDo = rootView.findViewById(R.id.buttonDo);
        buttonDodie = rootView.findViewById(R.id.buttonDodie);
        buttonRe = rootView.findViewById(R.id.buttonRe);
        buttonRedie = rootView.findViewById(R.id.buttonRedie);
        buttonMi = rootView.findViewById(R.id.buttonMi);
        buttonFa = rootView.findViewById(R.id.buttonFa);
        buttonFadie = rootView.findViewById(R.id.buttonFadie);
        buttonSol = rootView.findViewById(R.id.buttonSol);
        buttonSoldie = rootView.findViewById(R.id.buttonSoldie);
        buttonLa = rootView.findViewById(R.id.buttonLa);
        buttonLadie = rootView.findViewById(R.id.buttonLadie);
        buttonSi = rootView.findViewById(R.id.buttonSi);
        ImageButton buttonDiapason = rootView.findViewById(R.id.buttonDiapason);
        Button buttonPlayAgain = rootView.findViewById(R.id.buttonPlayAgain);
        ImageButton buttonNext = rootView.findViewById(R.id.buttonNext);

        buttonDiapason.setOnClickListener(v -> playSingleNote(9));

        buttonPlayAgain.setOnClickListener(v -> {
            for (MediaPlayer mPlayer : mediaPlayer) {
                mPlayer.stop();
                try {
                    mPlayer.prepare();
                } catch (IOException ignored) {
                    getParentFragmentManager().popBackStack();
                    Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
            }
            play();
        });

        buttonNext.setOnClickListener(v -> resetAndRestart());

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
            } else
                playSingleNote(0);
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
            } else
                playSingleNote(1);
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
            } else
                playSingleNote(2);
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
            } else
                playSingleNote(3);
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
            } else
                playSingleNote(4);
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
            } else
                playSingleNote(5);
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
            } else
                playSingleNote(6);
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
            } else
                playSingleNote(7);
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
            } else
                playSingleNote(8);
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
            } else
                playSingleNote(9);
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
            } else
                playSingleNote(10);
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
            } else
                playSingleNote(11);
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepareToPlaySingleNote();

        easterEggChallenge();
        prepareToPlay();
        if (easterEggChallenge)
            backgroundLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.easter_egg_wallpaper_icon));
    }

    @Override
    public void onPause() {
        super.onPause();
        for (MediaPlayer mPlayer : mediaPlayer)
            mPlayer.stop();
        for (MediaPlayer mPlayer : mediaPlayerSingleNote)
            mPlayer.stop();
        if (isRemoving()) {
            for (MediaPlayer mPlayer : mediaPlayer)
                mPlayer.release();
            for (MediaPlayer mPlayer : mediaPlayerSingleNote)
                mPlayer.release();
            activity.getWindow().setStatusBarColor(mContext.getColor(R.color.custom_color_primary));
            actionBar.setBackgroundDrawable(new ColorDrawable(mContext.getColor(R.color.custom_color_primary)));
            setHasOptionsMenu(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            activity.setButtonTrainingClickable();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.settings).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            getParentFragmentManager().popBackStack();
        return super.onOptionsItemSelected(item);
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
        textViewNotes.setTextColor(mContext.getColor(R.color.custom_right_round_green));

        buttonDo.setSelected(false);
        buttonDo.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonDodie.setSelected(false);
        buttonDodie.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonRe.setSelected(false);
        buttonRe.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonRedie.setSelected(false);
        buttonRedie.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonMi.setSelected(false);
        buttonMi.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonFa.setSelected(false);
        buttonFa.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonFadie.setSelected(false);
        buttonFadie.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonSol.setSelected(false);
        buttonSol.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonSoldie.setSelected(false);
        buttonSoldie.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonLa.setSelected(false);
        buttonLa.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonLadie.setSelected(false);
        buttonLadie.setBackgroundResource(R.drawable.ear_training_round_button);
        buttonSi.setSelected(false);
        buttonSi.setBackgroundResource(R.drawable.ear_training_round_button);

        if (easterEggChallenge) {
            NOTES = parser.getNotes();
            easterEggChallenge = false;
            reallocateVariables();
        }
        easterEggChallenge();
        prepareToPlay();
        if (easterEggChallenge)
            backgroundLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.easter_egg_wallpaper_icon));
        else
            backgroundLayout.setBackground(ContextCompat.getDrawable(mContext, R.color.custom_background_ear_training_deep_blue));
    }

    private void easterEggChallenge() {
        if (sp.getSharedmPrefEasterEggChallengeNotCompleted() && sp.getSharedmPrefEasterEggChallengeStarted()) {
            //This probability will increase proportionally to the notes guessed by the user
            if (new Random().nextInt(sp.getSharedmPrefEasterEggChallengeGuesses()) + 1 == 1) {
                easterEggChallenge = true;
                sp.setSharedmPrefEasterEggChallengeGuesses(100); //Resets probability to 1/100
                textViewNotes.setText(getString(R.string.easter_egg_challenge_started_title).toUpperCase());
                textViewNotes.setTextColor(mContext.getColor(R.color.custom_easter_egg_gold));
                NOTES = (int) Arrays.stream(getResources().getStringArray(R.array.array_start_service_array_number_of_notes)).count();
                reallocateVariables();
            }
        }
    }

    private void reallocateVariables() {
        outputtedNotesList = new ArrayList<>(NOTES);
        selectedNotesList = new ArrayList<>(NOTES);
        randomNotesList = new ArrayList<>(NOTES);
        notesColor = new ArrayList<>(NOTES);
        mediaPlayer = new MediaPlayer[NOTES];
        threads = new Thread[NOTES];
        cyclicBarrier = new CyclicBarrier(NOTES + 1);
    }

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
            mediaPlayer[i] = MediaPlayer.create(mContext,
                    getResources().getIdentifier(doc.getElementById(String.valueOf(randomId))
                            .getElementsByTagName("sound_name").item(0).getTextContent(), "raw", mContext.getPackageName()));
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
                    getParentFragmentManager().popBackStack();
                    Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start(); //...so that when they all are ready...
        }

        try {
            cyclicBarrier.await(); //...they start together!
        } catch (BrokenBarrierException | InterruptedException e) {
            getParentFragmentManager().popBackStack();
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }
    }

    private void prepareToPlaySingleNote() {
        int index = 0;
        for (int i = 18; i <= 29; i++) {//From Do4 to Si4
            mediaPlayerSingleNote[index] = MediaPlayer.create(mContext,
                    getResources().getIdentifier(parser.getDocum().getElementById(String.valueOf(i)) //Note in the 4th octave
                            .getElementsByTagName("sound_name").item(0).getTextContent(), "raw", mContext.getPackageName()));
            index++;
        }
    }

    private void playSingleNote(int id) {
        for (MediaPlayer mPlayer : mediaPlayerSingleNote)
            mPlayer.stop();

        try {
            mediaPlayerSingleNote[id].prepare();
        } catch (IOException ignored) {
            getParentFragmentManager().popBackStack();
            Toast.makeText(mContext, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        }

        mediaPlayerSingleNote[id].start();
    }

    //Checks the user guess
    private void check() {
        if (selectedNotesList.size() >= UNIQUE_NOTES) {
            if (selectedNotesList.containsAll(outputtedNotesList) && outputtedNotesList.containsAll(selectedNotesList)) {
                textViewNotes.setText(getString(R.string.congrats));
                if (sp.getSharedmPrefEasterEggChallengeNotCompleted() && sp.getSharedmPrefEasterEggChallengeStarted()) {
                    //Increasing probability logic
                    if (sp.getSharedmPrefEasterEggChallengeGuesses() > NOTES)
                        sp.setSharedmPrefEasterEggChallengeGuesses(sp.getSharedmPrefEasterEggChallengeGuesses() - NOTES);
                    else
                        sp.setSharedmPrefEasterEggChallengeGuesses(1);
                    if (easterEggChallenge) {
                        sp.setSharedmPrefEasterEggChallengeNotCompleted(false);
                        textViewNotes.setTextColor(mContext.getColor(R.color.custom_easter_egg_gold));
                        SpannableString easterEggChallengeCompletedTitle = new SpannableString(
                                getString(R.string.easter_egg_challenge_started_title) + " @");
                        easterEggChallengeCompletedTitle.setSpan(
                                new ImageSpan(mContext, R.drawable.easter_egg_icon),
                                easterEggChallengeCompletedTitle.length() - 1,
                                easterEggChallengeCompletedTitle.length(),
                                0);
                        new AlertDialog.Builder(mContext)
                                .setIcon(R.mipmap.launcher)
                                .setTitle(easterEggChallengeCompletedTitle)
                                .setMessage(Html.fromHtml(getString(R.string.easter_egg_challenge_completed_message_html), Html.FROM_HTML_MODE_LEGACY))
                                .setPositiveButton((android.R.string.ok), (dialog, which) -> dialog.dismiss())
                                .setCancelable(false)
                                .create()
                                .show();
                        activity.initializeAdapter();
                    } else {
                        textViewNotes.setTextColor(mContext.getColor(R.color.custom_right_round_green));
                    }
                }

                for (String snl : selectedNotesList) {
                    switch (snl) {
                        case "do":
                            if (selectedNotesList.contains(snl))
                                buttonDo.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "dodie":
                            if (selectedNotesList.contains(snl))
                                buttonDodie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "re":
                            if (selectedNotesList.contains(snl))
                                buttonRe.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "redie":
                            if (selectedNotesList.contains(snl))
                                buttonRedie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "mi":
                            if (selectedNotesList.contains(snl))
                                buttonMi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "fa":
                            if (selectedNotesList.contains(snl))
                                buttonFa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "fadie":
                            if (selectedNotesList.contains(snl))
                                buttonFadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "sol":
                            if (selectedNotesList.contains(snl))
                                buttonSol.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "soldie":
                            if (selectedNotesList.contains(snl))
                                buttonSoldie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "la":
                            if (selectedNotesList.contains(snl))
                                buttonLa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "ladie":
                            if (selectedNotesList.contains(snl))
                                buttonLadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                        case "si":
                            if (selectedNotesList.contains(snl))
                                buttonSi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                            break;
                    }
                }
            } else {
                //Colors in red the buttons of the wrong notes
                for (String snl : selectedNotesList) {
                    switch (snl) {
                        case "do":
                            if (!outputtedNotesList.contains(snl))
                                buttonDo.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "dodie":
                            if (!outputtedNotesList.contains(snl))
                                buttonDodie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "re":
                            if (!outputtedNotesList.contains(snl))
                                buttonRe.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "redie":
                            if (!outputtedNotesList.contains(snl))
                                buttonRedie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "mi":
                            if (!outputtedNotesList.contains(snl))
                                buttonMi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "fa":
                            if (!outputtedNotesList.contains(snl))
                                buttonFa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "fadie":
                            if (!outputtedNotesList.contains(snl))
                                buttonFadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "sol":
                            if (!outputtedNotesList.contains(snl))
                                buttonSol.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "soldie":
                            if (!outputtedNotesList.contains(snl))
                                buttonSoldie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "la":
                            if (!outputtedNotesList.contains(snl))
                                buttonLa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "ladie":
                            if (!outputtedNotesList.contains(snl))
                                buttonLadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                        case "si":
                            if (!outputtedNotesList.contains(snl))
                                buttonSi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.wrong_round));
                            break;
                    }
                }

                //Converts the name of the outputted notes based on the chosen language instead of the name present in the xml file and
                //Colors the outputted notes in the text view and the buttons of mContext notes:
                //green if the entry is correct,
                //yellow if the entry is missing in the outputted notes
                for (String onl : outputtedNotesList) {
                    switch (onl) {
                        case "do":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Do));
                            if (selectedNotesList.contains(onl)) {
                                buttonDo.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonDo.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "dodie":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Dodie));
                            if (selectedNotesList.contains(onl)) {
                                buttonDodie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonDodie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "re":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Re));
                            if (selectedNotesList.contains(onl)) {
                                buttonRe.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonRe.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "redie":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Redie));
                            if (selectedNotesList.contains(onl)) {
                                buttonRedie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonRedie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "mi":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Mi));
                            if (selectedNotesList.contains(onl)) {
                                buttonMi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonMi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "fa":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Fa));
                            if (selectedNotesList.contains(onl)) {
                                buttonFa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonFa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "fadie":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Fadie));
                            if (selectedNotesList.contains(onl)) {
                                buttonFadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonFadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "sol":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Sol));
                            if (selectedNotesList.contains(onl)) {
                                buttonSol.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonSol.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "soldie":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Soldie));
                            if (selectedNotesList.contains(onl)) {
                                buttonSoldie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonSoldie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "la":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.La));
                            if (selectedNotesList.contains(onl)) {
                                buttonLa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonLa.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "ladie":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Ladie));
                            if (selectedNotesList.contains(onl)) {
                                buttonLadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonLadie.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                        case "si":
                            outputtedNotesList.set(outputtedNotesList.indexOf(onl), getString(R.string.Si));
                            if (selectedNotesList.contains(onl)) {
                                buttonSi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.right_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_right_round_green));
                            } else {
                                buttonSi.setBackground(ContextCompat.getDrawable(mContext, R.drawable.missed_round));
                                notesColor.add(ContextCompat.getColor(mContext, R.color.custom_missed_round_yellow));
                            }
                            break;
                    }
                }

                //Prints all the outputted notes on the text view, since the user inserted an incorrect entry (see also the functions above)
                for (int i = 0; i < NOTES; i++) {
                    if (i != NOTES - 1)
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
    }

}