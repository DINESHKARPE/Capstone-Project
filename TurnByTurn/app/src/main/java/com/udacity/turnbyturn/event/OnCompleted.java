package com.udacity.turnbyturn.event;

import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by TechSutra on 11/10/16.
 */

public interface OnCompleted {


    void onTaskCompletes(List<JsonElement> jsonElementList);
}
