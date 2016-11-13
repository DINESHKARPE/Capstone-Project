package com.udacity.turnbyturn.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.udacity.turnbyturn.TurnByTurn;

/**
 * Created by TechSutra on 10/31/16.
 */

public class TurnByTurnDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "turnbyturn.db";

    public TurnByTurnDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_USER_ACCOUNT_TABLE = "CREATE TABLE " + TurnByTurnContract.UserAccountEntry.TABLE_NAME + " (" +
                TurnByTurnContract.UserAccountEntry._ID + " INTEGER PRIMARY KEY," +
                TurnByTurnContract.UserAccountEntry.NAME + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.PHOTO_URL + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.EMAIL + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.G_ID + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.ID_TOKEN + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.CONTACT_NUMBER + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.BUS_NUMBER + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.DRIVER_ID + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.IMEI  + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.SERVER_AUTH_CODE + " TEXT NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.USER_TYPE + " INTEGER NOT NULL, " +
                TurnByTurnContract.UserAccountEntry.SERVER_ID + " TEXT NOT NULL " +
                " );";


        final String SQL_CREATE_STOP_TABLE  = "CREATE TABLE "+ TurnByTurnContract.StopEntry.TABLE_NAME + " ("+
                TurnByTurnContract.StopEntry._ID + " INTEGER PRIMARY KEY," +
                TurnByTurnContract.StopEntry.LATITUDE + " TEXT NOT NULL," +
                TurnByTurnContract.StopEntry.LONGITUDE + " TEXT NOT NULL," +
                TurnByTurnContract.StopEntry.LANDMARK + " TEXT NOT NULL," +
                TurnByTurnContract.StopEntry.ADDRESS + " TEXT NOT NULL," +
                TurnByTurnContract.StopEntry.SERVERID + " TEXT NOT NULL" +
                " );";


        final String SQL_CREATE_PARENT_STOP = "CREATE TABLE "+ TurnByTurnContract.ParentStopEntry.TABLE_NAME + " ("+
                TurnByTurnContract.ParentStopEntry._ID + " INTEGER PRIMARY KEY," +
                TurnByTurnContract.ParentStopEntry.PARENT_ID + " TEXT NOT NULL," +
                TurnByTurnContract.ParentStopEntry.STOP_ID   + " TEXT NOT NULL"  +
                " );";


        sqLiteDatabase.execSQL(SQL_CREATE_USER_ACCOUNT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_STOP_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PARENT_STOP);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TurnByTurnContract.UserAccountEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TurnByTurnContract.StopEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TurnByTurnContract.ParentStopEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
