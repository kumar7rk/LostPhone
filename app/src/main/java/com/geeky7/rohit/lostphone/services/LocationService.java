//new
package com.geeky7.rohit.lostphone.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.geeky7.rohit.lostphone.Main;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LocationService extends Service implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,LocationListener{

    public static final String TAG = "LocationService";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS/2;

    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mlocationRequest;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;

    private boolean googleApiClientConnected;
    static Context context;

    Main m;

    Geocoder geocoder;
    List<Address> addresses;
    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        m = new Main(getApplicationContext());
        mLastUpdateTime = "";

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // googleAPI is connected and ready to get location updates- start fetching current location
        if(mGoogleApiClient.isConnected()&&mRequestingLocationUpdates)
            startLocationupdates();
        geocoder = new Geocoder(this, Locale.getDefault());
        Log.i(TAG,"LocationService Created");
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Main.showToast("BackgroundServiceDestroyed");
        stopSelf();
        if (mGoogleApiClient.isConnected())
            stopLocationupdates();
        mGoogleApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    // add the API and builds a client
    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(LocationService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        createLocationRequest();
    }

    // method- fetch location every `UPDATE_INTERVAL_IN_MILLISECONDS` milliseconds
    private void createLocationRequest() {
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }
    // method- update the new coordinates
    protected void updateToast(){
//        Main.showToast("From A-SUM New Coordinates: " + mCurrentLocation.getLatitude() + "\n" + mCurrentLocation.getLongitude());
        Log.i(TAG, mCurrentLocation.getLatitude() + "\n" + mCurrentLocation.getLongitude());
        String address = setAddress();
        Log.i(TAG,address);
    }
    // fetch location now
    protected void startLocationupdates() throws SecurityException{
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mlocationRequest, this);
    }
    // location update no longer needed;
    protected void stopLocationupdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle)throws SecurityException {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean b = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // if location null, get last known location, updating the time so that we don't show quite old location
        if (mCurrentLocation==null){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            if (b){
                sendSMS();
                updateToast();
            }
            else if (!b)
                m.openLocationSettings(manager);
        }

        if (mRequestingLocationUpdates)
            startLocationupdates();

        googleApiClientConnected = true;
    }

    //check is user wants to monitor walking, if yes then listen to the recognised activity;
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        //Main.showToast("I'm called- onLocationChanged");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        try {
            addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateToast();
        //sendSMS();

    }

    private void sendSMS() {
        String address = setAddress();
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage("+61410308348",null, address, null, null);
//        Main.showToast("SMS Sent");
        SmsManager manager1 = SmsManager.getDefault();
        manager1.sendTextMessage("+61430736226",null, address, null, null);
//        Main.showToast("SMS Sent");
    }
    private String setAddress() {
        String address1 = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();
        return address1 + " " + city + "\n" + state + " " + postalCode;
    }
}

