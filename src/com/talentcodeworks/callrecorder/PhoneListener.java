package com.talentcodeworks.callrecorder;

import android.content.Intent;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class PhoneListener extends PhoneStateListener
{
    private Context context;

    public PhoneListener(Context c) {
        context = c;
    }

    public void onCallStateChanged (int state, String incomingNumber)
    {
        Log.d("CallRecorder", "PhoneStateListener::onCallStateChanged state:" + state + " incomingNumber:" + incomingNumber);

        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            Log.d("CallRecorder", "CALL_STATE_IDLE, stoping recording");
            context.stopService(new Intent(context, RecordService.class));
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            Log.d("CallRecorder", "CALL_STATE_RINGING");
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            Log.d("CallRecorder", "CALL_STATE_OFFHOOK starting recording");
            context.startService(new Intent(context, RecordService.class));
            break;
        }
    }
}
