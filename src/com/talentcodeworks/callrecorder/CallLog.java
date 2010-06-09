package com.talentcodeworks.callrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

/*
  Ok, so, in theory this should work.  However, the behavior of the 
  android.widget.MediaController leaves something to be desired.
  Will have to see if we can get this figured out.
*/

public class CallLog
    extends Activity
{
    private final String TAG = "CallRecorder";

    private ListView fileList = null;
    private ArrayAdapter<String> fAdapter = null;
    private ArrayList<String> recordingNames = null;
    private MediaController controller = null;

    private void loadRecordingsFromProvider() 
    {
        fAdapter.clear();
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(RecordingProvider.CONTENT_URI, null, null, null, null);
        String[] names = new String[c.getCount()];
        int i = 0;

        if (c.moveToFirst()) {
            do {
                // Extract the recording names
                fAdapter.add(c.getString(RecordingProvider.DETAILS_COLUMN));
                i++;
            } while (c.moveToNext());
        }

        fAdapter.notifyDataSetChanged();
    }

    private void loadRecordingsFromDir() 
    {
        fAdapter.clear();
        File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
        String[] dlist = dir.list();

        for (int i=0; i<dlist.length; i++) {
            fAdapter.add(dlist[i]);
        }
        fAdapter.notifyDataSetChanged();
    }

    private class CallItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            CharSequence s = (CharSequence)parent.getItemAtPosition(position);
            Log.w(TAG, "CallLog just got an item clicked: " + s);
            File f = new File(RecordService.DEFAULT_STORAGE_LOCATION + "/" + s.toString());

            boolean useMediaController = true;

            if (useMediaController) {
                Intent playIntent = new Intent(getApplicationContext(), CallPlayer.class); //Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(f);
                playIntent.setData(uri);
                startActivity(playIntent);
            } else {
                playFile(s.toString());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_log);

        //recordingNames = new String[0];
        fileList = (ListView)findViewById(R.id.play_file_list);

        Context context = getApplicationContext();
        fAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        fileList.setAdapter(fAdapter);
        fileList.setOnItemClickListener(new CallItemClickListener());
    }

    public void onStart()
    {
        super.onStart();
        Log.i(TAG, "CallLog onStart");
    }

    public void onRestart()
    {
        super.onRestart();
        Log.i(TAG, "CallLog onRestart");
    }

    public void onResume()
    {
        super.onResume();
        Log.i(TAG, "CallLog onResume about to load recording list again, does this work?");

        loadRecordingsFromDir();
        // Once we switch from path to provider based storage, use this method
        //loadRecordingsFromProvider();
    }

    private void playFile(String fName) {
        Log.i(TAG, "playFile: " + fName);

        Context context = getApplicationContext();
        Intent playIntent = new Intent(context, PlayService.class);
        playIntent.putExtra(PlayService.EXTRA_FILENAME, RecordService.DEFAULT_STORAGE_LOCATION + "/" + fName);
        ComponentName name = context.startService(playIntent);
        if (null == name) {
            Log.w(TAG, "CallLog unable to start PlayService with intent: " + playIntent.toString());
        } else {
            Log.i(TAG, "CallLog started service: " + name);
        }
    }

    public void onDestroy() {
        Context context = getApplicationContext();
        Intent playIntent = new Intent(context, PlayService.class);
        context.stopService(playIntent);

        super.onDestroy();
    }
}
