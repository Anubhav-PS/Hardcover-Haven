package com.anubhav.hardcoverhaven.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.enums.ErrorCode;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.models.AlertDisplay;
import com.anubhav.hardcoverhaven.notifications.AppNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {


    //mail domain pattern
    private final String studentMailPattern = "@vitstudent.ac.in";
    ;

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference accountsSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.ACCOUNTS.getPath());


    // input field views
    private TextInputEditText mailEt, passwordEt;
    private ProgressBar progressBar;
    // input field string values
    private String inputMail;
    private String inputPassword;

    //input flags
    private boolean validMail = false;
    private boolean validPassword = false;

    //firebase authentication declarations
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //firebase auth instantiation
        firebaseAuth = FirebaseAuth.getInstance();

        // authStateListener definition
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();

            if (currentUser != null) {

            } else {

            }
        };

        AppNotification.getInstance().subscribeAllTopics();

        mailEt = findViewById(R.id.registerPgeMailTxt);
        passwordEt = findViewById(R.id.registerPgePasswordTxt);
        progressBar = findViewById(R.id.registerPgeProgressBar);

        MaterialButton createAccount = findViewById(R.id.signUpPageRegisterBtn);
        MaterialTextView toLogin = findViewById(R.id.signUpPageToLoginPageTxt);


        // mail id change listeners
        mailEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (Objects.requireNonNull(mailEt.getText()).toString().trim().isEmpty()) {
                    mailEt.setError("email i'd is required !");
                }
            }
        });

        mailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mailEt.setError(null);
                inputMail = Objects.requireNonNull(mailEt.getText()).toString().trim();
                if (TextUtils.isEmpty(inputMail)) {
                    mailEt.setError("email i'd is required !");
                    validMail = false;
                } else validMail = inputMail.endsWith(studentMailPattern);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // password change listeners
        passwordEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (Objects.requireNonNull(passwordEt.getText()).toString().trim().isEmpty()) {
                    passwordEt.setError("Minimum 10 characters");
                }
            }
        });

        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordEt.setError(null);
                inputPassword = Objects.requireNonNull(passwordEt.getText()).toString().trim();
                if (inputPassword.isEmpty()) {
                    passwordEt.setError("Password is required !");
                    validPassword = false;
                } else validPassword = inputPassword.length() >= 10;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // button click listeners
        createAccount.setOnClickListener(this);
        toLogin.setOnClickListener(this);

    }

    //onclick listener
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.signUpPageRegisterBtn) {
            try {
                validation();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.signUpPageToLoginPageTxt) {
            toLoginPage();
        }
    }


    // validation
    private void validation() throws NoSuchAlgorithmException {

        if (!validMail && !validPassword) {
            final String message = "Fill all the fields to proceed !";
            callSnackBar(message);
            return;
        }

        if (!validMail) {
            mailEt.setError("Invalid email i'd");
            return;
        }

        if (!validPassword) {
            passwordEt.setError("Minimum 10 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        uploadData();

    }

    // push to cloud
    private void uploadData() {

        Map<String, Object> request = new HashMap<>();
        request.put("studentMailId", inputMail.toLowerCase(Locale.ROOT));


        firebaseAuth.createUserWithEmailAndPassword(inputMail, inputPassword)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        String userId = currentUser.getUid();
                        request.put("user_UID", userId);

                        accountsSection.document("REQUEST_Q").collection("FILES").document()
                                .set(request)
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        firebaseAuth.getCurrentUser().sendEmailVerification()
                                                .addOnCompleteListener(
                                                        getVoidOnCompleteListener()
                                                )
                                                .addOnFailureListener(SignUpActivity.this::ifFailure);
                                    } else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnFailureListener(SignUpActivity.this::ifFailure);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }).addOnFailureListener(SignUpActivity.this::ifFailure);
    }

    @NonNull
    private OnCompleteListener<Void> getVoidOnCompleteListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task3) {
                if (task3.isSuccessful()) {
                    SignUpActivity.this.promptRegisteredSuccessfully();
                } else {
                    SignUpActivity.this.promptFailedToSendVerification();
                }
            }
        };
    }

    // on back pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    // snack bar method
    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(SignUpActivity.this, findViewById(R.id.registerPge), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(SignUpActivity.this, R.color.blue_100));
        snackbar.show();
    }


    // go to login page
    private void toLoginPage() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        finish();
    }

    //---process 0 and process 1 definition
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        currentUser = firebaseAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }


    private void promptRegisteredSuccessfully() {
        progressBar.setVisibility(View.INVISIBLE);
        AlertDisplay alertDisplay = new AlertDisplay("Registered Successfully", "Verify the link that is sent to your VIT mail and then login", SignUpActivity.this);
        alertDisplay.getBuilder().setPositiveButton("Ok", (dialogInterface, i) -> toLoginPage());
        alertDisplay.getBuilder().setCancelable(false);
        alertDisplay.display();
    }

    private void promptFailedToSendVerification() {
        progressBar.setVisibility(View.INVISIBLE);
        AlertDisplay alertDisplay = new AlertDisplay("ERROR CODE " + ErrorCode.RA006.getErrorCode(), ErrorCode.RA006.getErrorMessage(), SignUpActivity.this);
        alertDisplay.getBuilder().setCancelable(false);
        alertDisplay.displayAlert();
    }

    private void ifFailure(Exception e) {
        progressBar.setVisibility(View.INVISIBLE);
        callSnackBar(e.getLocalizedMessage());
    }


}