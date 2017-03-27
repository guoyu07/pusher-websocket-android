package com.pusher.android.notifications;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.pusher.android.notifications.fcm.FCMInstanceIDService;
import com.pusher.android.notifications.fcm.FCMMessagingService;
import com.pusher.android.notifications.gcm.GCMInstanceIDListenerService;
import com.pusher.android.notifications.gcm.GCMRegistrationIntentService;
import com.pusher.android.notifications.gcm.PusherGCMListenerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jamiepatel on 14/08/2016.
 */

public class ManifestValidator {
    void validateGCM(Context context) throws InvalidManifestException {
        // defined in-function so as to avoid NoClassDefFoundErrors when not using GCM
        final ArrayList<Class<? extends Service>> gcmServices =
                new ArrayList<>(Arrays.asList(
                        PusherGCMListenerService.class,
                        GCMInstanceIDListenerService.class,
                        GCMRegistrationIntentService.class));
        checkServicesInManifest(gcmServices, context);
    }

    void validateFCM(Context context) throws InvalidManifestException {
        if(!isInManifest(context, FCMInstanceIDService.class)){
            throw new InvalidManifestException(FCMInstanceIDService.class.getName() +
                    " is not registered in your AndroidManifest.xml");
        }

        if(!isInManifest(context, FCMMessagingService.class)){
            Log.d(ManifestValidator.class.getSimpleName(), FCMMessagingService.class.getName() +
                    " is not registered in your AndroidManifest.xml. " +
                    "If you are implementing your own FirebaseMessagingService this is fine. " +
                    "If not, you need to add it into your manifest.");
        }
    }

    private void checkServicesInManifest(ArrayList<Class<? extends Service>> list, Context context) throws InvalidManifestException {
        for (Class<? extends Service> service : list) {
            if (!isInManifest(context, service)) {

            }
        }
    }

    private boolean isInManifest(Context context, Class<? extends Service> service) {
        Intent intent = new Intent(context, service);
        List<ResolveInfo> info = context.getPackageManager().queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return info.size() > 0;
    }

    public class InvalidManifestException extends Exception {
        InvalidManifestException(String message) {
            super(message);
        }
    }
}
