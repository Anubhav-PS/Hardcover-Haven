package com.anubhav.hardcoverhaven.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.database.LocalSqlDatabase;
import com.anubhav.hardcoverhaven.interfaces.iOnStartActivityCheckDone;
import com.anubhav.hardcoverhaven.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity implements iOnStartActivityCheckDone {


    //firebase auth declarations
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;


    //listeners
    iOnStartActivityCheckDone onStartActivityCheckDone;

    //local database
    private LocalSqlDatabase localSqlDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        localSqlDatabase = new LocalSqlDatabase(SplashScreenActivity.this);

        onStartActivityCheckDone = this;

        //firebase auth instantiation
        firebaseAuth = FirebaseAuth.getInstance();

        //firebase authState listener definition
        authStateListener = firebaseAuth -> firebaseUser = firebaseAuth.getCurrentUser();

        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            User user = User.getInstance();
            user = localSqlDatabase.getCurrentUser();
            onStartActivityCheckDone.initiatePageChange(true);
        } else if (firebaseUser == null) {
            localSqlDatabase.deleteCurrentUser();
            onStartActivityCheckDone.initiatePageChange(false);
        } else {
            firebaseAuth.signOut();
            onStartActivityCheckDone.initiatePageChange(false);
        }


    }


    @Override
    public void initiatePageChange(boolean proceed) {
        int SPLASH_SCREEN = 1200;

        new Handler().postDelayed(() -> {
            Intent intent;
            if (proceed) {
                intent = new Intent(SplashScreenActivity.this, HomePageActivity.class);
            } else {
                intent = new Intent(SplashScreenActivity.this, SignUpActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            finish();
        }, SPLASH_SCREEN);

    }

    //process 0 and 1
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (User.getInstance() == null) {
            User user = null;
            user = localSqlDatabase.getCurrentUser();
        }
    }
}