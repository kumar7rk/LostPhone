package com.geeky7.rohit.lostphone.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;

import com.geeky7.rohit.lostphone.Mail;
import com.geeky7.rohit.lostphone.Main;
import com.geeky7.rohit.lostphone.PictureService;
import com.geeky7.rohit.lostphone.R;
import com.geeky7.rohit.lostphone.listeners.OnPictureCapturedListener;
import com.geeky7.rohit.lostphone.services.LocationService;

import java.io.File;
import java.util.TreeMap;

import static android.R.attr.x;


public class MainActivity extends Activity implements OnPictureCapturedListener, ActivityCompat.OnRequestPermissionsResultCallback{
    TextView sms;
    Mail mail;
    String message = "No text", sender;
    private OnPictureCapturedListener capturedListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sms = (TextView)findViewById(R.id.sms);

        checkPermission();

        capturedListener = MainActivity.this;
        mail = new Mail("rohitkumarrk1992@gmail.com","9780127576");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            Log.i("Text","Text is:"+ message);
        }
        if ("Take Picture".equals(message)|| "Kithe".equals(message)){
            Main.showToast("Text Matched taking picture now");
            new PictureService().startCapturing(this,capturedListener);
//            startService();
        }
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        sendSMS();
        if (!enabled){
              handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (enabled)
                        startService();
                }
            }, 60000);
        }



    }
    private void sendSMS() {
        SmsManager manager = SmsManager.getDefault();
        String text = "Location Unavailable, Retrying in 1 min";
        manager.sendTextMessage("+61410308348",null, text, null, null);
        SmsManager manager1 = SmsManager.getDefault();
        manager1.sendTextMessage("+61430736226",null, text, null, null);
    }

    private void startService() {
        Intent serviceIntent = new Intent(getApplicationContext(), LocationService.class);
        startService(serviceIntent);
    }
    public void checkPermission() {
        /*ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS);*/
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS},
                0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);
    }
        @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        try {
            sendEmailBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendEmailBackground() throws Exception {
        Log.i("PictureService","Called send email background");
        File file = new File(Environment.getExternalStorageDirectory()+"/" +"LostPhone/" +"1.jpg");
        mail.addAttachment(file.toString());
        Log.i("PictureService","Attachment added");
        boolean send =  mail.send();
        Log.i("PictureService","sent?" +""+ send);
    }


    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
