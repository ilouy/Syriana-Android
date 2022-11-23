package com.fadybasem.syriana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

import java.util.ArrayList;

public class PasswordsActivity extends AppCompatActivity {

    EditText passwordEditText;
    EditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords);

        passwordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);

        if (!getIntent().getBooleanExtra("signup", true)) {
            ((Button) findViewById(R.id.signupButton)).setText("Reset Password");
        }

            findViewById(R.id.signupButton).setOnClickListener(view -> {
            if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
                confirmPasswordEditText.setError("Passwords do not match");

                return;
            }

            String password = passwordEditText.getText().toString();

            if (isValidPassword(password)) {
                if (getIntent().getBooleanExtra("signup", true)) {
                    //Activity being used for signup
                    performSignup(password);
                } else {
                    //Activity being used for password reset
                    resetPassword(password);
                }
            } else {
                passwordEditText.setError("Passwords should contain:\n•At least 8 characters.\n•At least 1 uppercase letter.\n•At least 1 lowercase letter.\n•At least 1 number.");
            }
        });

    }

    private void performSignup(String password) {
        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        ArrayList<AuthUserAttribute> attributes = new ArrayList<>();

        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.name(), getIntent().getStringExtra("name")));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.gender(), getIntent().getStringExtra("gender")));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), phoneNumber));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.email(), getIntent().getStringExtra("email")));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.custom("custom:notification_token"), getIntent().getStringExtra("requestCode")));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.custom("custom:year"), getIntent().getStringExtra("year")));

        AuthSignUpOptions options = AuthSignUpOptions.builder()
                .userAttributes(attributes)
                .build();

        Amplify.Auth.signUp(phoneNumber, password, options, result -> {
            Log.d("signup", result.toString());
            try {
                Thread.sleep(5000);

                Amplify.Auth.signIn(phoneNumber, password, value -> {
                    startActivity(new Intent(PasswordsActivity.this, MainActivity.class));
                    //Save login status
                    SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putBoolean("loggedin", true);
                    ed.apply();

                }, error -> {
                    runOnUiThread(() -> new AlertDialog.Builder(this)
                            .setTitle("Unable to sign in.")
                            .setMessage("An error occurred: " + error.getCause().getMessage() + " " + error.getRecoverySuggestion())
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show());
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, error -> runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Unable to signup.")
                .setMessage("An error occurred: " + error.getCause().getMessage() + " " + error.getRecoverySuggestion())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()));

    }

    private void resetPassword (String password) {
        Amplify.Auth.confirmResetPassword(password, getIntent().getStringExtra("verificationCode"), () -> {
            Toast.makeText(this, "Password Reset Successfully", Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, error -> {
            runOnUiThread(() -> new AlertDialog.Builder(PasswordsActivity.this)
                    .setTitle("Error Occurred!")
                    .setMessage(error.getCause().getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            );


        });
    }

    public static boolean isValidPassword (String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$");
    }
}