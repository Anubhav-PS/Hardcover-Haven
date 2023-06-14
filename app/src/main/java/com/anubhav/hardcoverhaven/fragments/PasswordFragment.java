package com.anubhav.hardcoverhaven.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.activities.HomePageActivity;
import com.anubhav.hardcoverhaven.database.LocalSqlDatabase;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.interfaces.iOnFCMTokenGenerated;
import com.anubhav.hardcoverhaven.interfaces.iOnNotifyDbProcess;
import com.anubhav.hardcoverhaven.models.User;
import com.anubhav.hardcoverhaven.notifications.AppNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PasswordFragment extends Fragment implements View.OnClickListener, iOnFCMTokenGenerated, iOnNotifyDbProcess {


    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference accountSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.ACCOUNTS.getPath());
    private final CollectionReference tokenSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.FCM_TOKEN.getPath());

    //firebase auth  declarations
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;


    private TextInputEditText passwordEt;
    private ProgressBar progressBar;
    private MaterialTextView errorTxt, resetTxt;
    private MaterialButton loginBtn;
    private View rootView;

    private String mailID;
    private String password;


    //local database
    private LocalSqlDatabase localSqlDatabase;

    //listeners
    private iOnFCMTokenGenerated onFCMTokenGenerated;

    public PasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_password, container, false);

        // Retrieve the data passed from the previous fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            mailID = bundle.getString("mailID");
        }

        //firebase auth instantiation
        firebaseAuth = FirebaseAuth.getInstance();
        //firebase authState listener definition
        authStateListener = firebaseAuth -> firebaseUser = firebaseAuth.getCurrentUser();

        //listeners
        onFCMTokenGenerated = this;

        passwordEt = rootView.findViewById(R.id.passwordPgePasswordEt);
        errorTxt = rootView.findViewById(R.id.passwordPgeErrorTxt);
        resetTxt = rootView.findViewById(R.id.passwordPgeResetTxt);
        loginBtn = rootView.findViewById(R.id.passwordPgeLoginBtn);
        progressBar = rootView.findViewById(R.id.passwordPgeProgressBar);

        passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorTxt.setText("");
                password = Objects.requireNonNull(passwordEt.getText()).toString().trim();
                if (password.isEmpty()) {
                    errorTxt.setText("Password is required !");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        loginBtn.setOnClickListener(this);
        resetTxt.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.passwordPgeLoginBtn) {
            loginUser();
        } else if (id == R.id.passwordPgeResetTxt) {
            processForgotPassword();
        }
    }

    private void processForgotPassword() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.sendPasswordResetEmail(mailID)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        String message = "Link to reset password has been sent to your V.I.T mail ID";
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Password Reset Link Sent")
                                .setMessage(message).show();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        String message = Objects.requireNonNull(task.getException()).getMessage();
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Something went wrong :( !")
                                .setMessage(message).show();
                    }
                });
    }

    private void loginUser() {

        if (password == null) {
            errorTxt.setText("register numb er is required !");
            return;
        }

        if (password.length() < 10) {
            errorTxt.setText("Minimum 10 Character");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth
                .signInWithEmailAndPassword(mailID, password.trim())
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()) {

                            firebaseUser = firebaseAuth.getCurrentUser();
                            assert firebaseUser != null;

                            final String currentUserMailId = firebaseUser.getEmail();
                            final String currentUserId = firebaseUser.getUid();

                            assert currentUserMailId != null;


                            accountSection.document(Path.STUDENTS.getPath())
                                    .collection(Path.FILES.getPath())
                                    .document(currentUserId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {

                                                User user = User.getInstance();

                                                DocumentSnapshot documentSnapshot = task.getResult();

                                                final String user_UID = Objects.requireNonNull(documentSnapshot.get("user_UID")).toString();
                                                final String applicationID = Objects.requireNonNull(documentSnapshot.get("applicationID")).toString();
                                                final String studentMailId = Objects.requireNonNull(documentSnapshot.get("studentMailId")).toString();
                                                final String studentRegisterNumber = Objects.requireNonNull(documentSnapshot.get("studentRegisterNumber")).toString();
                                                final String studentName = Objects.requireNonNull(documentSnapshot.get("studentName")).toString();
                                                final String studentContactNumber = Objects.requireNonNull(documentSnapshot.get("studentContactNumber")).toString();
                                                final String fcmToken = Objects.requireNonNull(documentSnapshot.get("fcmToken")).toString();

                                                user.setUser_UID(user_UID);
                                                user.setApplicationID(applicationID);
                                                user.setStudentMailId(studentMailId);
                                                user.setStudentRegisterNumber(studentRegisterNumber);
                                                user.setStudentName(studentName);
                                                user.setStudentContactNumber(studentContactNumber);
                                                user.setFcmToken(fcmToken);

                                                localSqlDatabase = new LocalSqlDatabase(getContext(), PasswordFragment.this);
                                                localSqlDatabase.addUserInBackground(user);
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                errorTxt.setText("User Record Not Found");
                                            }
                                        }
                                    });

                        } else {
                            // prompt to verify mail
                            progressBar.setVisibility(View.GONE);
                            promptVerifyMail();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        errorTxt.setText(Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()));
                    }
                });
    }

    // display on unverified account login
    private void promptVerifyMail() {
        progressBar.setVisibility(View.INVISIBLE);
        //Display an alert dialog for  not verifying the mail ID
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(
                requireContext());
        materialAlertDialogBuilder.setTitle("User Not Verified");
        materialAlertDialogBuilder.setMessage("Verify the link sent to your V.I.T mail during registration");
        materialAlertDialogBuilder.setPositiveButton("Resend Verification Link", (dialog, which) -> {
            //second onComplete listener
            Objects.requireNonNull(firebaseAuth.getCurrentUser())
                    .sendEmailVerification()
                    .addOnCompleteListener(task1 -> {

                        //goTo login activity on successful email verification
                        if (task1.isSuccessful()) {
                            //create intent for going to login activity
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Verification Link has been sent")
                                    .setMessage("Please check your V.I.T mail for verification, and then login again")
                                    .setPositiveButton("ok", null)
                                    .setCancelable(false)
                                    .show();
                        } else {
                            errorTxt.setText(Objects.requireNonNull(task1.getException()).getLocalizedMessage());
                        }
                    });
        });
        materialAlertDialogBuilder.setNegativeButton("Ok", (dialog, which) -> {

        });
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.show();
    }

    @Override
    public void onTokenGenerated(String token) {
        //FCM_Tokens/UserId/-> token : value
        //-> lastUpdated : timestamp
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("lastUpdated", new Timestamp(new Date()));
        tokenSection.document(User.getInstance().getUser_UID()).set(tokenMap).addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                progressBar.setVisibility(View.INVISIBLE);
                callSnackBar("Try logging in again after sometime");
                logoutUser();
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                onFCMTokenGenerated.onTokenPushed();
            }

        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.INVISIBLE);
            callSnackBar("Try logging in again after sometime");
            logoutUser();
        });
    }

    @Override
    public void onTokenPushed() {
        AppNotification.getInstance().subscribeAllTopics();
        Intent intent = new Intent(getContext(), HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        requireActivity().finish();
    }

    @Override
    public void notifyUserUpdated() {

    }

    @Override
    public void notifyUserAdded() {
        uploadToken();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        localSqlDatabase.deleteCurrentUser();
    }

    // generating the token
    private void uploadToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                progressBar.setVisibility(View.INVISIBLE);
                callSnackBar(Objects.requireNonNull(task.getException()).getMessage());
                logoutUser();
            } else {
                onFCMTokenGenerated.onTokenGenerated(task.getResult());
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.INVISIBLE);
            callSnackBar(e.getMessage());
            logoutUser();
        });
    }

    // snack bar method
    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(requireContext(), rootView.findViewById(R.id.passwordFragment), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_100));
        snackbar.show();
    }


    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}