package com.udacity.turnbyturn.services;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.rest.TurnByTurnClient;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by TechSutra on 11/7/16.
 */

public class PULLParentsStop extends AsyncTask<HashSet, Object, JSONObject> {



    public Context context;

    public PULLParentsStop(Context appcontext){
        context = appcontext;
    }

    @Override
    protected JSONObject doInBackground(HashSet... params) {


        Iterator iterator = params[0].iterator();

        while (iterator.hasNext()){
            String stopValue = iterator.next().toString();
            Call<JsonElement> siginResponse = TurnByTurnClient.get().fetchParent(stopValue);

            siginResponse.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonObject jsonElement = response.body().getAsJsonObject();
                    String stopId = jsonElement.get("stopid").toString().replaceAll("^\"|\"$", "");
                    ContentValues[] parentStops = new ContentValues[jsonElement.getAsJsonObject().getAsJsonArray("parents").size()];
                    for (int i = 0; i < jsonElement.getAsJsonObject().getAsJsonArray("parents").size(); i++) {

                        parentStops[i] = new ContentValues();

                        parentStops[i].put(TurnByTurnContract.ParentStopEntry.STOP_ID,stopId);
                        parentStops[i].put(TurnByTurnContract.ParentStopEntry.PARENT_ID,jsonElement.getAsJsonObject().getAsJsonArray("parents").get(i).getAsJsonObject().get("PARENTID").getAsString().replaceAll("^\"|\"$", ""));

                    }
                    context.getContentResolver().bulkInsert(TurnByTurnContract.ParentStopEntry.CONTENT_URI,parentStops);
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });
        }



        return new JSONObject();
    }


    @Override
    protected void onPostExecute(JSONObject aVoid) {

        super.onPostExecute(aVoid);

    }
}
