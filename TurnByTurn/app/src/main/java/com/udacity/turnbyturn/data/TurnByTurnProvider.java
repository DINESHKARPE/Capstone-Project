package com.udacity.turnbyturn.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by TechSutra on 10/31/16.
 */

public class TurnByTurnProvider extends ContentProvider {


    private static final UriMatcher sUriMatcher = buildUriMatcher();



    private TurnByTurnDbHelper turnByTurnDbHelper;


    static final int USER_ACCOUNT = 100;
    static final int STOP = 101;
    static final int PARENT_STOP = 102;

    @Override
    public boolean onCreate() {

        turnByTurnDbHelper = new TurnByTurnDbHelper(getContext());

        return true;

    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            /**
             * Retun User Account
             */
            case USER_ACCOUNT:{
                retCursor = turnByTurnDbHelper.getReadableDatabase().query(
                        TurnByTurnContract.UserAccountEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case STOP:{


                retCursor = turnByTurnDbHelper.getReadableDatabase().query(
                        TurnByTurnContract.StopEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {


        final SQLiteDatabase db = turnByTurnDbHelper.getWritableDatabase();

        Uri returnUri;


        switch (sUriMatcher.match(uri)) {
            case USER_ACCOUNT: {

                long _id = db.insert(TurnByTurnContract.UserAccountEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TurnByTurnContract.UserAccountEntry.buildAccountUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STOP: {
                long _id = db.insert(TurnByTurnContract.StopEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TurnByTurnContract.StopEntry.buildStopUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PARENT_STOP: {
                long _id = db.insert(TurnByTurnContract.ParentStopEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TurnByTurnContract.ParentStopEntry.buildParentStopUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;


    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = turnByTurnDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOP:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(TurnByTurnContract.StopEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }


    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TurnByTurnContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TurnByTurnContract.PATH_USER_ACCOUNT, USER_ACCOUNT);
        matcher.addURI(authority, TurnByTurnContract.PATH_PARENT_STOP, PARENT_STOP);
        matcher.addURI(authority, TurnByTurnContract.PATH_STOP, STOP);
        return matcher;
    }


}
