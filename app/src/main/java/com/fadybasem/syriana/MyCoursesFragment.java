package com.fadybasem.syriana;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class MyCoursesFragment extends Fragment {

    GridView gridView;

    public MyCoursesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyrianaApp.getAccessToken(() -> getSubscriptions(SyrianaApp.accessToken));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_courses, container, false);
        gridView = view.findViewById(R.id.myCoursesGridView);

        return view;
    }

    public void getSubscriptions (String accessToken) {
        RestOptions options = RestOptions.builder()
                .addHeader("Authorization", accessToken)
                .addPath("/my-courses")
                .build();

        ArrayList<String> instructorsArrayList = new ArrayList<>();
        ArrayList<ArrayList<String>> coursesArrayList = new ArrayList<>();

        Amplify.API.get(options,
                restResponse -> {
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        JSONArray subscriptions = (JSONArray) json.get("subscriptions");
                        System.out.println(subscriptions);

                        if (subscriptions == null) {
                            return;
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);

                                for (Object subscription : subscriptions) {
                                    instructorsArrayList.add((String) ((JSONObject) subscription).get("instructor"));

                                    ArrayList<String> courses = new ArrayList<>();

                                    for (Object course : (JSONArray) ((JSONObject) subscription).get("courses")) {
                                        courses.add((String) course);
                                    }

                                    coursesArrayList.add(courses);
                                }

                                if (gridView != null) {
                                    gridView.setAdapter(new GridViewAdapter(getContext(), R.layout.item_gridview, instructorsArrayList, GridViewType.Instructor, null));

                                    gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                                        getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE));

                                        InstructorCoursesFragment frag = new InstructorCoursesFragment(instructorsArrayList.get(i), coursesArrayList.get(i));

                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .setReorderingAllowed(true)
                                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                                .add(R.id.nav_fragment, frag)
                                                .addToBackStack("instructor-courses")
                                                .commit();
                                    });
                                }

                            });
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
                        NoInternetConnectionFragment frag = new NoInternetConnectionFragment(() -> getSubscriptions(accessToken));

                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.nav_fragment, frag)
                                .commit();
                    } else {
                        getActivity().runOnUiThread(() -> new AlertDialog.Builder(getContext())
                                .setTitle("Unable to load courses.")
                                .setMessage("An error occurred: " + apiFailure.getCause().getMessage() + " " + apiFailure.getRecoverySuggestion())
                                .setPositiveButton(android.R.string.ok, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                        );

                    }
                });
    }
}