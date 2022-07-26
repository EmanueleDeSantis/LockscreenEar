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

package com.taffo.lockscreenear.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.preference.Preference;

import com.taffo.lockscreenear.BuildConfig;
import com.taffo.lockscreenear.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Updater {
    private static boolean cancel = false;
    private Boolean canceledUpdate;
    private int lastUpdateVersionCode;
    private final String gitRepoReleaseURL = "https://github.com/EmanueleDeSantis/LockscreenEar/releases/download/3.14/";
    private final String appName = "LockscreenEar.apk";
    private String updateFeaturesText;
    private String versionNameText;
    private int versionCode;
    private AlertDialog alertDialog;
    private View alertView;
    private ProgressBar progressBar;
    private TextView progressBarText;
    private CheckBox progressBarCheckBox;
    private File file;

    public void update(Context context, boolean bypassCanceledUpdate, Preference preference) {
        SharedPref sp = new SharedPref(context);

        deleteUpdate(context);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject object = new JSONObject(sp.getSharedmPrefCancelUpdateAndDontAskUntilNextUpdate());
                canceledUpdate = Boolean.parseBoolean(object.getString("canceled_update"));
                lastUpdateVersionCode = Integer.parseInt(object.getString("last_update_versioncode"));
            } catch (JSONException e) {
                canceledUpdate = false; //Default
                lastUpdateVersionCode = 1; //state
            }

            try {
                URL urlVersion = new URL(gitRepoReleaseURL + "update.properties");
                InputStreamReader is = new InputStreamReader(urlVersion.openStream());
                BufferedReader reader = new BufferedReader(is);
                StringBuilder sb = new StringBuilder();
                String line;
                if ((line = reader.readLine()) != null) {
                    versionNameText = line; //First saves the VERSION_NAME
                    while ((line = reader.readLine()) != null) { //Then scans the entire file and
                        if (line.contains(Locale.getDefault().getLanguage())) { //if there is the list of features in the language the user is using...
                            while ((line = reader.readLine()) != null) {
                                if (line.equals("//")) { //End of the list of the features
                                    sb.replace(sb.length() - 1, sb.length(), ""); //Removes last \n
                                    break;
                                }
                                sb.append(line); //... it appends it
                                sb.append("\n"); //and starts a new line
                            }
                        }
                    }
                }
                is.close();
                updateFeaturesText = sb.toString();
                versionNameText = versionNameText.replace("=", ": ");
                try {
                    versionCode = Integer.parseInt(versionNameText.substring(versionNameText.indexOf("(") + 1, versionNameText.indexOf(")")));
                } catch (NumberFormatException e) {
                    versionCode = 1;
                }
                if (versionCode > lastUpdateVersionCode)
                    sp.setSharedmPrefCancelUpdateAndDontAskUntilNextUpdate("{canceled_update:" + false
                            + ", last_update_versioncode:" + versionCode + "}");
            } catch (IOException ignored) {}

            new Handler(Looper.getMainLooper()).post(() -> {
                if (Utils.checkConnectivity(context)) {
                    if (BuildConfig.VERSION_CODE < versionCode) {
                        if (bypassCanceledUpdate || !(canceledUpdate && versionCode <= lastUpdateVersionCode)) {
                            alertDialog = new CustomAlertDialog(context, preference);
                            alertView = View.inflate(context, R.layout.update_install, null);
                            TextView versionNameTextView = alertView.findViewById(R.id.versionNameTextView);
                            versionNameTextView.setText(Html.fromHtml(context.getString(R.string.update_message)
                                    + "<br /><br /><b>" + versionNameText + "</b>", Html.FROM_HTML_MODE_LEGACY));
                            TextView updateFeaturesTextView = alertView.findViewById(R.id.updateFeaturesTextView);
                            if (!updateFeaturesText.isEmpty()) {
                                updateFeaturesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                updateFeaturesTextView.setText(updateFeaturesText);
                            }
                            progressBar = alertView.findViewById(R.id.progressBar);
                            progressBarText = alertView.findViewById(R.id.progressBarText);
                            progressBarCheckBox = alertView.findViewById(R.id.progressBarCheckBox);
                            alertDialog.setView(alertView);
                            progressBar.setVisibility(View.GONE);
                            progressBarText.setVisibility(View.GONE);
                            if (bypassCanceledUpdate)
                                progressBarCheckBox.setVisibility(View.GONE);

                            //This two buttons are handled below so that the dialog does not get dismissed when they get clicked
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.download_button),
                                    (DialogInterface.OnClickListener) null);
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.cancel),
                                    (DialogInterface.OnClickListener) null);

                            alertDialog.show();

                            Button positiveButtonAlertDialog = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button negativeButtonAlertDialog = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                            positiveButtonAlertDialog.setOnClickListener(view -> {
                                positiveButtonAlertDialog.setText(android.R.string.cancel);
                                progressBar.setVisibility(View.VISIBLE);
                                progressBarText.setVisibility(View.VISIBLE);
                                negativeButtonAlertDialog.setVisibility(View.GONE);
                                progressBarCheckBox.setVisibility(View.GONE);

                                positiveButtonAlertDialog.setOnClickListener(v -> {
                                    cancel = true;
                                    alertDialog.dismiss();
                                });

                                String destination = context.getExternalFilesDir(null).toString() + appName;
                                file = new File(destination);

                                Executors.newSingleThreadExecutor().execute(() -> {
                                    try {
                                        URL url = new URL(gitRepoReleaseURL + appName);
                                        URLConnection connection = url.openConnection();
                                        int fileLength = connection.getContentLength();
                                        InputStream inputStream = connection.getInputStream();
                                        OutputStream outputStream = new FileOutputStream(destination);

                                        byte[] data = new byte[1024];
                                        int total = 0;
                                        int count;
                                        while ((count = inputStream.read(data)) != -1) {
                                            if (cancel) {
                                                cancel = false;
                                                inputStream.close();
                                                deleteUpdate(context);
                                                break;
                                            }
                                            total += count;
                                            if (fileLength > 0) {
                                                progressBar.setProgress(total * 100 / fileLength, true);
                                                progressBarText.setText((String.format(
                                                        Locale.getDefault(), "%.3f / %.3fMbps",
                                                        (float) total / 1000000, (float) fileLength / 1000000)));
                                            }
                                            outputStream.write(data, 0, count);
                                        }
                                        if (total >= fileLength) {
                                            alertDialog.dismiss();
                                            context.startActivity(new Intent(Intent.ACTION_VIEW)
                                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    .setDataAndType(FileProvider.getUriForFile(
                                                                    context.getApplicationContext(),
                                                                    BuildConfig.APPLICATION_ID + ".provider", file),
                                                            "application/vnd.android.package-archive"));
                                        }
                                        outputStream.flush();
                                        outputStream.close();
                                        inputStream.close();
                                    } catch (IOException ignored) {}
                                });
                            });
                            negativeButtonAlertDialog.setOnClickListener(view -> {
                                if (progressBarCheckBox.isChecked())
                                    sp.setSharedmPrefCancelUpdateAndDontAskUntilNextUpdate("{canceled_update:" + true
                                            + ", last_update_versioncode:" + versionCode + "}");
                                alertDialog.dismiss();
                            });
                        }
                    } else {
                        if (bypassCanceledUpdate) {
                            alertDialog = new CustomAlertDialog(context, preference);
                            alertDialog.setMessage(context.getString(R.string.already_updated_message));
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(android.R.string.ok),
                                    (dialog, which) -> dialog.dismiss());
                            alertDialog.create();
                            alertDialog.show();
                        }
                    }
                }
            });
        });
    }

    private void deleteUpdate(Context context) {
        if (file != null && file.exists())
            if (!file.delete())
                Toast.makeText(context, context.getText(R.string.unsuccessful_deletion), Toast.LENGTH_LONG).show();
    }

    private static class CustomAlertDialog extends AlertDialog {
        private final Preference preference;

        protected CustomAlertDialog(@NonNull Context context, Preference pref) {
            super(context);
            preference = pref;

            setIcon(R.mipmap.launcher);
            setTitle(R.string.update_title);
            setCancelable(false);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (preference != null)
                preference.setSelectable(true);
        }
    }
}
