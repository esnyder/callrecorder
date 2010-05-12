package com.talentcodeworks.callrecorder;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class CallPlayer
    extends Activity
{
    private Spinner fileSpinner = null;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        File dir = new File("/sdcard");
        fileSpinner = (Spinner)findViewById(R.id.play_file_spinner);
        
        ArrayAdapter<CharSequence> fAdapter;
        Context context = getApplicationContext();
        fAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, dir.list());
        fileSpinner.setAdapter(fAdapter);

        Button b = (Button)findViewById(R.id.play_file_button);
        b.setOnClickListener(new Button.OnClickListener() 
            {
                public void onClick(View v) {
                    String fName = (String)fileSpinner.getSelectedItem();
                    playFile(fName);
                    //Log.i("CallPlayer", "button clicked with selected spinner: " + (String)fileSpinner.getSelectedItem());
                    //Log.d("CallPlayer", "button clicked");
                }
            });
    }

    private void playFile(String fName) {
        Log.i("CallPlayer", "playFile: " + fName);
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource("/sdcard/" + fName);
            player.prepare();
            player.start();
        } catch (java.io.IOException e) {
            Log.e("CallPlayer", "caught exception", e);
        }
    }
}
