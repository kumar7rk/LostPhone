package com.geeky7.rohit.lostphone.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.geeky7.rohit.lostphone.Mail;
import com.geeky7.rohit.lostphone.Main;
import com.geeky7.rohit.lostphone.R;
import com.geeky7.rohit.lostphone.listeners.OnPictureCapturedListener;
import com.geeky7.rohit.lostphone.services.PictureService;

import java.io.File;
import java.util.TreeMap;


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

        capturedListener = MainActivity.this;

        checkPermission();

        mail = new Mail("rohitkumarrk1992@gmail.com","9780127576");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("Message");
            sender = extras.getString("Sender");
            Log.i("Text","Text is:"+ message);
        }
        // #1 "Hi there, thanks for testing SMSTech. We offer Australia's simplest way to send SMS. No contracts and no monthly fees. Free sign up with 25 messages to t
        // #2 rial: http://bit.ly/smstechtrial

        if (/*message.equals("Take Picture")|| */ "rial: http://bit.ly/smstechtrial" .equals(message)){
            Main.showToast("Text Matched taking picture now");
            new PictureService().startCapturing(this,capturedListener);

        }
    }
    public void checkPermission() {
        ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS);
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
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
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
    public void sendEmail(TreeMap<String,byte[]> takenPictures){
        final File file = new File(Environment.getExternalStorageDirectory()+"/" +"LostPhone/" +"1.jpg");
        String path = file.toString();
        Main.showToast(path);

        Uri U = Uri.fromFile(file);
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.setType("application/image");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"rohitkumarrk1992@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "LostPhone");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Picture taken ");
        emailIntent.putExtra(Intent.EXTRA_STREAM,U);

//        startActivity(emailIntent);
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
    public void sendEmailBackground() throws Exception {
        Log.i("PictureService","Called send email background");
        File file = new File(Environment.getExternalStorageDirectory()+"/" +"LostPhone/" +"1.jpg");
        mail.addAttachment(file.toString());
        Log.i("PictureService","Attachment added");
        boolean send =  mail.send();
        Log.i("PictureService","sent?" +""+ send);
    }
}
