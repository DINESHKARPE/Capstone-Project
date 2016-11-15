package com.udacity.turnbyturn.ui;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.adapter.ContactsAdapter;
import com.udacity.turnbyturn.model.SelectedContact;
import com.udacity.turnbyturn.rest.TurnByTurnClient;
import com.udacity.turnbyturn.util.ImageLoader;
import com.udacity.turnbyturn.util.ProfileType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link }
 * interface.
 */
public class ContactListFragment extends ListFragment implements
         LoaderManager.LoaderCallbacks<Cursor> {

    private String TAG = ContactListFragment.class.getName();

    private ContactsAdapter contactsAdapter;
    private OnFragmentInteractionListener mListener;
    private JSONObject jsonObject;
    private ContentResolver contentResolver;
    private ImageLoader mImageLoader;
    private List<SelectedContact> selectedContacts;



    public ContactListFragment() {
    }



    public static ContactListFragment newInstance(JSONObject jsonObject) {
        ContactListFragment contactListFragment = new ContactListFragment();
        contactListFragment.setJsonObject(jsonObject);

        return contactListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedContacts = new ArrayList<>();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View contactList = inflater.inflate(R.layout.fragment_item_list, container, false);
        Button  button = (Button) contactList.findViewById(R.id.contact_invite_button);
        TextView editText = (TextView)contactList.findViewById(R.id.invite_info);
        try {
            if(getJsonObject().get(getString(R.string.user_profiletype)).toString().equals(ProfileType.DRIVER.toString())){
                editText.setText(getString(R.string.select_parent));
            }else{
                editText.setText(getString(R.string.select_driver));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final JSONObject jsonObject = getJsonObject();
                    JSONArray contactInvitation = new JSONArray();

                    for(int key:contactsAdapter.getSelectedContactMap().keySet()){

                            contactInvitation.put(contactsAdapter.getSelectedContactMap().get(key).getString(getString(R.string.contactnumber)));
                    }
                    jsonObject.put(getString(R.string.invitation_data),contactInvitation);

                    if(jsonObject.get(getString(R.string.profile_type_parent)).toString().equals(ProfileType.DRIVER.toString())){
                        jsonObject.put(getString(R.string.parent),contactsAdapter.getSelectedContactMap());
//                      mListener.onFragmentInteraction(jsonObject,"INVITE_PARENTS_WITH_LOCATION");
                    }else {
                        Call<JsonElement> siginResponse = TurnByTurnClient.get().sigin(jsonObject);
                        final String invitationNumber = contactInvitation.get(0).toString();

                        siginResponse.enqueue(new Callback<JsonElement>() {
                            @Override
                            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                                try {
                                JsonObject responseSigin = response.body().getAsJsonObject();
                                jsonObject.put(getString(R.string.response),responseSigin);

                                    if(responseSigin.get(getString(R.string.siginasparent)).getAsString().equals(getString(R.string.success))){
                                        sendInvitationSms(invitationNumber, getString(R.string.invitation));
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

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


          return contactList;
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
    public void onPause() {
        super.onPause();
        mImageLoader.setPauseWork(false);
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
    public void onDestroy() {
        super.onDestroy();
//        if (cursor != null) {
//            c.close();
//        }
//        if (db != null) {
//            db.close();
//        }
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
