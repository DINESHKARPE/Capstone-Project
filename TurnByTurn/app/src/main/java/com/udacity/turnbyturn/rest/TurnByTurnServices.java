package com.udacity.turnbyturn.rest;

import com.google.gson.JsonElement;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by TechSutra on 11/2/16.
 */

public interface TurnByTurnServices {

    @POST("sigin")
    Call<JsonElement> sigin(@Body JSONObject userJsonObject);


    @GET("stopwithParents/{stopid}")
    Call<JsonElement> fetchParent(@Path("stopid") String stopId);

    @GET("validatatedinvitation/{contactnumber}")
    Call<JsonElement> checkinvItationPending(@Path("contactnumber") String contactnumber);

    @PUT("updateparent")
    Call<JsonElement> updateparent(@Body JSONObject userJsonObject);


    @GET("fetchUser/{userid}")
    Call<JsonElement> fetchUser(@Path("userid") String userid);

    @GET("fetchStop/{stopid}")
    Call<JsonElement> fetchStop(@Path("stopid") String stopid);

    @POST("notification")
    Call<JsonElement> sendPush(@Body JSONObject userJsonObject);

}
