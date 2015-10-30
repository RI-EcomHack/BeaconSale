package de.commercetools.android_example;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CartActivity extends ListActivity {

    private static final String CART_ID = "cartId";
    private static final String NAME_TAG = "name";
    private static final String PRICE_TAG = "price";

    private ObjectMapper mapper = new ObjectMapper();
    private ArrayList<HashMap<String, String>> lineItemList = new ArrayList<>();

    private SphereService sphereService;
    private boolean bound = false;

    private void createCartAndAddProduct(final String productId) {
        if (bound) {
            ObjectNode body = mapper.createObjectNode();
            body.put("currency", "EUR");

            final SphereRequest request = SphereRequest.post("/carts", body.toString());
            sphereService.executeJacksonRequest(request,
                    new Response.Listener<JsonNode>() {
                        @Override
                        public void onResponse(JsonNode response) {
                           final SharedPreferences prefs = getSharedPreferences("ctp.cart", 0);
                            final SharedPreferences.Editor edit = prefs.edit();
                            edit.putString(CART_ID, response.get("id").asText());
                            addProductToCart(productId, response);
                            edit.commit();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            logError(error);
                        }
                    });
        }
    }

    private void getCartAndAddProduct(final String productId, final String cartId) {
        final SphereRequest getCart = SphereRequest.get("/carts/" + cartId );
        sphereService.executeJacksonRequest(getCart,
                new Response.Listener<JsonNode>() {
                    @Override
                    public void onResponse(JsonNode response) {
                        addProductToCart(productId, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        logError(error);
                    }
                });
    }

    private void addProductToCart(final String productId, final JsonNode cart) {
        if (bound) {
            final String cartId = cart.get("id").asText();
            final Long cartVersion = cart.get("version").asLong();

            final ObjectNode body = mapper.createObjectNode();
            final ArrayNode actions = mapper.createArrayNode();
            final ObjectNode action = mapper.createObjectNode();
            body.put("version", cartVersion);
            body.set("actions", actions);
            actions.add(action);
            action.put("action", "addLineItem");
            action.put("productId", productId);
            action.put("variantId", 1);
            action.put("quantity", 1);

            Log.d(this.getClass().getSimpleName(), body.asText());

            final SphereRequest addLineItem = SphereRequest.post("/carts/" + cartId, body.toString());
            sphereService.executeJacksonRequest(addLineItem,
                    new Response.Listener<JsonNode>() {
                        @Override
                        public void onResponse(JsonNode response) {
                            processJson(response);
                            setAdapter();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            logError(error);
                        }
                    });
        }
    }

    private void processJson(final JsonNode cart) {
        final Iterator<JsonNode> lineItems = cart.get("lineItems").elements();
        while (lineItems.hasNext()) {
            JsonNode next = lineItems.next();
            lineItemList.add(createLineItemEntry(next));
        }
    }

    private HashMap<String, String> createLineItemEntry(final JsonNode lineItem) {
        final HashMap<String, String> lineItemEntry = new HashMap<>();

        final String name = lineItem.get("name").get("en").asText();
        final long quantity = lineItem.get("quantity").asLong();
        final JsonNode totalPrice = lineItem.get("totalPrice");
        final int centAmount = totalPrice.get("centAmount").asInt();
        final String currencyCode = totalPrice.get("currencyCode").asText();
        final double euroAmount = centAmount / 100;

        lineItemEntry.put(NAME_TAG, quantity + "x " + name);
        lineItemEntry.put(PRICE_TAG, String.format("%.2f %s", euroAmount, currencyCode));

        return lineItemEntry;
    }

    protected void setAdapter() {
        /**
         * Updating parsed JSON data into ListView
         * */
        final ListAdapter adapter = new SimpleAdapter(CartActivity.this, lineItemList,
                R.layout.line_item, new String[]{NAME_TAG, PRICE_TAG}, new int[]{R.id.productName, R.id.productPrice});

        setListAdapter(adapter);
    }

    private void logError(final VolleyError error) {
        Log.e(this.getClass().getSimpleName(), new String(error.networkResponse.data));
    }

    /**
     * Lifecycle stuff
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
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

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection sphereServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            final SphereServiceBinder binder = (SphereServiceBinder) service;
            sphereService = binder.getService();
            bound = true;

            final SharedPreferences prefs = getSharedPreferences("ctp.cart", 0);
            final String productId = getIntent().getStringExtra("productId");

            if (!prefs.contains(CART_ID)) {
                createCartAndAddProduct(productId);
            } else {
                getCartAndAddProduct(productId, prefs.getString(CART_ID, null));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            bound = false;
        }
    };
}
