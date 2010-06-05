package com.talentcodeworks.callrecorder;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

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
    //private Spinner fileSpinner = null;
    private ListView fileList = null;
    private String[] recordingNames = null;

    private String[] loadRecordingsFromProvider() {
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(RecordingProvider.CONTENT_URI, null, null, null, null);
        String[] names = new String[c.getCount()];
        int i = 0;

        if (c.moveToFirst()) {
            do {
                // Extract the recording names
                names[i] = c.getString(RecordingProvider.DETAILS_COLUMN);
                i++;
            } while (c.moveToNext());
        }

        return names;
    }

    private String[] loadRecordingsFromDir() {
        File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
        return dir.list();
    }

    private class CallItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            CharSequence s = (CharSequence)parent.getItemAtPosition(position);
            Log.w("CallRecorder", "CallPlayer just got an item clicked: " + s);
            playFile(s.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        recordingNames = new String[0];
        setContentView(R.layout.player);

        fileList = (ListView)findViewById(R.id.play_file_list);

        recordingNames = loadRecordingsFromDir();
        // Once we switch from path to provider based storage, use this method
        //recordingNames = loadRecordingsFromProvider();

        ArrayAdapter<CharSequence> fAdapter;
        Context context = getApplicationContext();
        fAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_list_item_1, recordingNames);
        //fAdapter.setOnItemClickListener(new CallItemClickListener());
        fileList.setAdapter(fAdapter);
        fileList.setOnItemClickListener(new CallItemClickListener());
    }

    private void playFile(String fName) {
        Log.i("CallPlayer", "playFile: " + fName);

        Context context = getApplicationContext();
        Intent playIntent = new Intent(context, PlayService.class);
        playIntent.putExtra(PlayService.EXTRA_FILENAME, RecordService.DEFAULT_STORAGE_LOCATION + "/" + fName);
        ComponentName name = context.startService(playIntent);
        if (null == name) {
            Log.w("CallRecorder", "CallPlayer unable to start PlayService with intent: " + playIntent.toString());
        } else {
            Log.i("CallRecorder", "CallPlayer started service: " + name);
        }
    }

    public void onDestroy() {
        Context context = getApplicationContext();
        Intent playIntent = new Intent(context, PlayService.class);
        context.stopService(playIntent);

        super.onDestroy();
    }
}
