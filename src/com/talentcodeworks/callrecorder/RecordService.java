package com.talentcodeworks.callrecorder;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;

import android.os.IBinder;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.widget.Toast;
import android.util.Log;

public class RecordService 
    extends Service
            //implements MediaRecorder.OnInfoListener
{
    private static final int RECORDING_NOTIFICATION_ID = 1;

    private MediaRecorder recorder = null;
    private File recording = null;;
    
    public void onCreate()
    {
        super.onCreate();
        recorder = new MediaRecorder();
        Log.i("CallRecorder", "onCreate created MediaRecorder object");
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Boolean shouldRecord = prefs.getBoolean(Preferences.PREF_RECORD_CALLS, false);
        int audiosource = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1"));
        int audioformat = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT, "1"));

        if (!shouldRecord) {
            Log.d("CallRecord", "RecordService::onStartCommand with PREF_RECORD_CALLS false, not recording");
            return START_STICKY;
        }

        try {
            recording = File.createTempFile("callrecord", ".3gpp", new File("/sdcard"));
        } catch (IOException e) {
            Log.e("CallRecorder", "RecordService::onStart unable to create temp file in /sdcard: " + e);
            return START_STICKY;
        }

        try {
            // These calls will throw exceptions unless you set the 
            // android.permission.RECORD_AUDIO permission for your app
            recorder.reset();
            recorder.setAudioSource(audiosource);
            Log.d("CallRecorder", "set audiosource " + audiosource);
            recorder.setOutputFormat(audioformat);
            Log.d("CallRecorder", "set output " + audioformat);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            Log.d("CallRecorder", "set encoder default");
            recorder.setOutputFile(recording.getAbsolutePath());
            Log.d("CallRecorder", "set file: " + recording);
            //recorder.setMaxDuration(msDuration); //1000); // 1 seconds
            //recorder.setMaxFileSize(bytesMax); //1024*1024); // 1KB

            //recorder.setOnInfoListener(this);
            //recorder.setOnErrorListener(this);
            
            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                Log.e("CallRecorder", "RecordService::onStart() IOException attempting recorder.prepare()\n");
                recorder = null;
                return START_STICKY;
            }
            Log.d("CallRecorder", "recorder.prepare() returned");
            
            recorder.start();
            Log.i("CallRecorder", "recorder.start() returned");
            updateNotification(true);
        } catch (java.lang.Exception e) {
            Log.e("CallRecorder", "RecordService::onStart caught unexpected exception", e);
            recorder = null;
        }

        return START_STICKY;
    }

    public void onDestroy()
    {
        if (null != recorder) {
            Log.i("CallRecorder", "RecordService::onDestroy calling recorder.release()");
            recorder.release();
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder finished recording call to " + recording, Toast.LENGTH_LONG);
            t.show();
        }

        updateNotification(false);
    }


    // methods to handle binding the service

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public boolean onUnbind(Intent intent)
    {
        return false;
    }

    public void onRebind(Intent intent)
    {
    }


    private void updateNotification(Boolean status)
    {
        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        // 1.
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        if (status) {
            // 2.
            int icon = R.drawable.rec;
            CharSequence tickerText = "Recording call from channel " + prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1");
            long when = System.currentTimeMillis();
            
            Notification notification = new Notification(icon, tickerText, when);
            
            // 3.
            Context context = getApplicationContext();
            CharSequence contentTitle = "CallRecorder Status";
            CharSequence contentText = "Recording call from channel...";
            Intent notificationIntent = new Intent(this, RecordService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            
            // 4.
            mNotificationManager.notify(RECORDING_NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
        }
    }
}
