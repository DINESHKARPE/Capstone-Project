package com.udacity.turnbyturn.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.udacity.turnbyturn.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TechSutra on 11/8/16.
 */

public class DriverStopAdapter extends ArrayAdapter<JSONObject> {


    public DriverStopAdapter(Context context, ArrayList<JSONObject> stops) {
        super(context, 0, stops);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position

        JSONObject stopObject = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.driverstoplist, parent, false);
        }
        // Lookup view for data population
        TextView stopLandmark = (TextView) convertView.findViewById(R.id.stop_landmark);
        TextView stopAddress = (TextView) convertView.findViewById(R.id.stop_address);
        // Populate the data into the template view using the data object
        try {
            stopLandmark.setText(stopObject.getString("landmark"));
            stopAddress.setText(stopObject.getString("address"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return the completed view to render on screen
        return convertView;
    }


}
