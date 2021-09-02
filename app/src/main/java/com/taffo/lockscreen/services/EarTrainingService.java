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
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.taffo.lockscreen.EarTrainingActivity;
import com.taffo.lockscreen.utils.SharedPref;
import com.taffo.lockscreen.utils.XMLParser;

public final class EarTrainingService extends Service {
	private SharedPref sp;

	@Override
	public IBinder onBind(@Nullable Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		sp = new SharedPref(this);
		startEarTrainingActivity();
		return super.onStartCommand(intent, flags, startId);
	}

	private void startEarTrainingActivity() {
		XMLParser parser = new XMLParser();
		parser.setNotes(sp.getSharedmPrefNumberOfNotesToPlay());
		parser.parseXmlNotes(this);
		startActivity(new Intent(this, EarTrainingActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

}