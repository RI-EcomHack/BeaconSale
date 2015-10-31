package de.commercetools.android_example.adapters;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class BeaconFinderService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BeaconFinderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

       new Handler().postDelayed(new Runnable() {
               public void run() {


               }
           }, 10000);
    }
}