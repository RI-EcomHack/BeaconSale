package de.commercetools.android_example;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static com.estimote.sdk.BeaconManager.MonitoringListener;

/**
 * Demo that shows how to use region monitoring. Two important steps are:
 * <ul>
 * <li>start monitoring region, in example in {@link #onResume()}</li>
 * <li>respond to monitoring changes by registering {@link MonitoringListener} in {@link BeaconManager}</li>
 * </ul>
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class NotifyDemoActivity extends BaseActivity {

  private static final int NOTIFICATION_ID = 123;

  private BeaconManager beaconManager;
  private NotificationManager notificationManager;
  private Region region;

  @Override protected int getLayoutResId() {
    return R.layout.notify_demo;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Beacon beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
    region = new Region("regionId", beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    beaconManager = new BeaconManager(this);

    // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
    // In order for this demo to be more responsive and immediate we lower down those values.
    beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

    beaconManager.setMonitoringListener(new MonitoringListener() {
      @Override
      public void onEnteredRegion(Region region, List<Beacon> beacons) {
        postNotification("Entered region");
      }

      @Override
      public void onExitedRegion(Region region) {
        postNotification("Exited region");
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    notificationManager.cancel(NOTIFICATION_ID);
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        beaconManager.startMonitoring(region);
      }
    });
  }

  @Override
  protected void onDestroy() {
    notificationManager.cancel(NOTIFICATION_ID);
    beaconManager.disconnect();
    super.onDestroy();
  }

  private void postNotification(String msg) {

    Intent notifyIntent = new Intent(NotifyDemoActivity.this, NotifyDemoActivity.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivities(
        NotifyDemoActivity.this,
        0,
        new Intent[]{notifyIntent},
        PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification = new Notification.Builder(NotifyDemoActivity.this)
        .setSmallIcon(R.drawable.beacon_gray)
        .setContentTitle("Notify Demo")
        .setContentText(msg)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build();
    notification.defaults |= Notification.DEFAULT_SOUND;
    notification.defaults |= Notification.DEFAULT_LIGHTS;
    notificationManager.notify(NOTIFICATION_ID, notification);

    TextView statusTextView = (TextView) findViewById(R.id.status);
    statusTextView.setText(msg);
  }




  public void postData() throws JSONException{
    // Create a new HttpClient and Post Header
    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    URL url;

    con.setReadTimeout(7000);
    con.setConnectTimeout(7000);
    con.setDoOutput(true);
    con.setDoInput(true);
    con.setInstanceFollowRedirects(false);
    con.setRequestMethod("POST");
    con.setFixedLengthStreamingMode(param.getBytes().length);
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    // Send
    PrintWriter out = new PrintWriter(con.getOutputStream());
    out.print(param);
    out.close();

    con.connect();

    JSONObject json = new JSONObject();

    try {
      // JSON data:
      json.put("name", "Fahmi Rahman");
      json.put("position", "sysdev");

      JSONArray postjson=new JSONArray();
      postjson.put(json);

      // Post the data:
      httppost.setHeader("json",json.toString());
      httppost.getParams().setParameter("jsonpost",postjson);

      // Execute HTTP Post Request
      System.out.print(json);
      HttpResponse response = httpclient.execute(httppost);

      // for JSON:
      if(response != null)
      {
        InputStream is = response.getEntity().getContent();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
          while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            is.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        text = sb.toString();
      }

      tv.setText(text);

    }catch (ClientProtocolException e) {
      // TODO Auto-generated catch block
    } catch (IOException e) {
      // TODO Auto-generated catch block
    }
  }
}
}
