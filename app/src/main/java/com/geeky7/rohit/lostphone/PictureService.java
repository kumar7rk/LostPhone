package com.geeky7.rohit.lostphone;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import com.geeky7.rohit.lostphone.listeners.OnPictureCapturedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;


/**
 * The aim of this service is to secretly take pictures (without preview or opening device's camera app)
 * from all available cameras.
 * @author hzitoun (zitoun.hamed@gmail.com)
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP) //camera 2 api was added in API level 21
public class PictureService {

    private static final String TAG = "PictureService";
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Activity context;
    private WindowManager windowManager;
    private CameraManager manager;
    private TreeMap<String, byte[]> picturesTaken;
    private OnPictureCapturedListener capturedListener;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private String currentCameraId;
    private Queue<String> cameraIds;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /* making camera ready for capturing images
    its get all the available camera's check if an image is already taken
    if not
        it calls openCameraAndTakePicture Method
    else
        it calls method onDoneCapturingAllPhotos
     */
    public void startCapturing(final Activity activity,
                               final OnPictureCapturedListener capturedListener) {
        this.picturesTaken = new TreeMap<>();
        this.context = activity;
        this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.windowManager = context.getWindowManager();
        this.capturedListener = capturedListener;
        this.cameraIds = new LinkedList<>();
        try {
            final String[] cameraIdList = manager.getCameraIdList();
            if (cameraIdList != null && cameraIdList.length != 0) {
                for (final String cameraId : cameraIdList) {
                    this.cameraIds.add(cameraId);
                }
                this.currentCameraId = this.cameraIds.poll();
                //Main.showToast("Opening camera");
                openCameraAndTakePicture();
            } else {
                capturedListener.onDoneCapturingAllPhotos(picturesTaken);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /* it starts a background handler thread
    checks permission for camera and writing external storage
   and opens camera with statecallback object
    */
    private void openCameraAndTakePicture() {
        startBackgroundThread();
        //Log.i(TAG, "opening camera " + currentCameraId);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(currentCameraId, stateCallback, null);
            }
        } catch (CameraAccessException e) {
            Log.i(TAG, " exception opening camera " + currentCameraId + e.getMessage());
        }
    }

    /*
    updates camera's status
    calls takePicture which takes a picture
     */
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "camera " + camera.getId() + " opened");
            cameraDevice = camera;
            Log.i(TAG, "Taking picture from camera " + camera.getId());
            takePicture();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i(TAG, " camera " + camera.getId() + " disconnected");
            if (cameraDevice != null) {
                cameraDevice.close();
            }
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            Log.i(TAG, "camera " + camera.getId() + " closed");
           // stopBackgroundThread();
            if (!cameraIds.isEmpty()) {
                new Handler().postDelayed(() ->
                                takeAnotherPicture()
                        , 100);
            } else {
//                Log.i("PictureService",camera.getId());
                capturedListener.onDoneCapturingAllPhotos(picturesTaken);
            }
        }


        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.i(TAG, "camera in error, int code " + error);
            if (cameraDevice != null) {
                cameraDevice.close();
            } else {
                cameraDevice = null;
            }
        }
    };

    /*
    finally takes a pic with back and front camera
    called twice- one for each camera
     */
    private void takePicture() {
        //Main.showToast("taking picture");
        if (null == cameraDevice) {
            Log.i(TAG, "cameraDevice is null");
            return;
        }
        try {
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                if (characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) != null) {
                    jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                            .getOutputSizes(ImageFormat.JPEG);
                }
            }
            int width = 1;
            int height = 1;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
                //Log.i(TAG,"ImageWH" + width+","+height);
            }
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            final List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//            captureBuilder.set(CaptureRequest.JPEG_QUALITY,(byte)100);
            final int rotation = this.windowManager.getDefaultDisplay().getRotation();
            //Log.i("Rotation",rotation+"");
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            captureBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_SINGLE);
//            captureBuilder.set(CaptureRequest.FLASH_MODE,CaptureRequest.FLASH_MODE_TORCH);
            ImageReader.OnImageAvailableListener readerListener = (ImageReader readerL) -> {
                final Image image = readerL.acquireLatestImage();
                final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                final byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
//                Main.showToast("Calling save image to disk");
                saveImageToDisk(bytes);
                if (image != null) {
                    image.close();
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void saveImageToDisk(final byte[] bytes) {
//        Main.showToast("Saving image");
        Log.i(TAG,"Save image to disk");
        new File(Environment.getExternalStorageDirectory(), "LostPhone").mkdir();
        final File file = new File(Environment.getExternalStorageDirectory()+"/" +"LostPhone", this.cameraDevice.getId()+".jpg");
        //Main.showToast("Image saved at:"+file.toString() + ""+ this.cameraDevice.getId().toString());

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
            this.picturesTaken.put(file.getPath(), bytes);

            InputStream in = new FileInputStream(file);
            Bitmap bm2 = BitmapFactory.decodeStream(in);
            OutputStream stream = new FileOutputStream(file);
            bm2.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            this.picturesTaken.put(file.getPath(), bytes);
            stream.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"IOException in saveImageToDisk");

        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void checkDir(){
        ContextWrapper cw = new ContextWrapper(MyApplication.getAppContext());
        File directory = cw.getDir("LostPhone", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, "1.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
//            resizedbitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
        }
    }
    private void startBackgroundThread() {
        if (mBackgroundThread == null) {
            mBackgroundThread = new HandlerThread("Camera Background" + currentCameraId);
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.i(TAG, "exception stopBackgroundThread" + e.getMessage());
        }
    }


    private void takeAnotherPicture() {
        startBackgroundThread();
        this.currentCameraId = this.cameraIds.poll();
        openCameraAndTakePicture();
    }

    private void closeCamera() {
        //Log.i(TAG, "closing camera " + cameraDevice.getId());
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }


    final private CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (picturesTaken.lastEntry() != null) {
                capturedListener.onCaptureDone(picturesTaken.lastEntry().getKey(), picturesTaken.lastEntry().getValue());
                Log.i(TAG, "done taking picture from camera " + cameraDevice.getId());
            }
            closeCamera();
        }
    };
}
