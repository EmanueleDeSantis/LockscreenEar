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

package com.taffo.lockscreen.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreen.EarTrainingActivity;
import com.taffo.lockscreen.utils.SharedPref;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarTrainingService extends Service {
	SharedPref sp;

	//The actual number of notes to play (used in LockScreenActivity)
	private static String val;
	public int getNotes() {
		return Integer.parseInt(val);
	}

	//The actual number total number of stored notes in "res/raw" folder got from the xml document "notes.xml" in "src/main/assets" folder (used in LockScreenActivity)
	private static int totalVal;
	public int getTotalNotes() {
		return totalVal;
	}

	//The xml document is parsed here for optimizing time (used in LockScreenActivity)
	private static Document docum;
	public Document getDocum() {
		return docum;
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (context != null && intent.getAction() != null) {
				if (intent.getAction().equals("earTraining")) {
					val = sp.getSharedmPrefNotes();
					parseXmlNotes(context);
					startEarTrainingActivity();
				}
			}
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		sp = new SharedPref(this);
	}

	//Registers the receiver for lock screen events
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter(("earTraining")); //Starts EarTrainingActivity when the "earTraining" button in MainActivity is clicked
		registerReceiver(mReceiver, filter);
		return START_STICKY;
	}

	//Unregisters the receiver
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private void parseXmlNotes(Context cntx) {
		try {
			InputStream is = cntx.getAssets().open("notes.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			docum = dBuilder.parse(is);
			docum.getDocumentElement().normalize();
			totalVal = docum.getElementsByTagName("note").getLength();
		} catch (ParserConfigurationException | SAXException | IOException ignored) {}
	}

	private void startEarTrainingActivity() {
		startActivity(new Intent(this, EarTrainingActivity.class)
				.setAction(Intent.ACTION_VIEW)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

}