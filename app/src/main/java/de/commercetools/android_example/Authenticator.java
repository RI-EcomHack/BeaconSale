package de.commercetools.android_example;

import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Authenticator {

    private final String authUrl;
    private final String projectKey;
    private final String basicAuthToken;
    private final GlobalRequestQueue globalRequestQueue;

    private String accessToken;

    public Authenticator(final String authUrl,
                         final String projectKey,
                         final String clientId,
                         final String clientSecret,
                         final GlobalRequestQueue globalRequestQueue)
            throws UnsupportedEncodingException {
        this.authUrl = authUrl;
        this.projectKey = projectKey;
        this.basicAuthToken = createBasicAuthToken(clientId, clientSecret);
        this.globalRequestQueue = globalRequestQueue;
        System.out.println(basicAuthToken);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void obtainAccessToken(final Response.Listener<String> listener,
                                  final Response.ErrorListener errorListener) {
        globalRequestQueue.addToRequestQueue(getAccessTokenRequest(listener, errorListener));
    }

    private String createBasicAuthToken(final String clientId, final String clientSecret)
            throws UnsupportedEncodingException {
        final byte[] bytes = (clientId + ":" + clientSecret).getBytes("UTF-8");
        return android.util.Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private JsonObjectRequest getAccessTokenRequest(final Response.Listener<String> listener,
                                                    final Response.ErrorListener errorListener) {
        return new JsonObjectRequest(Request.Method.POST, authUrl + projectKey,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            accessToken = response.get("access_token").toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(listener != null) {
                            listener.onResponse(accessToken);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        if(errorListener != null) {
                            errorListener.onErrorResponse(error);
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String>  params = new HashMap<>();
                params.put("Authorization", "Basic " + basicAuthToken);
                return params;
            }
        };
    }
}
