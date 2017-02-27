package com.geeky7.rohit.lostphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.geeky7.rohit.lostphone.activities.MainActivity;

/**
 * Created by Rohit on 8/07/2016.
 */
public class SMSReceiver extends BroadcastReceiver {

    //final SmsManager sms = SmsManager.getDefault();

    public void onReceive(Context context, Intent intent) {

        String message = "";
        String senderNum = "";
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    senderNum = phoneNumber;
                    message = currentMessage.getDisplayMessageBody();
                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,"senderNum: "+ senderNum + ", message: " + message, duration);
                 //   toast.show();
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
        intent.putExtra("Message", message);
        intent.putExtra("Sender", senderNum);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, MainActivity.class);
        context.startActivity(intent);
    }
}