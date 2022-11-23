package com.fadybasem.syriana;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

public class InstructorCoursesFragment extends Fragment {

    GridView gridView;
    String instructor;
    ArrayList<String> courses = null;

    public InstructorCoursesFragment() {

    }

    //Constructor for loading all courses for the instructor
    public InstructorCoursesFragment(String instructor) {
        this.instructor = instructor;
    }

    //Constructor for loading only specific(given) courses
    public InstructorCoursesFragment(String instructor, ArrayList<String> courses) {
        this.instructor = instructor;
        this.courses = courses;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (courses == null) {
            getInstructorCourses(instructor);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instructor_courses, container, false);
        gridView = view.findViewById(R.id.instructorCoursesGridView);
        ((TextView) view.findViewById(R.id.instructorCoursesTextView)).setText(instructor);

        if (courses != null) {
            getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE));
            gridView.setAdapter(new GridViewAdapter(getContext(), R.layout.item_gridview, courses, GridViewType.Course, null));

            gridView.setOnItemClickListener((adapterView, _view, i, l) -> {
                CourseVideosFragment frag = new CourseVideosFragment(instructor, courses.get(i), false);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .add(R.id.nav_fragment, frag)
                        .addToBackStack("course-videos")
                        .commit();
            });
        }

        return view;
    }

    public void getInstructorCourses (String instructor) {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instructor", instructor);

        RestOptions options = RestOptions.builder()
                .addPath("/courses")
                .addQueryParameters(queryParams)
                .build();

        ArrayList<String> coursesArrayList = new ArrayList<>();

        Amplify.API.get(options,
                restResponse -> {
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        JSONArray courses = (JSONArray) json.get("courses");
                        System.out.println(courses);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);

                                for (Object course : courses) {
                                    coursesArrayList.add((String) course);
                                }

                                if (gridView != null) {
                                    gridView.setAdapter(new GridViewAdapter(getContext(), R.layout.item_gridview, coursesArrayList, GridViewType.Course, null));
                                    gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                                        getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE));

                                        CourseVideosFragment frag = new CourseVideosFragment(instructor, coursesArrayList.get(i), true);

                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .setReorderingAllowed(true)
                                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                                                .add(R.id.nav_fragment, frag)
                                                .addToBackStack("course-videos")
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
                        NoInternetConnectionFragment frag = new NoInternetConnectionFragment(() -> getInstructorCourses(instructor));

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