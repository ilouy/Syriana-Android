package com.fadybasem.syriana;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    EditText nameEditText;
    EditText emailEditText;
    EditText phoneNumberEditText;
    EditText yearEditText;

    Button continueButton;

    ImageView maleIcon;
    ImageView femaleIcon;

    String gender;
    String requestCode;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameEditText = findViewById(R.id.signup_nameEditText);
        emailEditText = findViewById(R.id.signup_emailEditText);
        phoneNumberEditText = findViewById(R.id.signup_phoneNumberEditText);
        yearEditText = findViewById(R.id.signup_yearEditText);

        maleIcon = findViewById(R.id.maleImageView);
        femaleIcon = findViewById(R.id.femaleImageView);

        continueButton = findViewById(R.id.signup_continueButton);

        continueButton.setOnClickListener(view -> {
            if (nameEditText.getText().toString().isEmpty()) {
                nameEditText.setError("Name cannot be empty");
                return;
            }

            if (!isValidPhoneNumber(phoneNumberEditText.getText().toString())){
                phoneNumberEditText.setError("Invalid phone number");
                return;
            }

            if (!isValidEmailAddress(emailEditText.getText().toString())){
                emailEditText.setError("Invalid email address");
                return;
            }

            if (yearEditText.getText().toString().isEmpty()) {
                nameEditText.setError("Year cannot be empty");
                return;
            }

            if (gender == null){
                Toast.makeText(SignupActivity.this, "Please choose your gender.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(this, PasswordsActivity.class);
            intent.putExtra("name", nameEditText.getText().toString());
            intent.putExtra("gender", gender);
            intent.putExtra("phoneNumber", phoneNumberEditText.getText().toString());
            intent.putExtra("email", emailEditText.getText().toString());
            intent.putExtra("year", yearEditText.getText().toString());
            intent.putExtra("requestCode", requestCode);

            startActivity(intent);

        });

        maleIcon.setOnClickListener(view -> {
            maleIcon.setBackgroundColor(0xE6E6E6E6);
            femaleIcon.setBackgroundColor(0x00000000);

            gender = "male";
        });

        femaleIcon.setOnClickListener(view -> {
            femaleIcon.setBackgroundColor(0xE6E6E6E6);
            maleIcon.setBackgroundColor(0x00000000);

            gender = "female";
        });

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

        findViewById(R.id.genderSelectionLayout).setClipToOutline(true);
        requestCode = getIntent().getStringExtra("requestCode");
    }

    private boolean isValidEmailAddress(String email) {
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.charAt(2) == '0' && phoneNumber.charAt(3) == '1' && phoneNumber.length() == 13;
    }

}