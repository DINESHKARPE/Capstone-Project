package com.udacity.turnbyturn.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by TechSutra on 11/2/16.
 */

public class TurnByTurnClient {


    private static TurnByTurnServices turnByTurnServices;

    private static final String API_URL = "http://128.199.218.81:9999/v1/";

    static {
        setupRestClient();
    }

    private TurnByTurnClient() {}

    public static TurnByTurnServices get() {
        return turnByTurnServices;
    }

    private static void setupRestClient() {


        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        OkHttpClient.Builder httpClient;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();


        turnByTurnServices = retrofit.create(TurnByTurnServices.class);

    }

}
