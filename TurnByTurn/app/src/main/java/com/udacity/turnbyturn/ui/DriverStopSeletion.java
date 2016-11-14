package com.udacity.turnbyturn.ui;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v4.app.ListFragment;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.otto.Subscribe;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.adapter.ContactsAdapter;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.event.ActivityResultBus;
import com.udacity.turnbyturn.event.ActivityResultEvent;
import com.udacity.turnbyturn.model.SelectedContact;
import com.udacity.turnbyturn.services.PULLParentsStop;
import com.udacity.turnbyturn.rest.TurnByTurnClient;
import com.udacity.turnbyturn.util.Config;
import com.udacity.turnbyturn.util.Constants;
import com.udacity.turnbyturn.util.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverStopSeletion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverStopSeletion extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {


    private PlacePicker.IntentBuilder builder;

    private ListView listView;
    private ContactsAdapter contactsAdapter;
    private List<SelectedContact> selectedContacts;
    private ImageLoader mImageLoader;
    private static final int PLACE_PICKER_FLAG = 1;
    private String TAG = DriverStopSeletion.class.getName();
    private ImageButton imageButton;
    private EditText driver_stop_landMark;
    private EditText driver_stop_address;
    private SupportMapFragment mMapFragment;
    private GoogleMap googleMap;
    private Button button;
    private FloatingActionButton floatingActionButton;


    private int MAP_TYPE_INDEX = 1;

    private OnFragmentInteractionListener mListener;

    private JSONArray contactWithAddress;

    private JSONObject jsonObject;

    private LatLng latLng;
    public DriverStopSeletion() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment DriverStopSeletion.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverStopSeletion newInstance(JSONObject userProfile, GoogleSignInOptions googleSignInOptions, GoogleApiClient mGoogleApiClient) {
        DriverStopSeletion fragment = new DriverStopSeletion();
        fragment.setJsonObject(userProfile);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View inflate =  inflater.inflate(R.layout.fragment_driver_stop_seletion, container, false);

        mMapFragment = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map));

        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gmap) {
                googleMap = gmap;
            }
        });

       driver_stop_landMark = (EditText)inflate.findViewById(R.id.driver_stop_landmark);
       driver_stop_address = (EditText)inflate.findViewById(R.id.driver_stop_address);



       imageButton = (ImageButton) inflate.findViewById(R.id.pick_location);
       imageButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               try {
                   builder = new PlacePicker.IntentBuilder();

                   Intent intent = builder.build(getActivity());

                   startActivityForResult(intent, PLACE_PICKER_FLAG);
               } catch (GooglePlayServicesRepairableException e) {
                   GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(),getActivity(), 0);
               } catch (GooglePlayServicesNotAvailableException e) {
                   Toast.makeText(getContext(), getString(R.string.googleservicesnotfount),
                           Toast.LENGTH_LONG)
                           .show();
               }
           }
       });

        floatingActionButton = (FloatingActionButton)inflate.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(TextUtils.isEmpty(driver_stop_landMark.getText().toString()) || TextUtils.isEmpty(driver_stop_address.getText().toString())){
                    Snackbar snackbar = Snackbar
                            .make(getView(),getString(R.string.select_location), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    imageButton.requestFocus();
                    return;
                }


                JSONArray contactInvitation = new JSONArray();
                JSONObject contactsStops = new JSONObject();
                for(int key:contactsAdapter.getSelectedContactMap().keySet()){

                    try {

                        String contactNumber = contactsAdapter.getSelectedContactMap().get(key).getString(getString(R.string.contactnumber)).trim().replaceAll("\\s+","");

                        if(!contactNumber.startsWith(getString(R.string.phonecountry_code))){
                            contactNumber = ( contactNumber.startsWith("0")) ? contactNumber.replaceFirst("0",getString(R.string.phonecountry_code)) : getString(R.string.phonecountry_code)+contactNumber;
                        }


                        if(contactNumber.startsWith(getString(R.string.phonecountry_code)) && contactNumber.length() == 13){
                            contactInvitation.put(contactNumber);
                            sendInvitationSms(contactsAdapter.getSelectedContactMap().get(key).getString(getString(R.string.contactNumber)), getString(R.string.invitation));

                        }else{
                            Snackbar snackbar = Snackbar
                                    .make(getView(),contactNumber + getString(R.string.invalid), Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {

                contactsStops.put(getString(R.string.lat),String.valueOf(latLng.latitude));
                contactsStops.put(getString(R.string.longi),String.valueOf(latLng.longitude));
                contactsStops.put(getString(R.string.landmark),driver_stop_landMark.getText().toString());
                contactsStops.put(getString(R.string.locationaddress),driver_stop_address.getText().toString());
                contactsStops.put(getString(R.string.contacts),contactInvitation);


                contactWithAddress.put(contactsStops);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                driver_stop_landMark.setText("");
                driver_stop_address.setText("");
                contactsAdapter.notifyDataSetChanged();
                googleMap.clear();
            }
        });


        button = (Button)inflate.findViewById(R.id.invite_to_parent);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    final JSONObject jsonObject = getJsonObject().put(getString(R.string.invitation_data),contactWithAddress);
                    Log.e("DRIVERSTOP",jsonObject.toString());
                    Call<JsonElement> siginResponse = TurnByTurnClient.get().sigin(jsonObject);

                    siginResponse.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                            try {

                                JsonObject responseSigin = response.body().getAsJsonObject();
                                jsonObject.put(getString(R.string.response),responseSigin);

                                if(responseSigin.get(getString(R.string.asdriver)).getAsString().equals(R.string.success)){


                                    /**
                                     * Insert User Account details in Database,
                                     */
                                    ContentValues accountValues = new ContentValues();
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.NAME, jsonObject.getString(getString(R.string.username)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.PHOTO_URL, jsonObject.getString(getString(R.string.userprofilepic)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.EMAIL, jsonObject.getString(getString(R.string.useremail)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.G_ID, jsonObject.getString(getString(R.string.usergid)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.ID_TOKEN, "");
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.CONTACT_NUMBER,jsonObject.getString(getString(R.string.drivercontactnumber)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.BUS_NUMBER, jsonObject.getString("name"));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.DRIVER_ID, "0");
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.IMEI,jsonObject.getString(getString(R.string.imei)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.SERVER_AUTH_CODE,jsonObject.getString(getString(R.string.serverauthcode)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.USER_TYPE,jsonObject.getString(getString(R.string.user_profiletype)));
                                    accountValues.put(TurnByTurnContract.UserAccountEntry.SERVER_ID,responseSigin.get(getString(R.string.driverid)).getAsString());


                                    Uri insertedUri = getContext().getContentResolver().insert(TurnByTurnContract.UserAccountEntry.CONTENT_URI,
                                            accountValues);

                                    long locationId = ContentUris.parseId(insertedUri);



                                    /**
                                     * Insert Stop Account details in Database,
                                     */
                                    ContentValues[] stopValues = new ContentValues[responseSigin.get(getString(R.string.stops)).getAsJsonArray().size()];

                                    JsonArray jsonarray = responseSigin.get(getString(R.string.stops)).getAsJsonArray();
                                    HashSet<String> stop  = new HashSet<>();
                                    for(int i=0;i<stopValues.length;i++){

                                        stopValues[i] = new ContentValues();

                                        stopValues[i].put(TurnByTurnContract.StopEntry.LATITUDE, String.valueOf(jsonarray.get(i).getAsJsonObject().get(getString(R.string.latitude))).replaceAll("^\"|\"$", ""));
                                        stopValues[i].put(TurnByTurnContract.StopEntry.LONGITUDE, String.valueOf(jsonarray.get(i).getAsJsonObject().get(getString(R.string.longitude))).replaceAll("^\"|\"$", ""));
                                        stopValues[i].put(TurnByTurnContract.StopEntry.LANDMARK, String.valueOf(jsonarray.get(i).getAsJsonObject().get(getString(R.string.landmark))).replaceAll("^\"|\"$", ""));
                                        stopValues[i].put(TurnByTurnContract.StopEntry.ADDRESS, String.valueOf(jsonarray.get(i).getAsJsonObject().get(getString(R.string.locationaddress))).replaceAll("^\"|\"$", ""));
                                        stopValues[i].put(TurnByTurnContract.StopEntry.SERVERID,String.valueOf(jsonarray.get(i).getAsJsonObject().get(getString(R.string.stopID))).replaceAll("^\"|\"$", ""));
                                        stop.add(String.valueOf(jsonarray.get(i).getAsJsonObject().get(getString(R.string.stopID))));
                                    }

                                    int stopinsert = getContext().getContentResolver().bulkInsert(TurnByTurnContract.StopEntry.CONTENT_URI,
                                            stopValues);


                                    new PULLParentsStop(getContext()).execute(stop);
                                    SharedPreferences pref = getContext().getSharedPreferences(Config.SHARED_PREF, 0);
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(getString(R.string.pushtype),getString(R.string.welcome_push));
                                    jsonObject.put(getString(R.string.deviceregisterid),pref.getString(getString(R.string.regId), null));

                                    Call<JsonElement> welcome = TurnByTurnClient.get().sendPush(jsonObject);
                                    welcome.enqueue(new Callback<JsonElement>() {
                                        @Override
                                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                                        }

                                        @Override
                                        public void onFailure(Call<JsonElement> call, Throwable t) {

                                        }
                                    });

                                    mListener.onFragmentInteraction(jsonObject,R.string.start_profile);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<JsonElement> call, Throwable t) {

                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        mImageLoader = new ImageLoader(getActivity(), getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {

                return loadContactPhotoThumbnail((String) data, getImageSize());
            }
        };
        mImageLoader.setLoadingImage(R.mipmap.ic_launcher);
        mImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
        contactsAdapter = new ContactsAdapter(getContext(),selectedContacts);
        contactsAdapter.setmImageLoader(mImageLoader);
        setListAdapter(contactsAdapter);

        contactWithAddress = new JSONArray();

        return inflate;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PLACE_PICKER_FLAG:
                    Place place = PlacePicker.getPlace(getContext(), data);
                    driver_stop_landMark.setText(place.getName());
                    driver_stop_address.setText(place.getAddress());


                    googleMap.clear();

                    latLng = place.getLatLng();

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                    googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                    googleMap.setMapType(Constants.MAP_TYPES[Constants.MAP_TYPE_INDEX]);

                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
                    CameraPosition cameraPosition = CameraPosition.builder()
                            .target(latLng)
                            .zoom(18f)
                            .bearing(0.0f)
                            .tilt(0.0f)
                            .build();

                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                            2000, null);
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.bus);
                    googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.pick_drop))
                            .icon(icon)
                    );

                    break;
            }
        }
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

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS},
                    0);
            return;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public void onPause() {
        super.onPause();

    }

    @Subscribe
    public void onActivityResultReceived(ActivityResultEvent event){
       onActivityResult(event.getRequestCode(),event.getResultCode(),event.getData());

    }




    private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {

        if (!isAdded() || getActivity() == null) {
            return null;
        }

        AssetFileDescriptor afd = null;

        try {
            Uri thumbUri;

            final Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, photoData);

            thumbUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

            afd = getActivity().getContentResolver().openAssetFileDescriptor(thumbUri, "r");

            FileDescriptor fileDescriptor = afd.getFileDescriptor();

            if (fileDescriptor != null) {

                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        fileDescriptor, imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {


        } finally {

            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {

                }
            }
        }

        // If the decoding failed, returns null
        return null;
    }
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {}
        });

        getLoaderManager().initLoader(ContactsAdapter.ContactsQuery.QUERY_ID, null, this);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == ContactsAdapter.ContactsQuery.QUERY_ID) {


            return new CursorLoader(getActivity(),
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] { ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI},
                    ContactsContract.Contacts.DISPLAY_NAME +
                            "<>''" + " AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1",
                    null,
                    ContactsAdapter.ContactsQuery.SORT_ORDER);
        }

        Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == ContactsAdapter.ContactsQuery.QUERY_ID) {
            contactsAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == ContactsAdapter.ContactsQuery.QUERY_ID) {
            contactsAdapter.swapCursor(null);
        }
    }


    private void sendInvitationSms(String mobNo, String message) {
        String smsSent = getString(R.string.sms_send);
        String smsDelivered = getString(R.string.sms_delivered);
        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0,
                new Intent(smsSent), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(getActivity(), 0,
                new Intent(smsDelivered), 0);



        // Receiver for Sent SMS.
        getActivity().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Snackbar snackbar = Snackbar
                                .make(getView(),getString(R.string.invitation_send), Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getContext(), getString(R.string.generic_failure),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getContext(), getString(R.string.no_service),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getContext(), getString(R.string.nullpdu),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getContext(), getString(R.string.radio_off),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(smsSent));

        // Receiver for Delivered SMS.
        getActivity().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:

                        Snackbar snackbar = Snackbar
                                .make(getView(),getString(R.string.invitation_delivered), Snackbar.LENGTH_SHORT);
                        snackbar.show();

                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getContext(), getString(R.string.sms_not_delivered),
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(smsDelivered));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mobNo, null, message, sentPI, deliveredPI);
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    }


