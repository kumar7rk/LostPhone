package com.geeky7.rohit.lostphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Rohit on 8/07/2016.
 */
public class SMS extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null)
            {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj .length; i++)
                {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[])                                                                                                    pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String senderNum = phoneNumber ;
                    String message = currentMessage .getDisplayMessageBody();
                    Toast.makeText(context,"message:"+message,Toast.LENGTH_SHORT).show();
                    try
                    {
                        if (senderNum.equals("1010"))
                        {
                        }
                    }
                    catch(Exception e){}
                }
            }
        } catch (Exception e){
          }
    }
}
