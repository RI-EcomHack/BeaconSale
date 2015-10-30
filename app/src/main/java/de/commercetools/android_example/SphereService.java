package de.commercetools.android_example;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class SphereService extends Service {
    private GlobalRequestQueue globalRequestQueue;

    private AuthenticationService authService;
    private String accessToken;

    private String sphereApiHost;
    private String projectKey;

    private boolean bound = false;

    public SphereService() {
    }

    public void executeRequest(final SphereRequest request, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        executeRequest(request.method, request.getUrl(), request.body, listener, errorListener);
    }

    public void executeJacksonRequest(final SphereRequest request, final Response.Listener<JsonNode> listener, final Response.ErrorListener errorListener) {
        final Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = mapper.readValue(response.toString(), JsonNode.class);
                    listener.onResponse(jsonNode);
                } catch (IOException e) {
                    throw new AssertionError("");
                }
            }
        };
        executeRequest(request, jsonObjectListener, errorListener);
    }

    public void executeRequest(final int method, final String url, final String requestBody, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
        globalRequestQueue.addToRequestQueue(
                new AuthorizedJsonRequest(method, sphereApiHost + projectKey + url, requestBody, listener, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == HTTP_UNAUTHORIZED) {
                            requestAccessToken(
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            setAccessToken(response);
                                            executeRequest(method, url, requestBody, listener, errorListener);
                                        }
                                    }
                            );
                        } else {
                            errorListener.onErrorResponse(error);
                        }
                    }
                }));
    }

    private void requestAccessToken(final Response.Listener<JSONObject> listener) {
        if (bound) {
            authService.getAccessToken(listener);
        }
    }

    private void setAccessToken(final JSONObject response) {
        try {
            accessToken = response.get("access_token").toString();
        } catch (JSONException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * An authorized request to access the sphere api
     */
    private class AuthorizedJsonRequest extends JsonObjectRequest {

        public AuthorizedJsonRequest(final int method, final String url, final String requestBody, final Response.Listener<JSONObject> listener, final Response.ErrorListener errorListener) {
            super(method, url, requestBody, listener, errorListener);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
        }
    };
}
