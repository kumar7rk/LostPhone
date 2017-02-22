package com.geeky7.rohit.lostphone.listeners;

import java.util.TreeMap;

/**
 * Photo capturing listener
 * @author hzitoun (hamed.zitoun@gmail.com)
 */
public interface OnPictureCapturedListener {
    
    /**
    * a callback called when we've done taking a picture from a single camera 
    * (use this method if you don't want to wait for ALL taken pictures to be ready @see onDoneCapturingAllPhotos) 
    * @param pictureUrl  taken picture's location on the device
    * @param pictureData taken picture's data as a byte array
    */
    void onCaptureDone(String pictureUrl, byte[] pictureData);
    
    /**
    * a callback called when we've done taking pictures from ALL AVAILABLE cameras
    * OR when NO camera was detected on the device
    * @param picturesTaken : a  Map<PictureUrl, PictureData>
    */
    void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken);
  
}
