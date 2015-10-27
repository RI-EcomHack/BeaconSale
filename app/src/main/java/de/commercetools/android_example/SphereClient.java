package de.commercetools.android_example;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SphereClient {

    private final String apiUrl;
    private final Authenticator authenticator;
    private final GlobalRequestQueue globalRequestQueue;

    public SphereClient(final String apiUrl,
                        final Authenticator authenticator,
                        final GlobalRequestQueue requestQueue) {
        this.apiUrl = apiUrl;
        this.authenticator = authenticator;
        this.globalRequestQueue = requestQueue;
    }

    public void get(final String url,
                    final Response.Listener<JSONObject> listener,
                    final Response.ErrorListener errorListener) {
        executeRequest(Request.Method.GET, url, listener, errorListener);
    }

    public void post(final String url,
                     final Response.Listener<JSONObject> listener,
                     final Response.ErrorListener errorListener) {
        executeRequest(Request.Method.POST, url, listener, errorListener);
    }

    public void executeRequest(final int method, final String url,
                               final Response.Listener<JSONObject> listener,
                               final Response.ErrorListener errorListener) {
        globalRequestQueue.addToRequestQueue(new AuthorizedJsonRequest(
                method,
                apiUrl + "/" + authenticator.getProjectKey() + url,
                listener,
                errorListener));
    }

    private class AuthorizedJsonRequest extends JsonObjectRequest {
        public AuthorizedJsonRequest(final int method, final String url,
                                     final Response.Listener<JSONObject> listener,
                                     final Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() {
            final Map<String, String>  params = new HashMap<>();
            params.put("Authorization", "Bearer " + authenticator.getAccessToken());
            return params;
        }
    }
}
