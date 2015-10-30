package de.commercetools.android_example;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView resultView;
    private SphereService sphereService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (TextView) findViewById(R.id.resultView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = new Intent(this, SphereService.class);
        bindService(intent, sphereServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(sphereServiceConnection);
            bound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection sphereServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            final SphereServiceBinder binder = (SphereServiceBinder) service;
            sphereService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
        }
    };

    public void executeExampleRequest(View view) {
        if(bound) {
            final SphereRequest request = SphereRequest.get("/products").limit(5);
            sphereService.executeRequest(request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            resultView.setText(response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            resultView.setText(error.getMessage());
                        }
                    });
        }
    }
}