package com.udacity.turnbyturn.services;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.rest.TurnByTurnClient;
import com.udacity.turnbyturn.util.ProfileType;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by TechSutra on 11/12/16.
 */

public class SyncParentProfile extends AsyncTask<JSONObject,Void,JSONObject> {

    private Context context;
    private View currentView;

    public SyncParentProfile(Context appContext, View rootView){
        context = appContext;
        currentView = rootView;
    }

    @Override
    protected JSONObject doInBackground(JSONObject... params) {


        JSONObject jsonObject = params[0];
        JSONObject userProfile = new JSONObject();
        try {
            userProfile.put("name",jsonObject.getString("name"));
            userProfile.put("photourl",jsonObject.getString("photourl"));
            userProfile.put("email",jsonObject.getString("email"));
            userProfile.put("gid",jsonObject.getString("gid"));
            userProfile.put("imei",jsonObject.getString("imei"));
            userProfile.put("serverauthcode",jsonObject.getString("serverauthcode"));
            userProfile.put("userid",new JSONObject( jsonObject.getString("INVRESPONSE")).getJSONArray("data").getJSONObject(0).get("id"));
            userProfile.put("regId",jsonObject.getString("regId"));

            Call<JsonElement> profileUpdate = TurnByTurnClient.get().updateparent(userProfile);
            profileUpdate.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Snackbar snackbar = Snackbar
                            .make(currentView, "Profile Sync Success", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });


            Call<JsonElement> parentDriver = TurnByTurnClient.get().fetchUser( new JSONObject( jsonObject.getString("INVRESPONSE")).getJSONArray("data").getJSONObject(0).get("driverid").toString());
            parentDriver.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonElement jsonElement = response.body();


                    ContentValues driverValue = new ContentValues();
                    driverValue.put(TurnByTurnContract.UserAccountEntry.NAME, ((JsonArray) jsonElement).get(0).getAsJsonObject().get("name").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.PHOTO_URL, ((JsonArray) jsonElement).get(0).getAsJsonObject().get("photourl").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.EMAIL, ((JsonArray) jsonElement).get(0).getAsJsonObject().get("email").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.G_ID, ((JsonArray) jsonElement).get(0).getAsJsonObject().get("gid").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.ID_TOKEN, ((JsonArray) jsonElement).get(0).getAsJsonObject().get("idtoken").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.CONTACT_NUMBER,((JsonArray) jsonElement).get(0).getAsJsonObject().get("contactnumber").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.BUS_NUMBER, ((JsonArray) jsonElement).get(0).getAsJsonObject().get("busnumber").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.DRIVER_ID, "0");
                    driverValue.put(TurnByTurnContract.UserAccountEntry.IMEI,((JsonArray) jsonElement).get(0).getAsJsonObject().get("imei").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.SERVER_AUTH_CODE,((JsonArray) jsonElement).get(0).getAsJsonObject().get("serverauthcode").toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.USER_TYPE, ProfileType.DRIVER.toString().replaceAll("^\"|\"$", ""));
                    driverValue.put(TurnByTurnContract.UserAccountEntry.SERVER_ID,((JsonArray) jsonElement).get(0).getAsJsonObject().get("id").toString().replaceAll("^\"|\"$", ""));


                    Uri insertedUri = context.getContentResolver().insert(TurnByTurnContract.UserAccountEntry.CONTENT_URI,
                            driverValue);

                    long locationId = ContentUris.parseId(insertedUri);


                    Snackbar snackbar = Snackbar
                            .make(currentView, "Driver Sync Success", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });

            Call<JsonElement> parentStop = TurnByTurnClient.get().fetchStop( new JSONObject( jsonObject.getString("INVRESPONSE")).getJSONArray("data").getJSONObject(0).get("STOPID").toString());
            parentStop.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonElement jsonElement = response.body();

                    ContentValues stopValues = new ContentValues();
                    stopValues.put(TurnByTurnContract.StopEntry.LATITUDE,((JsonArray) jsonElement).get(0).getAsJsonObject().get("latitude").toString().replaceAll("^\"|\"$", ""));
                    stopValues.put(TurnByTurnContract.StopEntry.LONGITUDE,((JsonArray) jsonElement).get(0).getAsJsonObject().get("longitude").toString().replaceAll("^\"|\"$", ""));
                    stopValues.put(TurnByTurnContract.StopEntry.LANDMARK,((JsonArray) jsonElement).get(0).getAsJsonObject().get("landmark").toString().replaceAll("^\"|\"$", ""));
                    stopValues.put(TurnByTurnContract.StopEntry.ADDRESS,((JsonArray) jsonElement).get(0).getAsJsonObject().get("address").toString().replaceAll("^\"|\"$", ""));
                    stopValues.put(TurnByTurnContract.StopEntry.SERVERID,((JsonArray) jsonElement).get(0).getAsJsonObject().get("id").toString().replaceAll("^\"|\"$", ""));

                    Uri insertedUri = context.getContentResolver().insert(TurnByTurnContract.StopEntry.CONTENT_URI,
                            stopValues);

                    long locationId = ContentUris.parseId(insertedUri);

                    Snackbar snackbar = Snackbar
                            .make(currentView, "Your Stop Sync Success", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
    }
}
