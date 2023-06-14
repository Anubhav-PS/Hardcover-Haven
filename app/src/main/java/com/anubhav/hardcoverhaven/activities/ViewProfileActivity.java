package com.anubhav.hardcoverhaven.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.database.LocalSqlDatabase;
import com.anubhav.hardcoverhaven.enums.ErrorCode;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.interfaces.iOnAccountDeleted;
import com.anubhav.hardcoverhaven.interfaces.iOnNotifyDbProcess;
import com.anubhav.hardcoverhaven.models.AlertDisplay;
import com.anubhav.hardcoverhaven.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewProfileActivity extends AppCompatActivity implements View.OnClickListener, iOnNotifyDbProcess{

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference accountSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.ACCOUNTS.getPath()).document(Path.STUDENTS.getPath()).collection(Path.FILES.getPath());

    private ImageButton backArrow;
    private MaterialTextView userTypeTxt, usernameMiddleTxt, usernameTxt, applicationIDTxt, registerNumberTxt, contactNumberTxt, mailIDTxt, deleteAccountTxt;
    private MaterialButton deleteAccountBtn;
    private ProgressBar progressBar;
    private String studentName;
    private String applicationID;
    private String registerNumber;
    private String contactNumber;
    private String studentMailID;
    private Dialog dialog;
    private boolean student, faculty, developer;
    private String userType = "default";

    // firebase declaration
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    //local database
    private LocalSqlDatabase localSqlDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        //firebase instantiation
        firebaseAuth = FirebaseAuth.getInstance();
        //firebase authState listener definition
        authStateListener = firebaseAuth -> firebaseUser = firebaseAuth.getCurrentUser();
        firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        firebaseAuth.getCurrentUser().getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Send token to your backend via HTTPS
                        // ...
                        System.out.println(task.getResult().getClaims());
                        Map<String, Object> map = task.getResult().getClaims();
                        developer = (boolean) map.get("developer");
                        student = (boolean) map.get("student");
                        faculty = (boolean) map.get("faculty");

                        if (student) userType = "Student";
                        if (faculty) userType = "Faculty";
                        if (developer) userType = "Developer";

                        userTypeTxt.setText(userType);
                    }
                });


        userTypeTxt = findViewById(R.id.viewProfilePgeUserTypeTxt);
        usernameMiddleTxt = findViewById(R.id.viewProfilePgeUserNameMiddleTxt);
        usernameTxt = findViewById(R.id.viewProfilePgeUsernameTxt);
        applicationIDTxt = findViewById(R.id.viewProfilePgeApplicationIDTxt);
        registerNumberTxt = findViewById(R.id.viewProfilePgeRegisterNumberTxt);
        contactNumberTxt = findViewById(R.id.viewProfilePgeContactNumberTxt);
        mailIDTxt = findViewById(R.id.viewProfilePgeEmailIDTxt);
        deleteAccountBtn = findViewById(R.id.viewProfilePgeDeleteBtn);
        progressBar = findViewById(R.id.viewProfilePgeProgressBar);
        progressBar.setVisibility(View.GONE);

        backArrow = findViewById(R.id.viewProfileBackArrow);
        backArrow.setOnClickListener(this);

        deleteAccountBtn.setOnClickListener(this);

        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        localSqlDatabase = new LocalSqlDatabase(ViewProfileActivity.this);

        firebaseUser = firebaseAuth.getCurrentUser();


        if (User.getInstance() != null) {
            studentName = User.getInstance().getStudentName();
            applicationID = User.getInstance().getApplicationID();
            registerNumber = User.getInstance().getStudentRegisterNumber();
            contactNumber = User.getInstance().getStudentContactNumber();
            studentMailID = User.getInstance().getStudentMailId();

            usernameMiddleTxt.setText(studentName);
            usernameTxt.setText(studentName);
            applicationIDTxt.setText(applicationID);
            registerNumberTxt.setText(registerNumber);
            contactNumberTxt.setText(contactNumber);
            mailIDTxt.setText(studentMailID);
        }

        contactNumberTxt.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.viewProfileBackArrow) {
            Intent intent = new Intent();
            setResult(54, intent);
            finish();
        } else if (id == R.id.viewProfilePgeContactNumberTxt) {
            openEditContactNumberView();
        } else if (id == R.id.editContactNumberViewCancelBtn) {
            dialog.dismiss();
        } else if (id == R.id.viewProfilePgeDeleteBtn) {
            displayDeleteDialog();
        }
    }

    private String generateCaptcha() {
        final int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final int min = Calendar.getInstance().get(Calendar.MINUTE);
        Random r = new Random();
        char c1 = (char) (r.nextInt(25) + 'a');

        char c2 = (char) (r.nextInt(25) + 'a');
        return String.valueOf(hr) + c1 + min + c2;
    }

    private void displayDeleteDialog() {
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_account);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialTextView declare = dialog.findViewById(R.id.dialogDeleteAccountAcceptTxt);
        MaterialTextView captchaTxt = dialog.findViewById(R.id.dialogDeleteCaptchaTxt);
        TextInputEditText captchaInputEt = dialog.findViewById(R.id.dialogDeleteCaptchaEt);
        MaterialButton proceedBtn = dialog.findViewById(R.id.dialogDeleteAccountDeleteBtn);

        proceedBtn.setEnabled(false);

        final String genCaptcha = generateCaptcha();
        captchaTxt.setText(genCaptcha);

        captchaInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(captchaInputEt.getText()).toString().equals(genCaptcha)) {
                    proceedBtn.setEnabled(true);
                    captchaInputEt.setTextColor(Color.parseColor("#FFFF4444"));
                    declare.setVisibility(View.VISIBLE);
                } else {
                    proceedBtn.setEnabled(false);
                    captchaInputEt.setTextColor(Color.parseColor("#E6626161"));
                    declare.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        proceedBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
            processDeleteAccount();
        });

        dialog.show();
    }

    private void processDeleteAccount() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                } else {
                }
                LocalSqlDatabase localSqlDatabase = new LocalSqlDatabase(ViewProfileActivity.this);
                localSqlDatabase.deleteCurrentUser();
                Intent intent = new Intent(this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                finishAffinity();

            }).addOnFailureListener(e -> {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }

    }

    private void updateProfile() {
        accountSection.document(User.getInstance().getUser_UID())
                .update("studentContactNumber", contactNumber)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        contactNumberTxt.setText(contactNumber);
                        callSnackBar("Successfully Updated");
                        progressBar.setVisibility(View.GONE);
                        User.getInstance().setStudentContactNumber(contactNumber);

                        LocalSqlDatabase localSqlDatabase = new LocalSqlDatabase(ViewProfileActivity.this, ViewProfileActivity.this);
                        localSqlDatabase.updateUserInBackground(User.getInstance());
                    } else {
                        progressBar.setVisibility(View.GONE);

                        AlertDisplay alertDisplay = new AlertDisplay(ErrorCode.EPF001.getErrorCode(), ErrorCode.EPF001.getErrorMessage(), this);
                        alertDisplay.displayAlert();
                    }
                }).addOnFailureListener(e -> callSnackBar("Couldn't perform the update,Please try again after sometime"));

    }


    //process 0 and process 1 functions
    @Override
    public void onStart() {

        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        firebaseUser = firebaseAuth.getCurrentUser();

        if (User.getInstance() == null) {
            User user = null;
            user = localSqlDatabase.getCurrentUser();
        }

    }

    private void openEditContactNumberView() {
        dialog.setContentView(R.layout.edit_contact_number_view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EditText contactNumberEt = dialog.findViewById(R.id.editContactNumberViewEt);
        ImageButton cancelBtn = dialog.findViewById(R.id.editContactNumberViewCancelBtn);
        ImageButton saveBtn = dialog.findViewById(R.id.editContactNumberViewSaveBtn);

        saveBtn.setOnClickListener(v -> {
            final String contactNumber = contactNumberEt.getText().toString().trim();

            if (TextUtils.isEmpty(contactNumber)) {
                contactNumberEt.setError("Contact number is required");
                return;
            }

            final String pattern = "[+][0-9]{11,14}";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(contactNumber);

            if (!m.matches()) {
                contactNumberEt.setError("Invalid or Incorrect Number");
                return;
            }
            dialog.dismiss();
            setTextValue(contactNumber);
            updateProfile();
        });
        cancelBtn.setOnClickListener(this);
        dialog.show();
    }

    private void setTextValue(String value) {
        this.contactNumber = value;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void notifyUserUpdated() {
        if (!LocalSqlDatabase.getExecutors().isTerminated()) {
            LocalSqlDatabase.stopExecutors();
        }
    }

    @Override
    public void notifyUserAdded() {

    }

    // snack bar method
    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(ViewProfileActivity.this, findViewById(R.id.viewProfileActivity), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(ViewProfileActivity.this, R.color.yellow_100));
        snackbar.show();
    }

}