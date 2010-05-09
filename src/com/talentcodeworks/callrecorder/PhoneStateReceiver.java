package com.talentcodeworks.callrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent) {
        Log.d("PhoneStateReceiver::onReceive", "got broadcast message");
        PhoneListener phoneListener = new PhoneListener(context);
        TelephonyManager telephony = (TelephonyManager)
            context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        Log.d("PhoneStateReceiver::onReceive", "set PhoneStateListener");
    }
}
