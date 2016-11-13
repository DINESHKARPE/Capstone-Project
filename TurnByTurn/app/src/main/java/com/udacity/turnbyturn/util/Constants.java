package com.udacity.turnbyturn.util;

import com.google.android.gms.maps.GoogleMap;
import com.udacity.turnbyturn.data.TurnByTurnContract;

/**
 * Created by TechSutra on 10/9/16.
 */

public class Constants {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME ="com.udacity.turnbyturn";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String TRIP_DATA_SENDER = PACKAGE_NAME + ".TRIP_DATA_SENDER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final String[] FORECAST_COLUMNS = {
            TurnByTurnContract.StopEntry.TABLE_NAME + "." + TurnByTurnContract.StopEntry._ID,
            TurnByTurnContract.StopEntry.LATITUDE,
            TurnByTurnContract.StopEntry.LONGITUDE,
            TurnByTurnContract.StopEntry.LANDMARK,
            TurnByTurnContract.StopEntry.ADDRESS,
            TurnByTurnContract.StopEntry.SERVERID
    };

    public static final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};


    public static int MAP_TYPE_INDEX = 1;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public interface ACTION {
        public static String MAIN_ACTION = "com.truiton.foregroundservice.action.main";
        public static String STOP_ACTION = "com.truiton.foregroundservice.action.stop";
        public static String STARTFOREGROUND_ACTION = "com.udacity.turnbyturn.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.udacity.turnbyturn.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
