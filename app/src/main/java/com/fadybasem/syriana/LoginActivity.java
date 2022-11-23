package com.fadybasem.syriana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.result.step.AuthSignInStep;
import com.amplifyframework.core.Amplify;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText phoneNumberEditText;
    TextInputEditText passwordEditText;

    TextView signupTextView;
    TextView forgotPasswordTextView;

    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signupTextView = findViewById(R.id.signupTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.signup_continueButton);

        phoneNumberEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                if (phoneNumberEditText.getText().toString().isEmpty())
                    phoneNumberEditText.setText("+2");
            } else {
                if (phoneNumberEditText.getText().toString().equals("+2"))
                    phoneNumberEditText.setText("");
            }
        });

        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            String oldText;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                oldText = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (oldText.equals("+2") && charSequence.toString().length() < 2 && charSequence.toString().length() != 0) {
                    phoneNumberEditText.setText("+2");
                    phoneNumberEditText.setSelection(2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        loginButton.setOnClickListener(view -> {
            String phoneNumber = phoneNumberEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            Amplify.Auth.signIn(phoneNumber, password, result -> {
                if (result.isSignInComplete()) {
                    sendNotificationToken();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    //Save login status
                    SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putBoolean("loggedin", true);
                    ed.apply();

                    finish();
                } else if (result.getNextStep().getSignInStep() == AuthSignInStep.RESET_PASSWORD) {
                    //TODO: Complete the following cases
                }
                //TODO: All alert dialogs need to be run on main thread.
            }, error -> runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle("Unable to login.")
                        .setMessage("An error occurred: " + error.getCause().getMessage() + " " + error.getRecoverySuggestion())
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show())
            );
        });

        SharedPreferences sp = this.getSharedPreferences("Login", MODE_PRIVATE);

        if (sp.getBoolean("loggedin", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        signupTextView.setOnClickListener(view -> startActivity(new Intent(this, RequestCodeActivity.class)));
        forgotPasswordTextView.setOnClickListener(view -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void sendNotificationToken () {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {

            Amplify.Auth.updateUserAttribute(new AuthUserAttribute(AuthUserAttributeKey.custom("custom:notification_token"), task.getResult()), System.out::println, error -> {
                new AlertDialog.Builder(this)
                        .setTitle("Unable to register for notifications.")
                        .setMessage("An error occurred: " + error.getCause().getMessage() + error.getRecoverySuggestion())
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            });

        });

    }

}