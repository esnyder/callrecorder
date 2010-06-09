package com.talentcodeworks.callrecorder;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.util.Date;
import java.text.SimpleDateFormat;

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

//import java.security.KeyPairGenerator;
//import java.security.KeyPair;
//import java.security.Key;

import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Iterator;

public class RecordService 
    extends Service
    implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener
{
    private static final String TAG = "CallRecorder";

    public static final String DEFAULT_STORAGE_LOCATION = "/sdcard/callrecorder";
    private static final int RECORDING_NOTIFICATION_ID = 1;

    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private File recording = null;;

    /*
    private static void test() throws java.security.NoSuchAlgorithmException
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        Key publicKey = kp.getPublic();
        Key privateKey = kp.getPrivate();
    }
    */

    private File makeOutputFile (SharedPreferences prefs)
    {
        File dir = new File(DEFAULT_STORAGE_LOCATION);

        // test dir for existence and writeability
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                Log.e("CallRecorder", "RecordService::makeOutputFile unable to create directory " + dir + ": " + e);
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create the directory " + dir + " to store recordings: " + e, Toast.LENGTH_LONG);
                t.show();
                return null;
            }
        } else {
            if (!dir.canWrite()) {
                Log.e(TAG, "RecordService::makeOutputFile does not have write permission for directory: " + dir);
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder does not have write permission for the directory directory " + dir + " to store recordings", Toast.LENGTH_LONG);
                t.show();
                return null;
            }
        }

        // test size

        // create filename based on call data
        String prefix = "call";
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd_HH:MM:SS");
        //prefix = sdf.format(new Date()) + "-callrecording";

        // add info to file name about what audio channel we were recording
        int audiosource = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1"));
        prefix += "-channel" + audiosource + "-";

        // create suffix based on format
        String suffix = "";
        int audioformat = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT, "1"));
        switch (audioformat) {
        case MediaRecorder.OutputFormat.THREE_GPP:
            suffix = ".3gpp";
            break;
        case MediaRecorder.OutputFormat.MPEG_4:
            suffix = ".mpg";
            break;
        case MediaRecorder.OutputFormat.RAW_AMR:
            suffix = ".amr";
            break;
        }

        try {
            return File.createTempFile(prefix, suffix, dir);
        } catch (IOException e) {
            Log.e("CallRecorder", "RecordService::makeOutputFile unable to create temp file in " + dir + ": " + e);
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create temp file in " + dir + ": " + e, Toast.LENGTH_LONG);
            t.show();
            return null;
        }
    }

    public void onCreate()
    {
        super.onCreate();
        recorder = new MediaRecorder();
        Log.i("CallRecorder", "onCreate created MediaRecorder object");
    }

    public void onStart(Intent intent, int startId) {
        //Log.i("CallRecorder", "RecordService::onStart calling through to onStartCommand");
        //onStartCommand(intent, 0, startId);
        //}

        //public int onStartCommand(Intent intent, int flags, int startId)
        //{
        Log.i("CallRecorder", "RecordService::onStartCommand called while isRecording:" + isRecording);

        if (isRecording) return;

        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Boolean shouldRecord = prefs.getBoolean(Preferences.PREF_RECORD_CALLS, false);
        if (!shouldRecord) {
            Log.i("CallRecord", "RecordService::onStartCommand with PREF_RECORD_CALLS false, not recording");
            //return START_STICKY;
            return;
        }

        int audiosource = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_SOURCE, "1"));
        int audioformat = Integer.parseInt(prefs.getString(Preferences.PREF_AUDIO_FORMAT, "1"));

        recording = makeOutputFile(prefs);
        if (recording == null) {
            recorder = null;
            return; //return 0;
        }

        Log.i("CallRecorder", "RecordService will config MediaRecorder with audiosource: " + audiosource + " audioformat: " + audioformat);
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

            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);
            
            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                Log.e("CallRecorder", "RecordService::onStart() IOException attempting recorder.prepare()\n");
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e, Toast.LENGTH_LONG);
                t.show();
                recorder = null;
                return; //return 0; //START_STICKY;
            }
            Log.d("CallRecorder", "recorder.prepare() returned");
            
            recorder.start();
            isRecording = true;
            Log.i("CallRecorder", "recorder.start() returned");
            updateNotification(true);
        } catch (java.lang.Exception e) {
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e, Toast.LENGTH_LONG);
            t.show();

            Log.e("CallRecorder", "RecordService::onStart caught unexpected exception", e);
            recorder = null;
        }

        return; //return 0; //return START_STICKY;
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (null != recorder) {
            Log.i("CallRecorder", "RecordService::onDestroy calling recorder.release()");
            isRecording = false;
            recorder.release();
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder finished recording call to " + recording, Toast.LENGTH_LONG);
            t.show();

            /*
            // encrypt the recording
            String keyfile = "/sdcard/keyring";
            try {
                //PGPPublicKey k = readPublicKey(new FileInputStream(keyfile));
                test();
            } catch (java.security.NoSuchAlgorithmException e) {
                Log.e("CallRecorder", "RecordService::onDestroy crypto test failed: ", e);
            }
            //encrypt(recording);
            */
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
            Intent notificationIntent = new Intent(this, RecordService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            mNotificationManager.notify(RECORDING_NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
        }
    }

    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra)
    {
        Log.i("CallRecorder", "RecordService got MediaRecorder onInfo callback with what: " + what + " extra: " + extra);
        isRecording = false;
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) 
    {
        Log.e("CallRecorder", "RecordService got MediaRecorder onError callback with what: " + what + " extra: " + extra);
        isRecording = false;
        mr.release();
    }
}
