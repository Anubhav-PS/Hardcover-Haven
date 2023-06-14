package com.anubhav.hardcoverhaven.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.activities.HomePageActivity;
import com.anubhav.hardcoverhaven.activities.SignUpActivity;
import com.anubhav.hardcoverhaven.api.CallCloudFunction;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterNumberFragment extends Fragment implements View.OnClickListener {


    private ProgressBar progressBar;
    private MaterialTextView errorTxt;
    private TextInputEditText registerNumberEt;

    private String registerNumber;

    public RegisterNumberFragment() {
        // Required empty public constructor
    }

    public static boolean regNumFormatter(String regNum) {
        final String pattern = "[1-3][0-9][BM][A-Z]{2}[1-9][0-9]{3}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(regNum);
        return m.matches() && regNum.length() == 9;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_number, container, false);

        MaterialTextView createAccountTxt = view.findViewById(R.id.registerNumberPgeToSignUpTxt);
        registerNumberEt = view.findViewById(R.id.registerNumberPgeRegisterEt);
        ImageButton proceedBtn = view.findViewById(R.id.registerNumberPgeProceedBtn);
        errorTxt = view.findViewById(R.id.registerNumberPgeErrorTxt);
        progressBar = view.findViewById(R.id.registerNumberPgeProgressBar);

        // mail id change listeners
        registerNumberEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorTxt.setText("");
                registerNumber = Objects.requireNonNull(registerNumberEt.getText()).toString().trim();
                if (TextUtils.isEmpty(registerNumber)) {
                    errorTxt.setText("register number is required !");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        createAccountTxt.setOnClickListener(this);
        proceedBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.registerNumberPgeToSignUpTxt) {
            goToSignUpPage();
        } else if (id == R.id.registerNumberPgeProceedBtn) {
            goToPasswordFragment();
        }
    }

    private void goToSignUpPage() {
        Intent intent = new Intent(getContext(), SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        requireActivity().finish();
    }

    private void goToPasswordFragment() {

        if (registerNumber == null) {
            errorTxt.setText("register number is required !");
            return;
        }

        if (!regNumFormatter(registerNumber.toUpperCase(Locale.ROOT))) {
            errorTxt.setText("Incorrect register number");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        CallCloudFunction callCloudFunction = new CallCloudFunction(new CallCloudFunction.CloudFunctionCallback() {
            @Override
            public void onSuccess(int code, String result) {
                // Handle the Cloud Function response here
                String message = "Error";
                if (code == 410) {
                    message = "Register Number Does Not Exist";
                } else if (code == 210) {
                    //user found
                    message = result;
                } else {
                    //error
                    message = "There was an error";
                }
                //user not found
                String finalMessage = message;
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    if (code != 210) {
                        errorTxt.setText(finalMessage);
                    }
                });
                if (code == 210) {
                    PasswordFragment passwordFragment = new PasswordFragment();
                    proceedToPassword(result, passwordFragment);
                }
            }

            @Override
            public void onError() {
                // Handle the error here
                String finalMessage = "There was an error";
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    errorTxt.setText(finalMessage);
                });
            }
        });

        String url = "https://us-central1-vitinsiderhostel.cloudfunctions.net/verifyRegisterNumber";
        callCloudFunction.callVerifyRegisterNumber(url, registerNumber.trim().toUpperCase(Locale.ROOT));
    }

    private void proceedToPassword(String result, Fragment passwordFragment) {
        // Create a Bundle and put the data you want to pass
        Bundle bundle = new Bundle();
        bundle.putString("mailID", result);
        passwordFragment.setArguments(bundle);

        // Get the fragment manager and start a new transaction
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left,R.anim.slide_from_left, R.anim.slide_to_right);


        // Replace the current fragment with the new fragment
        fragmentTransaction.replace(R.id.signInPgeFrameLayout, passwordFragment);

        // Add the transaction to the back stack
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
    }

    //process 0 and process 1 functions
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }


}