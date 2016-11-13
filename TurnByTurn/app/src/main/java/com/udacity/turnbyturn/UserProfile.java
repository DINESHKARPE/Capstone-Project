package com.udacity.turnbyturn;


import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonElement;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.event.OnCompleted;
import com.udacity.turnbyturn.ui.OnFragmentInteractionListener;
import com.udacity.turnbyturn.ui.profile.DriverStopList;
import com.udacity.turnbyturn.ui.profile.Trip;
import com.udacity.turnbyturn.util.Config;
import com.udacity.turnbyturn.util.NotificationUtils;
import com.udacity.turnbyturn.util.ProfileType;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import org.json.JSONObject;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;

public class UserProfile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>,OnFragmentInteractionListener,OnCompleted{


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private GoogleApiClient mGoogleApiClient;

    private View headerLayout;

    private static final String[] FORECAST_COLUMNS = {
            TurnByTurnContract.UserAccountEntry.TABLE_NAME + "." + TurnByTurnContract.UserAccountEntry._ID,
            TurnByTurnContract.UserAccountEntry.NAME,
            TurnByTurnContract.UserAccountEntry.PHOTO_URL,
            TurnByTurnContract.UserAccountEntry.EMAIL,
            TurnByTurnContract.UserAccountEntry.G_ID,
            TurnByTurnContract.UserAccountEntry.ID_TOKEN,
            TurnByTurnContract.UserAccountEntry.CONTACT_NUMBER,
            TurnByTurnContract.UserAccountEntry.BUS_NUMBER,
            TurnByTurnContract.UserAccountEntry.DRIVER_ID,
            TurnByTurnContract.UserAccountEntry.IMEI,
            TurnByTurnContract.UserAccountEntry.SERVER_AUTH_CODE,
            TurnByTurnContract.UserAccountEntry.USER_TYPE,
            TurnByTurnContract.UserAccountEntry.SERVER_ID
    };

    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        mGoogleApiClient = ((MyApplication) getApplication()).getGoogleApiClient(UserProfile.this);

        final NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        headerLayout = mNavigationView.getHeaderView(0);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

        tx.replace(R.id.parent_fragment_container, Trip.newInstance(mGoogleApiClient));
        tx.commit();

        sharedPreferences = getSharedPreferences("", Context.MODE_PRIVATE);
    }






    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_profile, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        int id = item.getItemId();

        if (id == R.id.trip) {
            Trip trip = Trip.newInstance(mGoogleApiClient);
            fragmentTransaction.replace(R.id.parent_fragment_container,trip);
        } else  if (id == R.id.driver_stop) {
            DriverStopList driverStopList = DriverStopList.newInstance();
            fragmentTransaction.replace(R.id.parent_fragment_container,driverStopList);
        } else if(id == R.id.logout){
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {

                            chnageActivity();
                        }
                    });
        }
        else if(id == R.id.licence){
            final String name = "LicensesDialog";
            final String url = "http://psdev.de";
            final String copyright = "Copyright 2013 Philip Schiffer <admin@psdev.de>";
            final License license = new ApacheSoftwareLicense20();
            final Notice notice = new Notice(name, url, copyright, license);
            new LicensesDialog.Builder(this)
                    .setNotices(notice)
                    .build()
                    .showAppCompat();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        fragmentTransaction.commit();
        return true;
    }



    private void chnageActivity() {

        Intent start_profile = new Intent(this, TurnByTurn.class);
        start_profile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(start_profile);
        overridePendingTransition(android.support.v7.appcompat.R.anim.abc_fade_in,android.support.v7.appcompat.R.anim.abc_fade_out);
    }

    @Override
    protected void onStart() {

        if(!sharedPreferences.getString(getString(R.string.user_sign_in),"").equals(getString(R.string.completed))){

            editor = sharedPreferences.edit();
            editor.putString(getString(R.string.user_sign_in),getString(R.string.completed));
            editor.commit();

        }

        super.onStart();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri accountUri = TurnByTurnContract.UserAccountEntry.CONTENT_URI;

        return new CursorLoader(this,
                accountUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            ImageView imageView = (ImageView) headerLayout.findViewById(R.id.imageView);

            TextView  user_name  = (TextView) headerLayout.findViewById(R.id.user_name);
            TextView  user_email = (TextView) headerLayout.findViewById(R.id.user_email);



            user_name.setText(data.getString(data.getColumnIndex(TurnByTurnContract.UserAccountEntry.NAME)));
            user_email.setText(data.getString(data.getColumnIndex(TurnByTurnContract.UserAccountEntry.EMAIL)));

            editor = sharedPreferences.edit();
            editor.putString(getString(R.string.profiletype),data.getString(data.getColumnIndex(TurnByTurnContract.UserAccountEntry.USER_TYPE)));
            editor.commit();

            if(data.getString(data.getColumnIndex(TurnByTurnContract.UserAccountEntry.USER_TYPE)).equals(ProfileType.DRIVER.toString())){



                setTitle(getString(R.string.driverTitle));
            }else {
                setTitle(getString(R.string.parentTitle));
            }
            Glide.with(getApplicationContext())
                    .load(data.getString(data.getColumnIndex(TurnByTurnContract.UserAccountEntry.PHOTO_URL)))
                    .centerCrop()
                    .override(200, 200)
                    .crossFade()
                    .fitCenter()
                    .into(imageView);



        }else{
            Toast.makeText(getApplicationContext(),R.string.data_not_found,Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onResume() {
        getSupportLoaderManager().initLoader(0, null, this);
        super.onResume();



        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    public void onFragmentInteraction(JSONObject fragmentData, String moveToscreen) {

    }

    @Override
    public void onTaskCompletes(List<JsonElement> jsonElementList) {

    }




    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }


}
