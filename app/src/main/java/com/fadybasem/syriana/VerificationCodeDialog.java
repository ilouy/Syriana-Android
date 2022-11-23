package com.fadybasem.syriana;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;
import com.fraggjkee.smsconfirmationview.SmsConfirmationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class VerificationCodeDialog extends DialogFragment {
    Button cancelButton;
    Button continueButton;

    SmsConfirmationView verificationCodeEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        Amplify.Auth.resetPassword(arguments.getString("username"), result -> {
            Toast.makeText(getContext(), "Verification code sent", Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(getContext(), error.getCause().getMessage(), Toast.LENGTH_LONG).show();
            dismiss();
        });

        View view = inflater.inflate(R.layout.dialog_verification_code, null);

        cancelButton = view.findViewById(R.id.verificationCodeDialog_cancelButton);
        continueButton = view.findViewById(R.id.verificationCodeDialog_continueButton);
        verificationCodeEditText = view.findViewById(R.id.verificationCodeDialog_verificationCodeEditText);

        cancelButton.setOnClickListener(_view -> dismiss());
        continueButton.setOnClickListener(_view -> {
            if (verificationCodeEditText.getEnteredCode().length() != 6) {
                Toast.makeText(getContext(), "Please enter the verification code.", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(getContext(), PasswordsActivity.class);
            intent.putExtra("verificationCode", verificationCodeEditText.getEnteredCode());
            intent.putExtra("signup", false);

            startActivity(intent);

        });

        verificationCodeEditText.setOnClickListener(view1 -> {
            verificationCodeEditText.requestFocus();
            ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, 0);

        });

        builder.setView(view);
        return builder.create();
    }

}
