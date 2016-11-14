package com.udacity.turnbyturn;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.udacity.turnbyturn.event.ActivityResultBus;
import com.udacity.turnbyturn.event.ActivityResultEvent;
import com.udacity.turnbyturn.rest.TurnByTurnClient;
import com.udacity.turnbyturn.ui.ContactListFragment;
import com.udacity.turnbyturn.ui.DriverBusDetails;
import com.udacity.turnbyturn.ui.DriverStopSeletion;
import com.udacity.turnbyturn.ui.OnFragmentInteractionListener;
import com.udacity.turnbyturn.ui.ParentStopLocation;
import com.udacity.turnbyturn.ui.ProfileSelection;
import com.udacity.turnbyturn.ui.SignIn;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * 
 */
public class TurnByTurn extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,OnFragmentInteractionListener {


    private static final String TAG = TurnByTurn.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions googleSignInOptions;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private SignIn signIn;
    private JSONObject userProfile;
    private View view;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn_by_turn);

        googleSignInOptions = ((MyApplication) getApplication()).getGoogleSignInOptions();
        mGoogleApiClient = ((MyApplication) getApplication()).getGoogleApiClient(TurnByTurn.this);

        view = findViewById(android.R.id.content);
        userProfile = new JSONObject();




        SignIn.newInstance(googleSignInOptions,mGoogleApiClient);


        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction=fragmentManager.beginTransaction();
        SignIn signIn = new SignIn();
        signIn.setGoogleSignInOptions(googleSignInOptions);
        signIn.setmGoogleApiClient(mGoogleApiClient);
        fragmentTransaction.add(R.id.fragment_container,signIn);
        fragmentTransaction.commit();
        if(Build.VERSION.SDK_INT > 23){

            String[] perms = {Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE};
            if (EasyPermissions.hasPermissions(getApplicationContext(), perms)) {


            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.all_permission_are_required),
                        PERMISSION_REQUEST_CODE, perms);
            }
        }





    }

    private void collectRequiresPermission() {
            String[] perms = {Manifest.permission.GET_ACCOUNTS};
            if (EasyPermissions.hasPermissions(this, perms)) {


            } else {
                EasyPermissions.requestPermissions(this, getString(R.string.all_permission_are_required),
                        PERMISSION_REQUEST_CODE, perms);
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_turn_by_turn, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    @Override
    protected void onStart() {


        super.onStart();


    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
    }



    @Override
    protected void onStop() {

        if(mGoogleApiClient !=null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultBus.getBus().post(new ActivityResultEvent(requestCode, resultCode, data));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        for(String permission: perms){

            switch (permission){

                case "android.permission.RECORD_AUDIO":
                    break;
                case "android.permission.GET_ACCOUNTS":

                    break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onFragmentInteraction(JSONObject fragmentData,int stringID) {

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Iterator<String> stringIterator = fragmentData.keys();
        for(;stringIterator.hasNext();){
            String key = stringIterator.next();
            try {
                userProfile.put(key,fragmentData.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        switch (stringID){

            case R.string.invite_contact_list:

                ContactListFragment contactListFragment = ContactListFragment.newInstance(userProfile);
                fragmentTransaction.replace(R.id.fragment_container,contactListFragment);
                break;
            //"PROFILE_SELECTION"
            case R.string.profile_selection:



                sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();

                if(sharedPreferences.getString(getString(R.string.user_sign_in),"").equals(getString(R.string.completed))){

                    Intent start_profile = new Intent(this, UserProfile.class);
                    start_profile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(start_profile);
                    overridePendingTransition(android.support.v7.appcompat.R.anim.abc_fade_in,android.support.v7.appcompat.R.anim.abc_fade_out);

                }else {

                    ProfileSelection profileSelection = ProfileSelection.newInstance(userProfile);

                    fragmentTransaction.replace(R.id.fragment_container, profileSelection);
                }
                break;

            case R.string.parent_stop:

                ParentStopLocation parentStopLocation = ParentStopLocation.newInstance(userProfile,googleSignInOptions,mGoogleApiClient);
                fragmentTransaction.replace(R.id.fragment_container,parentStopLocation);
                break;

            case R.string.driver_bus_details:

                DriverBusDetails driverBusDetails = DriverBusDetails.newInstance(userProfile);
                fragmentTransaction.replace(R.id.fragment_container,driverBusDetails);
                break;
            case R.string.invite_parents_with_location:

                DriverStopSeletion driverStopSeletion = DriverStopSeletion.newInstance(userProfile,googleSignInOptions,mGoogleApiClient);
                fragmentTransaction.replace(R.id.fragment_container,driverStopSeletion);
                break;
            case R.string.start_profile:



                Intent start_profile = new Intent(this, UserProfile.class);
                start_profile.putExtra(getString(R.string.response),userProfile.toString());
                start_profile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(start_profile);
                overridePendingTransition(android.support.v7.appcompat.R.anim.abc_fade_in,android.support.v7.appcompat.R.anim.abc_fade_out);
                break;

            default:


        }


        fragmentTransaction.commit();

    }



    @Override
    protected void onResume() {
        super.onPostResume();


    }
}
