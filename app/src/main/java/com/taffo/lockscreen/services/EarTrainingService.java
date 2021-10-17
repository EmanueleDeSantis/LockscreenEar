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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.taffo.lockscreen.EarTrainingActivity;
import com.taffo.lockscreen.R;
import com.taffo.lockscreen.utils.SharedPref;
import com.taffo.lockscreen.utils.XMLParser;

public final class EarTrainingService extends Service {
	private SharedPref sp;
	private static boolean dontStartEarActivities = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("parsingError"))
				dontStartEarActivities = true;
		}
	};

	@Override
	public IBinder onBind(@Nullable Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		sp = new SharedPref(this);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Used to prevent "guess the notes" activities from starting when a parsing error occurs
		registerReceiver(mReceiver, new IntentFilter("parsingError"));

		startEarTrainingActivity();
		return super.onStartCommand(intent, flags, startId);
	}

	private void startEarTrainingActivity() {
		XMLParser parser = new XMLParser();
		parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
		parser.parseXmlNotes(this);
		if (dontStartEarActivities) {
			Toast.makeText(this, getString(R.string.cant_start_service), Toast.LENGTH_LONG).show();
			sp.setSharedmPrefService(false);
			stopSelf();
		} else
			startActivity(new Intent(this, EarTrainingActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

}