package com.fadybasem.syriana;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import okhttp3.Cache;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.auth.AuthException;
import com.amplifyframework.core.Action;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.Consumer;

import java.io.File;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    public SettingsFragment () {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.contactUsTextView).setOnClickListener(_view -> {
            boolean isVisible = view.findViewById(R.id.telephoneNumbersLinearLayout).getVisibility() == View.VISIBLE;
            view.findViewById(R.id.telephoneNumbersLinearLayout).setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        getActivity().runOnUiThread(() -> getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE));

        view.findViewById(R.id.logoutTextView).setOnClickListener(_view ->
                Amplify.Auth.signOut(() -> {
                    SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putBoolean("loggedin", false);
                    ed.apply();

                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish();
                }, error -> new AlertDialog.Builder(getContext())
                            .setTitle("Unable to logout.")
                            .setMessage("An error occurred: " + error.getCause().getMessage() + " " + error.getRecoverySuggestion())
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                ));

        view.findViewById(R.id.whatsappImageView).setOnClickListener(_view -> {
            Uri uri = Uri.parse("smsto:+201025308170");
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            i.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(i, ""));
        });

        view.findViewById(R.id.callImageView).setOnClickListener(_view -> {
            String uri = "tel:+201025308170" ;
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        });

        view.findViewById(R.id.aboutUsTextView).setOnClickListener(_view -> new AlertDialog.Builder(getContext())
                .setTitle("About Us")
                .setMessage("Text")
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show());

        view.findViewById(R.id.centerPhoneNumberTextView).setOnClickListener(_view -> {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Number copied to clipboard", "01025308170");
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getContext(), "Number copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}