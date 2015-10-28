package de.commercetools.android_example;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SphereService extends Service {
    private GlobalRequestQueue globalRequestQueue;

    private AuthenticationService authService;
    private String accessToken;

    private String sphereApiHost;
    private String projectKey;

    private boolean bound = false;

    public SphereService() {
    }

    public void executeRequest(final int method, final String url, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        globalRequestQueue.addToRequestQueue(
                new AuthorizedJsonRequest(method, sphereApiHost + projectKey + url, listener, errorListener));
    }

    private void requestAccessToken() {
        if (bound) {
            authService.getAccessToken(
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                accessToken = response.get("access_token").toString();
                            } catch (JSONException e) {
                                throw new AssertionError(e);
                            }
                            // retry request
                        }
                    }
            );
        }

    }

    /**
     * An authorized request to access the sphere api
     */
    private class AuthorizedJsonRequest extends JsonObjectRequest {

        public AuthorizedJsonRequest(final int method, final String url, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() {
            final Map<String, String> params = new HashMap<>();
            params.put("Authorization", "Bearer " + accessToken);
            return params;
        }
    }

    /**
     * Lifecycle stuff
     */

    @Override
    public IBinder onBind(Intent intent) {
        bindAuthService();
        globalRequestQueue = GlobalRequestQueue.getInstance(this.getApplicationContext());
        sphereApiHost = getString(R.string.sphereApiHost);
        projectKey = getString(R.string.project);
        return new SphereServiceBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (bound) {
            unbindService(authServiceConnection);
            bound = false;
        }
        return false;
    }

    private void bindAuthService() {
        final Intent authServiceIntent = new Intent(this, AuthenticationService.class);
        bindService(authServiceIntent, authServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection authServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            final AuthenticationServiceBinder binder = (AuthenticationServiceBinder) service;
            authService = binder.getService();
            bound = true;
            requestAccessToken();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
        }
    };
}