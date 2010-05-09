package com.talentcodeworks.callrecorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity
{
    static public final String PREF_RECORD_CALLS = "PREF_RECORD_CALLS";
    static public final String PREF_AUDIO_SOURCE = "PREF_AUDIO_SOURCE";
    static public final String PREF_AUDIO_FORMAT = "PREF_AUDIO_FORMAT";

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }
}
