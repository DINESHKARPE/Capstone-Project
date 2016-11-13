package com.udacity.turnbyturn.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.rest.TurnByTurnClient;
import com.udacity.turnbyturn.services.SyncParentProfile;
import com.udacity.turnbyturn.util.Config;
import com.udacity.turnbyturn.util.ProfileType;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileSelection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileSelection extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RadioGroup profileSelectionRadioButtonGroup;
    private RadioButton parentRadioButton;
    private RadioButton driverRadioButton;

    private OnFragmentInteractionListener mListener;
    private Button button;

    private View profileSelectionView;
    private String profileType;
    private EditText parentContactNumber;
    private  String parentContact;

    private JSONObject userProfile;
    public ProfileSelection() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment ProfileSelection.
     * @param userProfile
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileSelection newInstance(JSONObject userProfile) {
        ProfileSelection fragment = new ProfileSelection();
        fragment.setUserProfile(userProfile);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        profileSelectionView = inflater.inflate(R.layout.fragment_profile_selection, container,false);

        parentRadioButton = (RadioButton)profileSelectionView.findViewById(R.id.parent_profile_selection);
        driverRadioButton = (RadioButton)profileSelectionView.findViewById(R.id.driver_profile_selection);

        profileSelectionRadioButtonGroup = (RadioGroup) profileSelectionView.findViewById(R.id.profile_selection_button_group);


        button = (Button) profileSelectionView.findViewById(R.id.profile_selection_button);
        parentContactNumber = (EditText)profileSelectionView.findViewById(R.id.parent_contact_number);
        parentContact = parentContactNumber.getText().toString();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 final JSONObject jsonObject = getUserProfile();
                try {
                    parentContact = parentContactNumber.getText().toString();
                  if(TextUtils.isEmpty(profileType)){
                      parentRadioButton.setError(getString(R.string.profile_selection_error_parent));
                      driverRadioButton.setError(getString(R.string.profile_selection_error_driver));
                      parentRadioButton.requestFocus();
                      return;
                  }

                 if(ProfileType.DRIVER.toString().equals(profileType)){

                     jsonObject.put("profiletype",ProfileType.DRIVER.toString());
                     mListener.onFragmentInteraction(jsonObject,getString(R.string.driver_bus_details));
                 }else{
                     jsonObject.put("profiletype",ProfileType.PARENT.toString());


                     if (TextUtils.isEmpty(parentContact) && View.VISIBLE == parentContactNumber.getVisibility()) {
                         parentContactNumber.setError(getString(R.string.empty_not_valid));
                         parentContactNumber.requestFocus();
                         return;
                     }else {

                         final Call<JsonElement> resopnseData = TurnByTurnClient.get().checkinvItationPending("+91" + parentContact.trim().replaceAll(" ",""));
                         resopnseData.enqueue(new Callback<JsonElement>() {
                             @Override
                             public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                                 try {
                                     JsonElement jsonElement = response.body();
                                     jsonObject.put("parentcontactnumber","+91"+ parentContact.trim().replaceAll(" ",""));
                                     SharedPreferences pref = getContext().getSharedPreferences(Config.SHARED_PREF, 0);
                                     jsonObject.put("regId", pref.getString("regId", null));

                                     if(((JsonObject) jsonElement).get("status").toString().replaceAll("\"","").equals("FAIL")){
                                       mListener.onFragmentInteraction(jsonObject,"PARENT_STOP");
                                     }else {

                                         jsonObject.put("INVRESPONSE",jsonElement.getAsJsonObject());

                                         ContentValues accountValues = new ContentValues();
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.NAME, jsonObject.getString("name"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.PHOTO_URL, jsonObject.getString("photourl"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.EMAIL, jsonObject.getString("email"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.G_ID, jsonObject.getString("gid"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.ID_TOKEN, "");
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.CONTACT_NUMBER,jsonObject.getString("parentcontactnumber"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.BUS_NUMBER, "0");
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.DRIVER_ID, String.valueOf(((JsonObject) jsonElement).getAsJsonArray("data").get(0).getAsJsonObject().get("driverid")));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.IMEI,jsonObject.getString("imei"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.SERVER_AUTH_CODE,jsonObject.getString("serverauthcode"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.USER_TYPE,jsonObject.getString("profiletype"));
                                         accountValues.put(TurnByTurnContract.UserAccountEntry.SERVER_ID, String.valueOf(((JsonObject) jsonElement).getAsJsonArray("data").get(0).getAsJsonObject().get("id")));

                                         Uri insertedUri = getContext().getContentResolver().insert(TurnByTurnContract.UserAccountEntry.CONTENT_URI,
                                                 accountValues);

                                         long locationId = ContentUris.parseId(insertedUri);

                                         new SyncParentProfile(getContext(),getActivity().getWindow().getDecorView().getRootView()).execute(jsonObject);

                                         mListener.onFragmentInteraction(jsonObject,"START_PROFILE");
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




                 }
                 } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });




        profileSelectionRadioButtonGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                parentRadioButton.setError(null);
                driverRadioButton.setError(null);
                if(parentRadioButton.isChecked())
                {
                    parentContactNumber.setVisibility(View.VISIBLE);
                    profileType = ProfileType.PARENT.toString();
                }
                else {
                    profileType = ProfileType.DRIVER.toString();
                    parentContactNumber.setVisibility(View.INVISIBLE);
                }


            }
        });
        return profileSelectionView;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
