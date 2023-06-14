package com.anubhav.hardcoverhaven.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.database.LocalSqlDatabase;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.fragments.HomePageFragment;
import com.anubhav.hardcoverhaven.fragments.SearchFragment;
import com.anubhav.hardcoverhaven.interfaces.iOnAccountDeleted;
import com.anubhav.hardcoverhaven.interfaces.iOnFeatureItemClicked;
import com.anubhav.hardcoverhaven.interfaces.iOnSearchResultClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.anubhav.hardcoverhaven.models.User;
import com.anubhav.hardcoverhaven.notifications.AppNotification;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener, iOnFeatureItemClicked, iOnSearchResultClicked{


    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference tokenSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.FCM_TOKEN.getPath());
    // firebase declaration
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    //local database
    private LocalSqlDatabase localSqlDatabase;
    private Dialog dialog;
    private MaterialCardView manageAccountCardView;
    private MaterialTextView toolbarTitle;
    private BottomNavigationView bottomNavigationView;
    // callback to Home Activity and its child fragment destruction
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult result) {
           /* if (result.getResultCode() == 50) {     //call from show book activity
                bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
            } else */
            if (result.getResultCode() == 52) { //call from should try activity
                bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
            } else if (result.getResultCode() == 54) { //call from view profile activity
                bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
            } else if (result.getResultCode() == 56) { //call from new release list activity result
                bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);
            } else if (result.getResultCode() == 58) { //call from search result
                bottomNavigationView.setSelectedItemId(R.id.nav_menu_search);
            } else if (result.getResultCode() == 60) { //call from book list activity
                bottomNavigationView.setSelectedItemId(R.id.nav_menu_search);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //firebase instantiation
        firebaseAuth = FirebaseAuth.getInstance();
        //firebase authState listener definition
        authStateListener = firebaseAuth -> user = firebaseAuth.getCurrentUser();

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        // placing the room fragment on initial entry

        toolbarTitle = findViewById(R.id.homePageTitleTxt);


        if (savedInstanceState == null) {
            toolbarTitle.setText("hardcover haven");
            HomePageFragment homePageFragment = new HomePageFragment();
            makeTransaction(homePageFragment);
        }

        bottomNavigationView = findViewById(R.id.homePageBottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu_home);


        bottomNavigationViewSetup();

        localSqlDatabase = new LocalSqlDatabase(HomePageActivity.this);

        dialog = new Dialog(this);

        manageAccountCardView = findViewById(R.id.homePageAccountCardView);
        manageAccountCardView.setOnClickListener(this);

       /* //Initialize PagedList Configuration
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        //Initialize FirebasePagingOptions
        DatabasePagingOptions<Post> options = new DatabasePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(mDatabase, config, Post.class)
                .build();
*/
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.homePageAccountCardView) {
            openManageAccountDialog();
        } else if (id == R.id.dialogAccountSignOutBtn) {
            if (dialog != null) dialog.dismiss();
            processSignOut();
        } else if (id == R.id.dialogAccountShareAppTxt) {
            openShareDialog();
        } else if (id == R.id.dialogAccountViewProfileTxt) {
            openViewProfile();
        }

    }


    // bottom navigation setup process
    private void bottomNavigationViewSetup() {

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_menu_home) {
                toolbarTitle.setText("hardcover haven");
                HomePageFragment homePageFragment = new HomePageFragment();
                makeTransaction(homePageFragment);
            } else if (id == R.id.nav_menu_search) {
                toolbarTitle.setText("search");
                SearchFragment searchFragment = new SearchFragment();
                makeTransaction(searchFragment);
            }
            return true;
        });

        bottomNavigationView.setOnItemReselectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_menu_home) {
                HomePageFragment homePageFragment = new HomePageFragment();
                makeTransaction(homePageFragment);
            } else if (id == R.id.nav_menu_search) {
                SearchFragment searchFragment = new SearchFragment();
                makeTransaction(searchFragment);
            }
        });

    }

    private void openManageAccountDialog() {

        dialog.setContentView(R.layout.dialog_manage_account);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
        dialog.setCanceledOnTouchOutside(true);

        MaterialTextView studentNameTxt, studentMailIdTxt, viewProfileTxt, overDueFineTxt, shareAppTxt, aboutTheAppTxt, sendFeedbackTxt, versionTxt;
        MaterialButton signOutTxt = dialog.findViewById(R.id.dialogAccountSignOutBtn);
        ImageView closeBtn = dialog.findViewById(R.id.dialogAccountCloseBtn);

        studentNameTxt = dialog.findViewById(R.id.dialogAccountStudentNameTxt);
        studentMailIdTxt = dialog.findViewById(R.id.dialogAccountStudentMailIdTxt);

        viewProfileTxt = dialog.findViewById(R.id.dialogAccountViewProfileTxt);
        overDueFineTxt = dialog.findViewById(R.id.dialogAccountOverDueFineTxt);
        shareAppTxt = dialog.findViewById(R.id.dialogAccountShareAppTxt);
        aboutTheAppTxt = dialog.findViewById(R.id.dialogAccountAboutAppTxt);
        sendFeedbackTxt = dialog.findViewById(R.id.dialogAccountSendFeedbackTxt);
        versionTxt = dialog.findViewById(R.id.dialogAccountAppVersionTxt);


        studentNameTxt.setText(User.getInstance().getStudentName());
        studentMailIdTxt.setText(User.getInstance().getStudentMailId());

        String versionName = "version " + getVersionCode();
        versionTxt.setText(versionName);

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        signOutTxt.setOnClickListener(this);

        viewProfileTxt.setOnClickListener(this);
        overDueFineTxt.setOnClickListener(this);
        shareAppTxt.setOnClickListener(this);
        aboutTheAppTxt.setOnClickListener(this);
        sendFeedbackTxt.setOnClickListener(this);

        dialog.show();
    }

    public String getVersionCode() {
        String v = "";
        try {
            v = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            return v;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(HomePageActivity.this, "Failed to fetch Version Detail", Toast.LENGTH_SHORT).show();
        }
        return v;
    }


    //method to make the fragment transaction
    public void makeTransaction(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.homePageFragmentContainer, fragment);
        fragmentTransaction.commit();
    }


    // method to handle the sign out process
    private void processSignOut() {
        AppNotification.getInstance().unSubscribeAllTopics();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tokenSection.document(User.getInstance().getUser_UID()).delete();
        }

        FirebaseAuth.getInstance().signOut();
        LocalSqlDatabase localSqlDatabase = new LocalSqlDatabase(HomePageActivity.this);
        localSqlDatabase.deleteCurrentUser();

        Toast.makeText(HomePageActivity.this, "Logging out, see you soon !", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomePageActivity.this, SignInActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        finish();
    }

    // snack bar method
    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(HomePageActivity.this, findViewById(R.id.homePage), message, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(HomePageActivity.this, R.color.blue_100));
        snackbar.show();
    }

    private void openShareDialog() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hardcover Haven");
        String shareMessage = "\nHey dude, check out this app for library\n\n";
        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + this.getPackageName() + "\n";
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(intent, "Select One"));
    }

    private void openViewProfile() {
        Intent intent = new Intent(HomePageActivity.this, ViewProfileActivity.class);
        activityResultLauncher.launch(intent);
    }


    //process 0 and process 1 functions
    @Override
    public void onStart() {

        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        user = firebaseAuth.getCurrentUser();

        if (User.getInstance() == null) {
            User user = null;
            user = localSqlDatabase.getCurrentUser();
        }

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
    public void onNewReleaseClicked(String bookDocId) {
        Intent intent = new Intent(HomePageActivity.this, ShowBookActivity.class);
        intent.putExtra("BOOK_DOC_ID", bookDocId);
        intent.putExtra("TAG", "HOME_FRAGMENT");
        activityResultLauncher.launch(intent);

    }

    @Override
    public void onWaitListClicked(String bookDocId) {

    }

    @Override
    public void onShouldTryClicked(String bookDocId) {
        Intent intent = new Intent(HomePageActivity.this, ShouldTryActivity.class);
        intent.putExtra("BOOK_DOC_ID", bookDocId);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onCurrentReadsClicked(Books books) {

    }

    @Override
    public void onNewReleaseLinearLayoutClicked() {
        Intent intent = new Intent(HomePageActivity.this, NewReleaseActivity.class);
        intent.putExtra("TAG", "HOME_FRAGMENT");
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onSearchResultClicked(String bookID) {
        Intent intent = new Intent(HomePageActivity.this, ShowBookActivity.class);
        intent.putExtra("BOOK_DOC_ID", bookID);
        intent.putExtra("TAG", "SEARCH_FRAGMENT");
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onSearchResultViewMoreClicked(Intent intent) {
        activityResultLauncher.launch(intent);
    }

}