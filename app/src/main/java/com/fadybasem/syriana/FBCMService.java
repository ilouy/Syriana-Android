package com.fadybasem.syriana;

import android.app.Activity;
import android.content.SharedPreferences;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.core.Amplify;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class FBCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);

        if (sp.getBoolean("loggedin", false)) {
            Amplify.Auth.updateUserAttribute(new AuthUserAttribute(AuthUserAttributeKey.custom("custom:notification_token"), token), System.out::println, error -> {
                Activity mainActivity = SyrianaApp.mActivityRef.get();

                if (mainActivity != null) {
                    new AlertDialog.Builder(mainActivity)
                            .setTitle("Unable to logout.")
                            .setMessage("An error occurred: " + error.getCause().getMessage() + error.getRecoverySuggestion())
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String instructor = message.getData().get("instructor");
        String course = message.getData().get("course");

        if (instructor != null && course != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(instructor + "." + course);
        }
        Activity mainActivity = SyrianaApp.mActivityRef.get();

        ((BottomNavigationView) mainActivity.findViewById(R.id.bottomNavigationView)).getOrCreateBadge(R.id.notifications_menu_item).setVisible(true);

    }
}
