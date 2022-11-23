package com.fadybasem.syriana;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsFragment extends Fragment {

    RecyclerView notificationsRecyclerView;

    public NotificationsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyrianaApp.getAccessToken(() -> getNotifications(SyrianaApp.accessToken));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);

        return view;
    }

    public void getNotifications (String accessToken) {
        RestOptions options = RestOptions.builder()
                .addHeader("Authorization", accessToken)
                .addPath("/notifications")
                .build();

        ArrayList<HashMap<String, String>> notificationsArrayList = new ArrayList<>();

        Amplify.API.get(options,
                restResponse -> {

                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        JSONArray notifications = (JSONArray) json.get("notifications");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE));
                        }

                        if (notifications != null) {
                            if (getActivity() != null) {
                                if (notifications.size() == 0) {
                                    getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.noNotificationsTextView).setVisibility(View.VISIBLE));
                                } else {
                                    for (Object notification : notifications) {
                                        HashMap<String, String> notificationHashMap = new HashMap<>();
                                        notificationHashMap.put("body", (String) ((JSONObject) notification).get("body"));
                                        notificationHashMap.put("datetime", (String) ((JSONObject) notification).get("datetime"));

                                        notificationsArrayList.add(notificationHashMap);
                                    }

                                    getActivity().runOnUiThread(() -> {

                                        if (notificationsRecyclerView != null) {
                                            notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                            notificationsRecyclerView.setAdapter(new NotificationsRecyclerViewAdapter(notificationsArrayList));
                                        }
                                    });


                                }
                            }

                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                },
                apiFailure -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE));
                    }

                    if (apiFailure.getCause().getMessage().contains("Unable to resolve host")) {
                        //no internet
                        NoInternetConnectionFragment frag = new NoInternetConnectionFragment(() -> getNotifications(accessToken));

                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.nav_fragment, frag)
                                .commit();
                    } else {
                        getActivity().runOnUiThread(() -> new AlertDialog.Builder(getContext())
                                .setTitle("Unable to fetch notifications.")
                                .setMessage("An error occurred: " + apiFailure.getCause().getMessage() + apiFailure.getRecoverySuggestion())
                                .setPositiveButton(android.R.string.ok, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                        );

                    }
                });
    }
}