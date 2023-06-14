package com.anubhav.hardcoverhaven.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.fragments.RegisterNumberFragment;

public class SignInActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (savedInstanceState == null) {
            RegisterNumberFragment fragment = new RegisterNumberFragment();
            makeTransaction(fragment);
        }

    }


    //method to make the fragment transaction
    public void makeTransaction(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.signInPgeFrameLayout, fragment);
        fragmentTransaction.commit();
    }


}