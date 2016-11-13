package com.udacity.turnbyturn.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import com.udacity.turnbyturn.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by TechSutra on 11/12/16.
 */

public class SendTripData extends IntentService {

    private static final String TAG = SendTripData.class.getSimpleName();


    protected ResultReceiver mReceiver;
    private String userid;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    public SendTripData() {
        super("Send Trip Data To Server");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

//        userid = intent.getStringExtra("us")

        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");

        }
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        if (location == null) {
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),1);


            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }try {
                                JSONObject currentLocation = new JSONObject();
                                currentLocation.put("userid",1);
                                currentLocation.put("latitude",location.getLatitude());
                                currentLocation.put("longitude",location.getLatitude());
                                currentLocation.put("address",TextUtils.join(System.getProperty("line.separator"), addressFragments));
                                currentLocation.put("speed",location.getSpeed());
                                Socket s = new Socket("128.199.218.81", 5000);
                                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                                dout.writeBytes(currentLocation.toString());
                                dout.flush();
                                dout.close();
                                s.close();


                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

        } catch (IOException ioException) {
        } catch (IllegalArgumentException illegalArgumentException) {
        }
        if (addresses == null || addresses.size()  == 0) {
            deliverResultToReceiver(Constants.FAILURE_RESULT, "Not Found");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }

        stopSelf();
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
//        mReceiver.send(resultCode, bundle);
    }
}
