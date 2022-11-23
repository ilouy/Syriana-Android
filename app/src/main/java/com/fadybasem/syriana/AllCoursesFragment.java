package com.fadybasem.syriana;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.amplifyframework.api.ApiException;
import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class AllCoursesFragment extends Fragment {

    GridView gridView;

    public AllCoursesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getInstructors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_courses, container, false);
        gridView = view.findViewById(R.id.allCoursesGridView);

        return view;
    }

    public void getInstructors () {
        RestOptions options = RestOptions.builder()
                .addPath("/instructors")
                .build();

        ArrayList<String> instructorsArrayList = new ArrayList<>();

        Amplify.API.get(options,
                restResponse -> {
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        JSONArray instructors = (JSONArray) json.get("instructors");
                        System.out.println(instructors);

                        if (getActivity() != null) {

                            getActivity().runOnUiThread(() -> {
                                for (Object instructor : instructors) {
                                    instructorsArrayList.add((String) instructor);
                                }

                                getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);

                                if (gridView != null) {
                                    gridView.setAdapter(new GridViewAdapter(getContext(), R.layout.item_gridview, instructorsArrayList, GridViewType.Instructor, null));

                                    gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                                        getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE));

                                        InstructorCoursesFragment frag = new InstructorCoursesFragment(instructorsArrayList.get(i));

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
                        NoInternetConnectionFragment frag = new NoInternetConnectionFragment(() -> getInstructors());

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
                                .show());

                    }
        });
    }

}