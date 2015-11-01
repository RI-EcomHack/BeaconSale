package de.commercetools.android_example;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ProductListActivity extends ListActivity implements
        TextToSpeech.OnInitListener {

    private SphereService sphereService;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private TextToSpeech tts;
    private ProgressDialog progressDialog;
    private ArrayList<HashMap<String, String>> productList;
    private ArrayList<JsonNode> products;
    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private Region region;
    private static final int NOTIFICATION_ID = 123;


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final Intent intent = new Intent(this, CartActivity.class);
        final String productId = products.get(position).get("id").textValue();
        intent.putExtra("productId", productId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            //speakOut();
        }

        return true;
    }
    /**
     * Lifecycle stuff
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);


        tts = new TextToSpeech(this, this);

        // Configure BeaconManager.
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        for (Beacon beacon : beacons) {
                            Log.i("UUID", beacon.getProximityUUID().toString() + " " + Integer.toString(beacon.getMajor()) + " " + Integer.toString(beacon.getMinor()));

                            if (beacon.getProximityUUID().toString().equals("b9407f30-f5f8-466e-aff9-25556b57fe6d")
                                    && beacon.getMajor() == 52008
                                    && beacon.getMinor() == 23433) {
                                String id = beacon.getProximityUUID().toString() + beacon.getMajor() + beacon.getMinor();
                                //speakOut("You are close to your beacon" + beacon.getProximityUUID().toString());
                                try {
                                    new NotifyAPI().execute(
                                            new URL("http://5.196.27.161:8080/customer_in_range/3be9c767-11c0-4280-ad5d-a3135a138c6c/" + id)
                                    );
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                Log.i("Speak", "speak here");
               // speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut(String toSpeak) {

        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void startScanning() {

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
            }

        });
    }


    @Override protected void onResume() {
        super.onResume();

        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            startScanning();
        }
    }

    @Override
    protected void onDestroy() {
        //notificationManager.cancel(NOTIFICATION_ID);
        beaconManager.disconnect();
        super.onDestroy();
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
        unbindService(sphereServiceConnection);
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection sphereServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            final SphereServiceBinder binder = (SphereServiceBinder) service;
            sphereService = binder.getService();
            new GetProducts().execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
        }
    };

    /**
     * Async task to fetch products
     */
    private class GetProducts extends AsyncTask<Void, Void, Void> {
        private static final String NAME_TAG = "name";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            productList = new ArrayList<>();
            products = new ArrayList<>();
            progressDialog = new ProgressDialog(ProductListActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            final SphereRequest productRequest = SphereRequest.get("/products").limit(5);
            sphereService.executeJacksonRequest(productRequest,
                    new Response.Listener<JsonNode>() {
                        @Override
                        public void onResponse(JsonNode response) {
                            processJson(response);
                            setAdapter();
                        }
                    });
            return null;
        }

        protected void setAdapter() {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            /**
             * Updating parsed JSON data into ListView
             * */
            final ListAdapter adapter = new SimpleAdapter(ProductListActivity.this, productList,
                    R.layout.list_item, new String[]{NAME_TAG}, new int[]{R.id.productName});

            setListAdapter(adapter);
        }

        private void processJson(JsonNode queryResult) {
            if (queryResult != null) {
                final Iterator<JsonNode> results = queryResult.get("results").elements();
                while (results.hasNext()) {
                    JsonNode next = results.next();
                    productList.add(createProductEntry(next));
                    products.add(next);
                }
            } else {
                Log.d(this.getClass().getSimpleName(), "Couldn't get any data from the url");
            }
        }

        private HashMap<String, String> createProductEntry(final JsonNode productJson) {
            final HashMap<String, String> productEntry = new HashMap<>();
            final JsonNode masterData = productJson.get("masterData").get("current");
            productEntry.put(NAME_TAG, masterData.get("name").get("en").textValue());
            return productEntry;
        }
    }
}


class NotifyAPI extends AsyncTask<URL, Integer, Long> {

    private Exception exception;

    protected void onPostExecute() {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

    @Override
    protected Long doInBackground(URL... params) {
        int count = params.length;
        long totalSize = 0;
        for (int i = 0; i < count; i++) {
            try {
                URLConnection connection = params[i].openConnection();
                InputStream response = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return totalSize;
    }
}