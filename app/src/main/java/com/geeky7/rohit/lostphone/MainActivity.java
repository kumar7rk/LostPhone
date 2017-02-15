package com.geeky7.rohit.lostphone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.util.Date;


public class MainActivity extends Activity {
    TextView sms;
    String message = "NoText", sender = "Someone";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sms = (TextView)findViewById(R.id.sms);
        checkPermission();
//        openFrontCamera();
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(intent);
        captureImage();
        /*Intent i = getIntent();
        if (i.getExtras()!=null){
            message = i.getStringExtra("Message");
            sender= i.getStringExtra("Sender");
            if (sender.equals("1") && message.equals("Take Picture")){
            }
            else{
                Main.showToast("Keyword did not match");
            }
        }*/
    }
    private void checkPermission() {
        ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS},
                0);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                0);
    }
    private Camera openFrontCamera() {
        Main.showToast("openFrontCamera called!");
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("Camera", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }
    public void captureImage() {

        // Creating folders for Image
        String imageFolderPath = Environment.getExternalStorageDirectory().toString()
                + "/Kumar";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();

        // Generating file name
     String imageName = new Date().toString() + ".png";

        // Creating image here
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imageFolderPath, imageName)));
        startActivityForResult(takePictureIntent,
                1);

    }
}
