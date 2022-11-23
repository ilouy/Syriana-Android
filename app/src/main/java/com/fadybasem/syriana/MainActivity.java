package com.fadybasem.syriana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Consumer;
import com.google.android.material.navigation.NavigationBarView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    NavigationBarView bottomNavigationBar;

    final int NOTIFICATION_PERMISSION_REQUEST_CODE = 112;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkUserBlocked();

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.nav_fragment, AllCoursesFragment.class, null)
                .commit();

        bottomNavigationBar = findViewById(R.id.bottomNavigationView);

        SyrianaApp.getAccessToken(() -> checkNewNotifications(SyrianaApp.accessToken));

        bottomNavigationBar.setOnItemSelectedListener(item -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Class fragmentClass = null;

            switch (item.getItemId()) {
                case R.id.all_courses_menu_item:
                    fragmentClass = AllCoursesFragment.class;
                    break;
                case R.id.my_courses_menu_item:
                    fragmentClass = MyCoursesFragment.class;
                    break;
                case R.id.notifications_menu_item:
                    fragmentClass = NotificationsFragment.class;
                    bottomNavigationBar.getOrCreateBadge(R.id.notifications_menu_item).setVisible(false);
                    break;
                case R.id.settings_menu_item:
                    fragmentClass = SettingsFragment.class;
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .remove(getSupportFragmentManager().findFragmentById(R.id.nav_fragment))
                    .add(R.id.nav_fragment, fragmentClass, null)
                    .commit();

            return true;
        });

        //Don't load the current fragment again from the beginning if reselected.
        bottomNavigationBar.setOnItemReselectedListener(item -> {});

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.POST_NOTIFICATIONS) && Build.VERSION.SDK_INT >= 33) {
            EasyPermissions.requestPermissions(this, getString(R.string.notification_permission_rationale), NOTIFICATION_PERMISSION_REQUEST_CODE, Manifest.permission.POST_NOTIFICATIONS);
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

    }

    public void checkNewNotifications (String accessToken) {
        RestOptions options = RestOptions.builder()
                .addHeader("Authorization", accessToken)
                .addPath("/notifications-receipts")
                .build();

        Amplify.API.get(options,
                restResponse -> {
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        boolean seen = (boolean) json.get("seen");

                        runOnUiThread(() -> {
                            bottomNavigationBar.getOrCreateBadge(R.id.notifications_menu_item).setVisible(!seen);

                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                },
                apiFailure -> Log.e("MyAmplifyApp", "GET failed.", apiFailure)
        );
    }

    public void checkUserBlocked () {
        Amplify.Auth.fetchUserAttributes(result -> {
            System.out.println(result);
        }, error -> {
            if (error instanceof AuthException.NotAuthorizedException && error.getCause().getMessage().contains("disabled")) {
                runOnUiThread(() -> {
                    startActivity(new Intent(MainActivity.this, UserBlockedActivity.class));
                    finish();
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyrianaApp.mActivityRef = new WeakReference<>(this);
    }

}