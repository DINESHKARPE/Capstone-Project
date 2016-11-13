package com.udacity.turnbyturn.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TechSutra on 10/31/16.
 */

public class TurnByTurnContract {

    public static final String CONTENT_AUTHORITY = "com.udacity.turnbyturn";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_USER_ACCOUNT = "account";
    public static final String PATH_STOP="stop";
    public static final String PATH_PARENT_STOP="parentstop";
    public static final String PATH_TRIP="trip";



    public static final class UserAccountEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER_ACCOUNT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER_ACCOUNT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER_ACCOUNT;


        public static final String TABLE_NAME = "account";


        public static final String NAME = "name";
        public static final String PHOTO_URL = "photourl";
        public static final String EMAIL = "email";
        public static final String G_ID = "gid";
        public static final String ID_TOKEN = "idtoken";
        public static final String CONTACT_NUMBER ="contactnumber";
        public static final String BUS_NUMBER="busnumber";
        public static final String DRIVER_ID="driverid";
        public static final String IMEI = "imei";
        public static final String SERVER_AUTH_CODE = "serverauthcode";
        public static final String USER_TYPE = "usertype";
        public static final String SERVER_ID = "serverid";


        public static Uri buildAccountUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }



    }

    public static final class StopEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOP;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOP;

        public static final String TABLE_NAME = "stop";

        public static final String LATITUDE  = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String LANDMARK  = "landmark";
        public static final String ADDRESS = "address";
        public static final String SERVERID = "serverid";

        public static Uri buildStopUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

    }

    public static final class ParentStopEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PARENT_STOP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARENT_STOP;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARENT_STOP;

        public static final String TABLE_NAME = "parentstop";

        public static final String PARENT_ID = "parentid";
        public static final String STOP_ID = "stopid";

        public static Uri buildParentStopUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
