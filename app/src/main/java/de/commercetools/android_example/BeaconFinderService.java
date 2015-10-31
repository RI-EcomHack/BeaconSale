package de.commercetools.android_example.adapters;

public class BeaconFinderService extends IntentService {
    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

       new Handler().postDelayed(new Runnable() {
               public void run() {
                   String url = "http://5.196.27.161:8080/customer_in_range/3be9c767-11c0-4280-ad5d-a3135a138c6c/2634";
                   URL url = new URL(url);
                      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                      try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        readStream(in);
                       finally {
                        urlConnection.disconnect();
                      }
                    }
               }
           }, 10000);
    }
}