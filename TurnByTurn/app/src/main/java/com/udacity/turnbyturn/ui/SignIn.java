package com.udacity.turnbyturn.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.iid.FirebaseInstanceId;
import com.udacity.turnbyturn.MyApplication;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.util.Config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignIn#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignIn extends Fragment implements View.OnClickListener{

    private String TAG = SignIn.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions googleSignInOptions;

    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "mGoogleApiClient";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private OnFragmentInteractionListener mListener;




    public SignIn() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param
     * @param
     * @param googleSignInOptions
     * @return A new instance of fragment SignIn.
     */
    // TODO: Rename and change types and number of parameters
    public static SignIn newInstance(GoogleSignInOptions googleSignInOptions, GoogleApiClient googleApiClient) {
        SignIn fragment = new SignIn();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setGoogleSignInOptions(googleSignInOptions);
        fragment.setmGoogleApiClient(googleApiClient);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sign_in, container,false);

        SignInButton signInButton = (SignInButton) rootView.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(getGoogleSignInOptions().getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }


    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d(TAG,"Person loaded");
            Log.d(TAG,("DisplayName "+acct.getDisplayName()));
            Log.d(TAG,"Url "+acct.getPhotoUrl());
            Log.d(TAG,FirebaseInstanceId.getInstance().getToken());
            JSONObject userAccount = new JSONObject();
            TelephonyManager mngr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

            try {

                SharedPreferences pref = getContext().getSharedPreferences(Config.SHARED_PREF, 0);

                userAccount.put(getString(R.string.username),acct.getDisplayName());
                userAccount.put(getString(R.string.userprofilepic),acct.getPhotoUrl());
                userAccount.put(getString(R.string.useremail),acct.getEmail());
                userAccount.put(getString(R.string.usergid),acct.getId());
                userAccount.put(getString(R.string.usergidtoken),acct.getIdToken());
                userAccount.put(getString(R.string.imei),mngr.getDeviceId());
                userAccount.put(getString(R.string.serverauthcode),acct.getServerAuthCode());
                userAccount.put(getString(R.string.regId),pref.getString(getString(R.string.regId), null));
                Log.d(TAG,userAccount.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListener.onFragmentInteraction(userAccount, R.string.profile_selection);


        } else {
            Snackbar snackbar = Snackbar
                    .make(getView(), getString(R.string.warning), Snackbar.LENGTH_SHORT);
            snackbar.show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if(getmGoogleApiClient() != null){
            getmGoogleApiClient().connect();
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(getmGoogleApiClient());

            if (opr.isDone()) {

                GoogleSignInResult result = opr.get();

                handleSignInResult(result);

            } else {

                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {

                        handleSignInResult(googleSignInResult);

                    }
                });
            }
        }
    }



    public GoogleSignInOptions getGoogleSignInOptions() {
        return googleSignInOptions;
    }

    public void setGoogleSignInOptions(GoogleSignInOptions googleSignInOptions) {
        this.googleSignInOptions = googleSignInOptions;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }



    @Override
    public void onPause() {
        super.onPause();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
