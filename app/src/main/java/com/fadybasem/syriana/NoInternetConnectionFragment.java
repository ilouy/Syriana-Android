package com.fadybasem.syriana;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NoInternetConnectionFragment extends Fragment {
    Runnable tryAgain;

    public NoInternetConnectionFragment() {

    }

    public NoInternetConnectionFragment(Runnable tryAgain) {
        this.tryAgain = tryAgain;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_internet_connection, container, false);
        view.findViewById(R.id.noInternetFragment_tryAgainButton).setOnClickListener(_view -> {
            tryAgain.run();
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        });

        return view;
    }
}