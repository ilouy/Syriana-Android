package com.fadybasem.syriana;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;

public class SyrianaApp extends Application {
    public static String accessToken;
    public static WeakReference<Activity> mActivityRef;

    public void onCreate() {
        super.onCreate();

        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(AWSApiPlugin.builder()
                    .configureClient("Syriana API", okHttpClientBuilder -> okHttpClientBuilder.readTimeout(30, TimeUnit.SECONDS))
                    .build());

            Amplify.configure(getApplicationContext());
            Log.i("MyAmplifyApp", "Initialized Amplify");

        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
    }

    public static void getAccessToken(Runnable completionHandler) {
        Amplify.Auth.fetchAuthSession(
                result -> {
                    AWSCognitoAuthSession cognitoAuthSession = (AWSCognitoAuthSession) result;
                    switch (cognitoAuthSession.getIdentityId().getType()) {
                        case SUCCESS:
                            accessToken = cognitoAuthSession.getUserPoolTokens().getValue().getAccessToken();
                            System.out.println(accessToken);
                            completionHandler.run();
                            break;
                        case FAILURE:
                            Log.i("AuthQuickStart", "IdentityId not present because: " + cognitoAuthSession.getIdentityId().getError().toString());

                            new AlertDialog.Builder(mActivityRef.get())
                                    .setTitle("Unable to fetch access token.")
                                    .setMessage("An error occurred: " + cognitoAuthSession.getIdentityId().getError())
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                    }
                },
                error -> {
                    if (mActivityRef != null) {
                        new Handler(Looper.getMainLooper()).post(() -> new AlertDialog.Builder(mActivityRef.get())
                                .setTitle("Unable to fetch access token.")
                                .setMessage("An error occurred: " + error.getCause().getMessage() + " " + error.getRecoverySuggestion())
                                .setPositiveButton(android.R.string.ok, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show());


                    }
                }
        );
    }
}
