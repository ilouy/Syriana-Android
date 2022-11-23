package com.fadybasem.syriana;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CourseVideosFragment extends Fragment {
    GridView gridView;

    String instructor;
    String course;
    boolean markPreviews;

    public CourseVideosFragment() {

    }

    public CourseVideosFragment(String instructor, String course, boolean markPreviews) {
        this.instructor = instructor;
        this.course = course;
        this.markPreviews = markPreviews;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getCourseVideos(instructor, course);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_videos, container, false);
        gridView = view.findViewById(R.id.courseVideosGridView);
        ((TextView) view.findViewById(R.id.courseVideosTextView)).setText(course);


        return view;
    }

    public void getCourseVideos (String instructor, String course) {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instructor", instructor);
        queryParams.put("course", course);

        RestOptions options = RestOptions.builder()
                .addPath("/videos")
                .addQueryParameters(queryParams)
                .build();

        ArrayList<String> videosArrayList = new ArrayList<>();
        ArrayList<HashMap<String, String>> thumbnailsArrayList = new ArrayList<>();

        Amplify.API.get(options,
                restResponse -> {
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        JSONArray videos = (JSONArray) json.get("videos");
                        System.out.println(videos);

                        if (getActivity() != null) {

                            getActivity().runOnUiThread(() -> {
                                getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);

                                for (Object video : videos) {
                                    videosArrayList.add((String) ((JSONObject) video).get("name"));

                                    HashMap<String, String> hm = new HashMap<>();
                                    hm.put("thumbnailURL", (String) ((JSONObject) video).get("thumbnail"));
                                    if (markPreviews)
                                        hm.put("isPreview", ((boolean) ((JSONObject) video).get("isPreview")) ? "true" : "false");
                                    thumbnailsArrayList.add(hm);

                                }

                                if (gridView != null) {
                                    gridView.setAdapter(new GridViewAdapter(getContext(), R.layout.item_gridview, videosArrayList, GridViewType.Video, thumbnailsArrayList));

                                    gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                                        if (!markPreviews || Objects.equals(thumbnailsArrayList.get(i).get("isPreview"), "true")) {
                                            Intent intent = new Intent(getContext(), VideoActivity.class);
                                            intent.putExtra("instructor", instructor);
                                            intent.putExtra("course", course);
                                            intent.putExtra("video", videosArrayList.get(i));

                                            getActivity().startActivity(intent);
                                        } else {
                                            Toast.makeText(getContext(), "Preview not available", Toast.LENGTH_LONG).show();
                                        }
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

                    getActivity().runOnUiThread(() -> new AlertDialog.Builder(getContext())
                            .setTitle("Unable to load videos.")
                            .setMessage("An error occurred: " + apiFailure.getCause().getMessage() + " " + apiFailure.getRecoverySuggestion())
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    );

                }
        );
    }
}