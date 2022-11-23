package com.fadybasem.syriana;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText phoneNumberEditText;
    Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        phoneNumberEditText = findViewById(R.id.forgotPassword_phoneNumberEditText);
        continueButton = findViewById(R.id.forgotPassword_continueButton);

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

        phoneNumberEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                if (phoneNumberEditText.getText().toString().isEmpty())
                    phoneNumberEditText.setText("+2");
            } else {
                if (phoneNumberEditText.getText().toString().equals("+2"))
                    phoneNumberEditText.setText("");
            }
        });

        continueButton.setOnClickListener(view -> {
            if (!isValidPhoneNumber(phoneNumberEditText.getText().toString())) {
                phoneNumberEditText.setError("Invalid phone number");
                return;
            }

            VerificationCodeDialog dialog = new VerificationCodeDialog();
            Bundle data = new Bundle();
            data.putString("username", phoneNumberEditText.getText().toString());

            dialog.setArguments(data);

            dialog.show(getSupportFragmentManager(), "verificationCodeDialog");
        });

    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.charAt(2) == '0' && phoneNumber.charAt(3) == '1' && phoneNumber.length() == 13;
    }
}