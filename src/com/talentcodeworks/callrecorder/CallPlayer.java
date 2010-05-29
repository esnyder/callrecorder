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

/*
  This is brain dead at the moment.  At a minimum I need to have the 
  MediaPlayer playing on a thread.

  Right now if you are playing a clip and leave the CallPlayer activity it 
  faults.

  Ideally I would just farm all this out to a pre-existing media player 
  activity, but the default one in the SDK 1.6 emulator doesn't know how
  to play 3gpp files.

  If I'm going to have to go to all the bother of creating a little media
  player to play back recordings should at a minimum have the seekbar to scrub
  around and allow pausing, etc..
*/

public class CallPlayer
    extends Activity
{
    private Spinner fileSpinner = null;
    private MediaPlayer player = null;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        player = new MediaPlayer();

        File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
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
        if (player == null)
            return;

        try {
            player.reset();
            player.setDataSource(RecordService.DEFAULT_STORAGE_LOCATION + "/" + fName);
            player.setLooping(false);
            player.prepare();
            player.start();
        } catch (java.io.IOException e) {
            Log.e("CallPlayer", "caught exception", e);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
