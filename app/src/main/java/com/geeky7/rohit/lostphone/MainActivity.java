package com.geeky7.rohit.lostphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    TextView sms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sms = (TextView)findViewById(R.id.sms);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,1);

    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public void receivedSms(String message)
    {
        Toast.makeText(getApplicationContext(), "receivedSMS called", Toast.LENGTH_SHORT).show();
        sms.setText(message);
    }
}
