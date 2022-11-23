package com.fadybasem.syriana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;

public class RequestCodeActivity extends AppCompatActivity {

    EditText requestCodeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_code);

        requestCodeEditText = findViewById(R.id.requestCodeEditText);

        findViewById(R.id.signupButton).setOnClickListener(view -> {
            if (requestCodeEditText.getText().toString().isEmpty()) {
                requestCodeEditText.setError("Please enter your request code.");
                return;
            }

            checkRequestCode(requestCodeEditText.getText().toString());

        });
    }

    public void checkRequestCode (String requestCode) {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("requestCode", requestCode);

        RestOptions options = RestOptions.builder()
                .addPath("/request-codes")
                .addQueryParameters(queryParams)
                .build();

        Amplify.API.get(options,
                restResponse -> {
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                        boolean exists = (boolean) json.get("exists");

                        if (exists) {
                            Intent intent = new Intent(RequestCodeActivity.this, SignupActivity.class);
                            intent.putExtra("requestCode", requestCode);

                            startActivity(intent);
                        } else {
                            runOnUiThread(() -> requestCodeEditText.setError("Incorrect request code."));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                },
                apiFailure -> {
                    runOnUiThread(() -> new AlertDialog.Builder(RequestCodeActivity.this)
                            .setTitle("Unable to check request code.")
                            .setMessage("An error occurred: " + apiFailure.getCause().getMessage() + " " + apiFailure.getRecoverySuggestion())
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    );

                }
        );
    }
}