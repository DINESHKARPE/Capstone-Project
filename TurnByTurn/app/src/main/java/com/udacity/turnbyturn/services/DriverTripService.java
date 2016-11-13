package com.udacity.turnbyturn.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.udacity.turnbyturn.MyApplication;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.TurnByTurn;
import com.udacity.turnbyturn.UserProfile;
import com.udacity.turnbyturn.event.ServicesObserver;
import com.udacity.turnbyturn.ui.ParentStopLocation;
import com.udacity.turnbyturn.util.Constants;

import java.util.Date;

import android.support.v4.app.NotificationCompat;

/**
 * Created by TechSutra on 11/10/16.
 */

public class DriverTripService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private String TAG = DriverTripService.class.getName();

    public static final String BROADCAST_LOCATION_ACTION = "com.udacity.turnbyturn.userprofile";

    private final Handler handler = new Handler();
    private Intent driverTrip;

    private GoogleApiClient mLocationClient;
    protected LocationRequest mLocationRequest;
    private Notification notification;

    protected String mAddressOutput;

//    private AddressResultReceiver mResultReceiver;

    private ProgressDialog mProgressDialog;

    private ServicesObserver servicesObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {

            Intent intent = new Intent(getApplicationContext(), SendTripData.class);
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
            getApplicationContext().startService(intent);

        }


    }

    private Runnable updateLocation = new Runnable() {
        public void run() {
            Log.d(TAG, "entered DisplayLoggingInfo");
            driverTrip.putExtra("time", new Date().toString());
            sendBroadcast(driverTrip);
            handler.postDelayed(this, 1000);

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(100);


        if(mLocationClient!= null){
            mLocationClient.connect();

            if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                Log.i(TAG, "Received Start Foreground Intent ");
                Intent notificationIntent = new Intent(this, UserProfile.class);
                notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);

                Intent previousIntent = new Intent(this, DriverTripService.class);
                previousIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                        previousIntent, 0);

                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher);

                notification = new NotificationCompat.Builder(this)
                        .setContentTitle("TurnByTurn School Bus Trip")
                        .setTicker("TurnByTurn School Bus Trip")
                        .setContentInfo("TurnByTurn is running")
                        .setSmallIcon(R.drawable.logo)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true).build();

                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        notification);


            } else if (intent.getAction().equals(
                    Constants.ACTION.STOPFOREGROUND_ACTION)) {
                Log.i(TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
                mLocationClient = null;
            }
        }


        return START_STICKY;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, mLocationRequest, this);


    }

}
