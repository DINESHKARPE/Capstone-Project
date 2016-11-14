package com.udacity.turnbyturn.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.udacity.turnbyturn.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DriverBusDetails#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverBusDetails extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText driverBusNumber;
    private EditText driverContactNumber;
    private Button button;
    private String busNumber;
    private String contactNumber;

    private JSONObject userProfile;
    public DriverBusDetails() {
        // Required empty public constructor
    }


    public static DriverBusDetails newInstance(JSONObject userProfile) {
        DriverBusDetails fragment = new DriverBusDetails();
        fragment.setUserProfile(userProfile);
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


        View rootView = inflater.inflate(R.layout.fragment_driver_bus_details, container, false);

        driverBusNumber = (EditText) rootView.findViewById(R.id.driver_bus_number);
        driverContactNumber = (EditText)rootView.findViewById(R.id.driver_contact_number);


        button  = (Button)rootView.findViewById(R.id.bus_driver);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    busNumber = driverBusNumber.getText().toString();
                    contactNumber = driverContactNumber.getText().toString();

                    if(TextUtils.isEmpty(busNumber)){
                        driverBusNumber.setError(getString(R.string.empty_not_valid));
                        driverBusNumber.requestFocus();
                        return;

                    }
                    if(TextUtils.isEmpty(contactNumber) || contactNumber.length() != 10){
                        driverContactNumber.setError(getString(R.string.empty_not_valid));
                        driverContactNumber.requestFocus();
                        return;
                    }



                    getUserProfile().put(getString(R.string.driverbusnumber),busNumber);
                    getUserProfile().put(getString(R.string.drivercontactnumber),getString(R.string.phonecountry_code)+contactNumber.trim());
                    mListener.onFragmentInteraction(getUserProfile(),R.string.invite_parents_with_location);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        return rootView;
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

    public JSONObject getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(JSONObject userProfile) {
        this.userProfile = userProfile;
    }
}
