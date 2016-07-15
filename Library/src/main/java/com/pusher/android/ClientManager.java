package com.pusher.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by jamiepatel on 15/07/2016.
 */

class ClientManager {
    static final String PUSHER_PUSH_CLIENT_ID_KEY = "__pusher__client__key__";
    private static final String TAG = "PClientManager";
    private final String clientId;
    private final Context context;
    private final Outbox outbox;
    private final String appKey;
    private final PusherPushNotificationRegistrationOptions options;

    ClientManager(String clientId, Context context, Outbox outbox, String appKey, PusherPushNotificationRegistrationOptions options) {
        this.clientId = clientId;
        this.context = context;
        this.outbox = outbox;
        this.appKey = appKey;
        this.options = options;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(PUSHER_PUSH_CLIENT_ID_KEY, clientId).apply();
        flushOutbox();
    }

    void flushOutbox() {
        Log.d(TAG, "Trying to flushing outbox");
        if (outbox.size() > 0) {
            final Outbox.Item item = outbox.remove(0);
            JSONObject json = new JSONObject();
            try {
                json.put("app_key", appKey);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            StringEntity entity = new StringEntity(json.toString(), "UTF-8");

            Runnable successCallback = new Runnable() {
                @Override
                public void run() {
                    flushOutbox();
                }
            };

            String url = options.buildURL("/clients/" + clientId + "/interests/") + item.getInterest();

            Factory factory = Factory.getInstance();
            ResponseHandlerInterface handler = factory.newSubscriptionChangeHandler(item, successCallback);
            AsyncHttpClient client = factory.newAsyncHttpClient();
            switch (item.getChange()) {
                case SUBSCRIBE:
                    client.post(context, url, entity, "application/json", handler);
                    break;
                case UNSUBSCRIBE:
                    client.delete(context, url, entity, "application/json", handler);
                    break;
            }
        }
    }
}
