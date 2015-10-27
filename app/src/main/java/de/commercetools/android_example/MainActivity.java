package de.commercetools.android_example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private GlobalRequestQueue requestQueue;
    private Authenticator authenticator;
    private SphereClient sphereClient;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tokenTextView);

        requestQueue = GlobalRequestQueue.getInstance(getApplicationContext());

        final String authUrl = getString(R.string.authUrl);
        final String projectKey = getString(R.string.project);
        final String clientId = getString(R.string.clientId);
        final String clientSecret = getString(R.string.clientSecret);

        try {
            authenticator = new Authenticator(authUrl, projectKey, clientId, clientSecret, requestQueue);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        authenticator.obtainAccessToken(
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    createSphereClientAndPerformExampleRequest();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    textView.setText(getString(R.string.credentialsError));
                }
            });
    }

    private void createSphereClientAndPerformExampleRequest() {
        final String apiUrl = getString(R.string.apiUrl);

        sphereClient = new SphereClient(apiUrl, authenticator, requestQueue);
        sphereClient.get("/products",
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    textView.setText(response.toString());
                }
            }, null);
    }
}