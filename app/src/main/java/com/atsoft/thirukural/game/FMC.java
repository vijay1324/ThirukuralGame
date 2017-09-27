package com.atsoft.thirukural.game;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ATSoft on 9/27/2017.
 */

public class FMC extends FirebaseInstanceIdService {

    String TAG = "FMC Class";
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        Bundle fbundle = new Bundle();
        fbundle.putString(FirebaseAnalytics.Param.ITEM_ID, Defs.gamerName);
        fbundle.putString(FirebaseAnalytics.Param.ITEM_NAME, refreshedToken);
        fbundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, fbundle);
//        sendRegistrationToServer(refreshedToken);
    }
}
