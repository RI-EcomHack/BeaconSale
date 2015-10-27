package de.commercetools.android_example;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public final class GlobalRequestQueue {
    private static GlobalRequestQueue instance;
    private static Context context;

    private RequestQueue requestQueue;

    private GlobalRequestQueue(final Context context) {
        GlobalRequestQueue.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized GlobalRequestQueue getInstance(final Context context) {
        if (instance == null) {
            instance = new GlobalRequestQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
