package com.udacity.turnbyturn.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.maps.GeoApiContext;

import com.google.maps.RoadsApi;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
//import com.google.maps.model.LatLng;
//import com.google.maps.model.SnappedPoint;
import com.udacity.turnbyturn.MyApplication;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.event.ServicesObserver;
import com.udacity.turnbyturn.services.CollectAddressIntentService;
import com.udacity.turnbyturn.services.DriverTripService;
import com.udacity.turnbyturn.ui.ParentStopLocation;
import com.udacity.turnbyturn.util.Constants;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;


import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.udacity.turnbyturn.util.Constants.FORECAST_COLUMNS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Trip} interface
 * to handle interaction events.
 * Use the {@link Trip#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Trip extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, LocationListener,ServicesObserver{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int LOAD_ALL_STOP = 101;
    private String TAG = Trip.class.getSimpleName();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private GeoApiContext mContext;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions googleSignInOptions;
    private SupportMapFragment mapView;
    private GoogleMap map;
    protected LocationRequest mLocationRequest;
    private Intent intent;
    private Location mLastLocation;

    private ProgressDialog mProgressDialog;
    private AddressResultReceiver mResultReceiver;

    private static final int PAGINATION_OVERLAP = 5;
    private static final int PAGE_SIZE_LIMIT = 100;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private  View tripView;
    private Button button;
    private  boolean receiverRegister;
    public Trip() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment Trip.
     * @param mGoogleApiClient
     */
    // TODO: Rename and change types and number of parameters
    public static Trip newInstance(GoogleApiClient mGoogleApiClient) {
        Trip fragment = new Trip();
        fragment.setmGoogleApiClient(mGoogleApiClient);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        tripView = inflater.inflate(R.layout.fragment_trip, container, false);

        int statusCode = com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
        switch (statusCode) {
            case ConnectionResult.SUCCESS:
                Toast.makeText(this.getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(this.getActivity(), getString(R.string.service_missing), Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(this.getActivity(), getString(R.string.update_req), Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(this.getActivity(), getString(R.string.play_services_result) + statusCode, Toast.LENGTH_SHORT).show();
        }

        // Gets the MapView from the XML layout and creates it
        mapView = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView));
        mapView.onCreate(savedInstanceState);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (mapView != null) {
            mapView.getMapAsync(this);

        }

        MapsInitializer.initialize(this.getActivity());

        button = (Button)tripView.findViewById(R.id.start_stop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().equals(getString(R.string.trip_start))){
                    button.setText(R.string.trip_stop);
                    button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.stoptrip));
                    intent = new Intent(getContext(), DriverTripService.class);
                    intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    getActivity().startService(intent);
                    receiverRegister = true;
                }else {
                    button.setText(R.string.trip_start);
                    button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
                    intent = new Intent(getContext(), DriverTripService.class);
                    intent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                    receiverRegister = false;
                    getActivity().stopService(intent);
                }


            }
        });
        if(isTripServicesRunning(DriverTripService.class,getContext())){
            Button button = (Button)tripView.findViewById(R.id.start_stop);
            button.setText(R.string.trip_stop);
            button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.stoptrip));

        }



        return tripView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, getString(R.string.location_setting));


                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(getmGoogleApiClient());


                        LatLng mapCenter = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 18));
                        map.animateCamera(CameraUpdateFactory.zoomIn());
                        map.setMapType(Constants.MAP_TYPES[Constants.MAP_TYPE_INDEX]);
                        map.getUiSettings().setZoomControlsEnabled(true);


                        map.addMarker(new MarkerOptions()
                                .position(mapCenter)
                                .title(getString(R.string.current_location))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))
                        );

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new com.google.android.gms.maps.model.LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 18);
                        map.animateCamera(cameraUpdate);


                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, getString(R.string.resolution_required));

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                            Log.i(TAG, getString(R.string.pendingintent_unable));
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, getString(R.string.settings_chnag_unavilable));
                        break;
                }
            }
        });


    }

    @Override
    public void onResume() {
        mapView.onResume();
        getLoaderManager().initLoader(LOAD_ALL_STOP, null, this);
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(mapView != null){
//            mapView.onDestroy();
//        }
//        if(receiverRegister && !isTripServicesRunning(DriverTripService.class,getContext())){
//            getContext().unregisterReceiver(broadcastReceiver);
//        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
//        mapView.onLowMemory();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri stopUri = TurnByTurnContract.StopEntry.CONTENT_URI;

        String sortOrder = TurnByTurnContract.StopEntry.SERVERID + " ASC";

        return new CursorLoader(getContext(),
                stopUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            for (int i = 0; i < data.getCount(); i++) {

                com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(Double.valueOf(data.getString(data.getColumnIndex(TurnByTurnContract.StopEntry.LATITUDE))),
                        Double.valueOf(data.getString(data.getColumnIndex(TurnByTurnContract.StopEntry.LONGITUDE))));
                polylineOptions.add(latLng);
                map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop))
                        .anchor(0.0f, 1.0f)
                        .position(latLng)
                        .title(data.getString(data.getColumnIndex(TurnByTurnContract.StopEntry.LANDMARK))));

                data.moveToNext();
            }

            polylineOptions.color(R.color.green);
            polylineOptions.width(20);
//            map.addPolyline(polylineOptions);

        } else {

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }


    @Override
    public void onLocationChanged(Location location) {




        PolylineOptions polylineOptions = new PolylineOptions();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        map.clear();
        Activity activity = getActivity();
        if(activity != null){
            getLoaderManager().initLoader(0, null, this);
        }

        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.current_location))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))
        );

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.animateCamera(cameraUpdate);


    }


    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void startLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle(getString(R.string.google_client_loding));
            mProgressDialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);
            mProgressDialog.show();
        }else {
            mProgressDialog.show();
        }

    }

    @Override
    public void stopLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getContext(),mAddressOutput,Toast.LENGTH_LONG).show();
            }

        }
    }

    private boolean isTripServicesRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i(getString(R.string.service_already),getString(R.string.running));
                return true;
            }
        }
        Log.i(getString(R.string.service_not),getString(R.string.running));
        return false;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getContext(),intent.getExtras().getString(getString(R.string.location)),Toast.LENGTH_LONG).show();
        }
    };


}
