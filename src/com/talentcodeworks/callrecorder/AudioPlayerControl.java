package com.talentcodeworks.callrecorder;

import android.media.MediaPlayer;
import android.widget.MediaController;
import android.net.Uri;
import android.util.Log;
import android.widget.LinearLayout;

//
// It seems weird that the SDK provides a video implementation of 
// a MediaController.MediaPlayerControl (VideoView) but no audio 
// equivalent.
//

class AudioPlayerControl
    implements MediaController.MediaPlayerControl
{
    private static final String TAG = "CallRecorder";

    private MediaPlayer player = null;
    private String path = null;

    public AudioPlayerControl(String path, CallPlayer listenerActivity) 
        throws java.io.IOException
    {
        Log.i(TAG, "AudioPlayerControl constructed with path " + path);
        this.path = path;

        player = new MediaPlayer();
        player.setDataSource(path);

        player.setOnPreparedListener(listenerActivity);
        player.setOnInfoListener(listenerActivity);
        player.setOnErrorListener(listenerActivity);
        player.setOnCompletionListener(listenerActivity);
        /*
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    Log.i(TAG, "AudioPlayerControl onCompletion called");
                    player.reset();
                }
            });
        */
        player.prepareAsync();
    }

    //
    // MediaController.MediaPlayerControl implementation
    //
    public boolean canPause() { return true; }
    public boolean canSeekBackward() { return true; }
    public boolean canSeekForward() { return true; }

    public int getBufferPercentage() {
        Log.d(TAG, "AudioPlayerControl::getBufferPercentage returning 100");
        return 100;
    }

    public int getCurrentPosition() { 
        int pos = player.getCurrentPosition();
        Log.d(TAG, "AudioPlayerControl::getCurrentPosition returning " + pos);
        return pos;
    }

    public int getDuration() {
        int duration = player.getDuration();
        Log.d(TAG, "AudioPlayerControl::getDuration returning " + duration);
        return duration;
    }

    public boolean isPlaying() {
        boolean isp = player.isPlaying();
        Log.d(TAG, "AudioPlayerControl::isPlaying returning " + isp);
        return isp;
    }

    public void pause() {
        Log.d(TAG, "AudioPlayerControl::pause");
        player.pause();
    }

    public void seekTo(int pos) {
        Log.d(TAG, "AudioPlayerControl::seekTo " + pos);
        player.seekTo(pos);
    }

    public void start() {
        Log.d(TAG, "AudioPlayerControl::start");
        player.start();
    }

    public void destroy() {
        Log.i(TAG, "AudioPlayerControll::destroy shutting down player");
        if (player != null) {
            player.reset();
            player.release();
            player = null;
        }
    }
}
