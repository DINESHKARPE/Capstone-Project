package com.udacity.turnbyturn.event;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by TechSutra on 10/9/16.
 */

public class ActivityResultBus extends Bus {

    private static  Bus bus = new Bus();
    private static ActivityResultBus instance;

    public static ActivityResultBus getInstance() {
        if (instance == null)
            instance = new ActivityResultBus();
        return instance;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public void postQueue(final Object obj) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ActivityResultBus.getInstance().postQueue(obj);
            }
        });
    }

    public static Bus getBus() {
        return bus;
    }
}
