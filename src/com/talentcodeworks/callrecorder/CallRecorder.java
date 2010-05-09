package com.talentcodeworks.callrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class CallRecorder
    extends Activity
{
    private static final int MENU_UPDATE = Menu.FIRST;
    private static final int MENU_PREFERENCES = Menu.FIRST+1;

    private static final int SHOW_PREFERENCES = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //startActivityForResult(new Intent(this, Preferences.class), SHOW_PREFERENCES);
        startActivity(new Intent(this, Preferences.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CallRecorder", "onActivityResult with requestCode: " + requestCode + " resultCode: " + resultCode + " Intent: " + data);
        
        if (requestCode == SHOW_PREFERENCES) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("CallRecord", "onActivityResult for preferences with RESULT_OK");
                //updateFromPreferences();
            }
        }
    }
}

/*
public class CallRecorder 
    extends Activity
{
    static public final String PREF_AUDIO_SOURCE = "PREF_AUDIO_SOURCE";
    static public final String PREF_AUDIO_FORMAT = "PREF_AUDIO_FORMAT";

    private SharedPreferences prefs;
    private CheckBox recordingEnabledCheckBox;
    private Spinner audioSourceSpinner;
    private Spinner audioFormatSpinner;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        Log.d("CallRecorder", "created");

        final Button button = (Button) findViewById(R.id.save_prefs);
        button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click 
                    savePreferences();
                }
        });

        // get/set preferences
        recordingEnabledCheckBox = (CheckBox)findViewById(R.id.check_recording_enabled);
        audioSourceSpinner = (Spinner)findViewById(R.id.spinner_audio_source);
        audioFormatSpinner = (Spinner)findViewById(R.id.spinner_audio_format);

        populateSpinners();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateUIFromPreferences();
    }

    private void populateSpinners() {
        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;

        // source
        ArrayAdapter<CharSequence> audioSourceAdapter;
        audioSourceAdapter = ArrayAdapter.createFromResource(this, R.array.audio_source_options,
                                                             android.R.layout.simple_spinner_item);
        audioSourceAdapter.setDropDownViewResource(spinner_dd_item);
        audioSourceSpinner.setAdapter(audioSourceAdapter);

        // format
        ArrayAdapter<CharSequence> audioFormatAdapter;
        audioFormatAdapter = ArrayAdapter.createFromResource(this, R.array.audio_format_options,
                                                             android.R.layout.simple_spinner_item);
        audioFormatAdapter.setDropDownViewResource(spinner_dd_item);
        audioFormatSpinner.setAdapter(audioFormatAdapter);
    }

    private void updateUIFromPreferences() {
        int audioSourceIndex = prefs.getInt(PREF_AUDIO_SOURCE, MediaRecorder.AudioSource.MIC);
        audioSourceSpinner.setSelection(audioSourceIndex);

        int audioFormatIndex = prefs.getInt(PREF_AUDIO_FORMAT, MediaRecorder.OutputFormat.THREE_GPP);
        audioFormatSpinner.setSelection(audioFormatIndex);
    }

    private void savePreferences() {
        int audioSourceIndex = audioSourceSpinner.getSelectedItemPosition();
        int audioFormatIndex = audioFormatSpinner.getSelectedItemPosition();

        Editor editor = prefs.edit();

        editor.putInt(PREF_AUDIO_SOURCE, audioSourceIndex);
        editor.putInt(PREF_AUDIO_FORMAT, audioFormatIndex);

        editor.commit();
    }
}
*/
