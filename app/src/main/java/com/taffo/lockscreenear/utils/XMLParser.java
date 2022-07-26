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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.taffo.lockscreenear.R;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class XMLParser {
    //The actual number of notes to play (used in all "guess the notes" activities)
    private static String notes;
    //The actual total number of stored notes in "res/raw" folder
    //got from the xml document "notes.xml" in "src/main/assets" folder (used in all "guess the notes" activities)
    private static int totalNotes;
    //The Document containing information about the notes to be played
    private static Document document;
    //Used by services to update the correct number of notes to play
    public void setNotes(String s) {
        notes = s;
    }
    public int getNotes() {
        int intNotes = 3;
        if (notes != null)
            intNotes = Integer.parseInt(notes);
        if (intNotes < 1 || intNotes > 8) //Integrity Notes check
            intNotes = 3;
        return intNotes;
    }
    public int getTotalNotes() {
        return totalNotes;
    }
    public Document getDocum() {
        return document;
    }

    //The xml document is parsed here for more usability (used in all "guess the notes" activities)
    public boolean parseXmlNotes(@NonNull Context context) {
        try {
            document = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(context.getAssets().open("notes.xml"));
            totalNotes = document.getElementsByTagName("note").getLength();
            if (document != null && totalNotes > 0) {
                document.getDocumentElement().normalize();
                return true;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
        return false;
    }

}