package com.udacity.turnbyturn.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.squareup.otto.Subscribe;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.UserProfile;
import com.udacity.turnbyturn.event.ActivityResultBus;
import com.udacity.turnbyturn.event.ActivityResultEvent;
import com.udacity.turnbyturn.services.CollectAddressIntentService;
import com.udacity.turnbyturn.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ParentStopLocation#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParentStopLocation extends Fragment implements LocationListener {

    private String TAG = ParentStopLocation.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions googleSignInOptions;

    private SupportMapFragment mMapFragment;
    private EditText parentLandmark;
    private GoogleMap googleMap;

    private OnFragmentInteractionListener mListener;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected LocationRequest mLocationRequest;

    protected Boolean mRequestingLocationUpdates;



    protected static final int REQUEST_CHECK_SETTINGS = 0x1;




    protected String mAddressOutput;
//    protected boolean mAddressRequested;
    private ProgressBar mProgressBar;

    private AddressResultReceiver mResultReceiver;

    private EditText edit_text_address;
    private Button button;
    private JSONObject userProfile;
    private Location mLastLocation;
    public ParentStopLocation() {
        // Required empty public constructor
    }


    public static ParentStopLocation newInstance(JSONObject jsonObject, GoogleSignInOptions googleSignInOptions, GoogleApiClient googleApiClient) {
        ParentStopLocation parentStopLocation = new ParentStopLocation();
        parentStopLocation.setGoogleSignInOptions(googleSignInOptions);
        parentStopLocation.setmGoogleApiClient(googleApiClient);
        parentStopLocation.setUserProfile(jsonObject);
        return parentStopLocation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parent_stop_location, container, false);
        mResultReceiver = new AddressResultReceiver(new Handler());

        edit_text_address = (EditText) rootView.findViewById(R.id.edit_text_address);
        parentLandmark = (EditText) rootView.findViewById(R.id.editText);
        button = (Button) rootView.findViewById(R.id.profile_selection_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject parentAddress = getUserProfile();
                try {
                    parentAddress.put(getString(R.string.landmark), parentLandmark.getText().toString());
                    parentAddress.put(getString(R.string.locationaddress), edit_text_address.getText().toString());
                    parentAddress.put(getString(R.string.lat), String.valueOf(mLastLocation.getLatitude()));
                    parentAddress.put(getString(R.string.longi), String.valueOf(mLastLocation.getLongitude()));
                    mListener.onFragmentInteraction(parentAddress, R.string.invite_contact_list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));

        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gmap) {

                createLocationRequest();
                checkLocationSettings();
                googleMap = gmap;
                try {
                    setCurrentLocation();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true); // To hide never button on the Popup dialog.

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                Toast.makeText(getContext(), status.getStatusMessage(),Toast.LENGTH_LONG).show();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, getString(R.string.location_sucess));

                        try {
                            setCurrentLocation();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, getString(R.string.resolution_required));

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, getString(R.string.request_check_setting));
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, getString(R.string.settings_chnages));
                        break;
                }
            }
        });
    }



    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + getString(R.string.implementListner));
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(getmGoogleApiClient(),  this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                // setButtonsEnabledState();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            setCurrentLocation();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentLocation() throws JSONException {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(getmGoogleApiClient());
        if (mLastLocation != null) {
//            mAddressRequested = true;
            startIntentService(mLastLocation);
            googleMap.clear();
            final LatLng mapCenter = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapCenter, 18));
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            googleMap.setMapType(Constants.MAP_TYPES[Constants.MAP_TYPE_INDEX]);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new com.google.android.gms.maps.model.LatLng(mLastLocation.getLatitude(),  mLastLocation.getLongitude()), 16);
            googleMap.animateCamera(cameraUpdate);

            Glide.with(getContext()).
                        load(userProfile.getString(getString(R.string.photourl)))
                        .asBitmap()
                        .fitCenter()
                        .into(new SimpleTarget<Bitmap>(120, 120) {
                            @Override public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                googleMap.addMarker(new MarkerOptions()
                                        .position(mapCenter)
                                        .title(getString(R.string.pick_drop))
                                        .icon(BitmapDescriptorFactory.fromBitmap(resource))
                                );
                            }});
        }

    }


    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public GoogleSignInOptions getGoogleSignInOptions() {
        return googleSignInOptions;
    }

    public void setGoogleSignInOptions(GoogleSignInOptions googleSignInOptions) {
        this.googleSignInOptions = googleSignInOptions;
    }

    protected void startIntentService(Location mLastLocation) {
        Intent intent = new Intent(getActivity(), CollectAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        getActivity().startService(intent);
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

            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) {
                edit_text_address.setText(mAddressOutput);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, getString(R.string.result_ok));
                        try {
                            Toast.makeText(getContext(),resultCode+getString(R.string.ok),Toast.LENGTH_LONG).show();
                            setCurrentLocation();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, getString(R.string.result_canceled));
                        break;
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        ActivityResultBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ActivityResultBus.getBus().unregister(this);
    }

    @Override
    public  void onPause(){
        super.onPause();
    }


    @Subscribe
    public void onActivityResultReceived(ActivityResultEvent event){
        onActivityResult(event.getRequestCode(),event.getResultCode(),event.getData());
    }

    public JSONObject getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(JSONObject userProfile) {
        this.userProfile = userProfile;
    }
}
