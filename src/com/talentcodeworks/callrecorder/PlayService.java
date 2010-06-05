package com.talentcodeworks.callrecorder;

/*
  This is the non-UI portion of our call recording player.

  We want the usual behavior of a media player, where the playback continues 
  whether we remain in the UI activity or not.  When we return to the
  UI it should reflect what is actually playing in this thread.
*/

import java.io.File;
import java.io.IOException;
import java.lang.Exception;

import android.os.IBinder;
import android.app.Service;
//import android.app.Notification;
//import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.preference.PreferenceManager;
//import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;
import android.util.Log;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Iterator;

public class PlayService 
    extends Service
    implements MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener
{
    private static String TAG = "CallRecorder";

    //public static final String DEFAULT_STORAGE_LOCATION = "/sdcard/callrecorder";
    //private static final int RECORDING_NOTIFICATION_ID = 1;

    public static final String EXTRA_FILENAME = "filename";

    private MediaPlayer player = null;
    private boolean isPlaying = false;
    private String recording = null;

    public void onCreate()
    {
        super.onCreate();
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnInfoListener(this);
        player.setOnErrorListener(this);
        Log.i(TAG, "PlayService::onCreate created MediaPlayer object");
    }

    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "PlayService::onStart called while isPlaying:" + isPlaying);

        if (isPlaying) return;

        Context c = getApplicationContext();
        recording = intent.getStringExtra(EXTRA_FILENAME);

        if (recording == null) {
            Log.w(TAG, "PlayService::onStart recording == null, returning");
            return;
        }

        Log.i(TAG, "PlayService will play " + recording);
        try {
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            player.setDataSource(recording);
            player.setLooping(false);
            player.prepare();
            Log.d(TAG, "PlayService player.prepare() returned");            
            player.start();

            isPlaying = true;
            Log.i(TAG, "player.start() returned");
            //updateNotification(true);
        } catch (java.io.IOException e) {
            Log.e(TAG, "PlayService::onStart() IOException attempting player.prepare()\n");
            Toast t = Toast.makeText(getApplicationContext(), "PlayService was unable to start playing recording: " + e, Toast.LENGTH_LONG);
            t.show();
            return;
        } catch (java.lang.Exception e) {
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start playing recording: " + e, Toast.LENGTH_LONG);
            t.show();

            Log.e(TAG, "PlayService::onStart caught unexpected exception", e);
        }

        return;
    }

    public void onDestroy()
    {
        if (null != player) {
            Log.i(TAG, "PlayService::onDestroy calling player.release()");
            isPlaying = false;
            player.release();
        }

        //updateNotification(false);
        super.onDestroy();
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

    /*
    private void updateNotification(Boolean status)
    {
        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        if (status) {
            int icon = R.drawable.rec;
            CharSequence tickerText = "Recording call from channel " + prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1");
            long when = System.currentTimeMillis();
            
            Notification notification = new Notification(icon, tickerText, when);
            
            Context context = getApplicationContext();
            CharSequence contentTitle = "CallRecorder Status";
            CharSequence contentText = "Recording call from channel...";
            Intent notificationIntent = new Intent(this, PlayService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            mNotificationManager.notify(RECORDING_NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
        }
    }
    */

    // MediaPlayer.OnCompletionListener
    public void onCompletion(MediaPlayer mp)
    {
        Log.i(TAG, "PlayService got MediaPlayer onCompletion callback");
        isPlaying = false;
    }

    // MediaPlayer.OnInfoListener
    public boolean onInfo(MediaPlayer mp, int what, int extra)
    {
        Log.i(TAG, "PlayService got MediaPlayer onInfo callback with what: " + what + " extra: " + extra);
        return false;
    }

    // MediaPlayer.OnErrorListener
    public boolean onError(MediaPlayer mp, int what, int extra) 
    {
        Log.e(TAG, "PlayService got MediaPlayer onError callback with what: " + what + " extra: " + extra);
        isPlaying = false;
        mp.reset();
        return true;
    }
}
