package com.udacity.turnbyturn.ui.profile;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import com.udacity.turnbyturn.R;
import com.udacity.turnbyturn.TurnByTurn;
import com.udacity.turnbyturn.adapter.DriverStopAdapter;
import com.udacity.turnbyturn.data.TurnByTurnContract;
import com.udacity.turnbyturn.ui.OnFragmentInteractionListener;
import com.udacity.turnbyturn.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriverStopList} interface
 * to handle interaction events.
 * Use the {@link DriverStopList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverStopList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View stopView;

    private DriverStopAdapter driverStopAdapter;

    private ArrayList<JSONObject> stopJsonObject;



    public DriverStopList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param
     * @param
     * @return A new instance of fragment DriverStopList.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverStopList newInstance() {
        DriverStopList fragment = new DriverStopList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       stopView =  inflater.inflate(R.layout.fragment_driver_stop_list, container, false);

        stopJsonObject = new ArrayList<>();
        driverStopAdapter = new DriverStopAdapter(getContext(),stopJsonObject);

        setListAdapter(driverStopAdapter);



        return stopView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri stopUri = TurnByTurnContract.StopEntry.CONTENT_URI;

        String sortOrder = TurnByTurnContract.StopEntry.SERVERID + " ASC";

        return new CursorLoader(getContext(),
                stopUri,
                Constants.FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        driverStopAdapter.clear();

        if (data != null && data.moveToFirst()) {
            ArrayList<JSONObject > jsonObjects = new ArrayList<>();
            for (int i = 0; i < data.getCount(); i++) {
                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put("landmark",data.getString(data.getColumnIndex(TurnByTurnContract.StopEntry.LANDMARK)));
                    jsonObject.put("address",data.getString(data.getColumnIndex(TurnByTurnContract.StopEntry.ADDRESS)));
                    jsonObjects.add(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                data.moveToNext();
            }

            driverStopAdapter.addAll(jsonObjects);
        }else {

        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



    public ArrayList<JSONObject> getStopJsonObject() {
        return stopJsonObject;
    }

    public void setStopJsonObject(ArrayList<JSONObject> stopJsonObject) {
        this.stopJsonObject = stopJsonObject;
    }

    @Override
    public void onResume() {
        getLoaderManager().initLoader(0, null, this);
        super.onResume();

    }
}
