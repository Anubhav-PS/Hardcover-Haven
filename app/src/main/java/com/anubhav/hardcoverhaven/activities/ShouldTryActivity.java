package com.anubhav.hardcoverhaven.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.database.LocalSqlDatabase;
import com.anubhav.hardcoverhaven.enums.MORE_TOGGLE;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.enums.WAIT_LIST_TOGGLE;
import com.anubhav.hardcoverhaven.models.Books;
import com.anubhav.hardcoverhaven.models.Genre;
import com.anubhav.hardcoverhaven.models.ISBN;
import com.anubhav.hardcoverhaven.models.Stock;
import com.anubhav.hardcoverhaven.models.User;
import com.anubhav.hardcoverhaven.services.DownloadData;
import com.anubhav.hardcoverhaven.services.UploadData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShouldTryActivity extends AppCompatActivity implements View.OnClickListener, DownloadData.iOnDownloadedDataCallback , UploadData.iOnUploadedDataCallback {

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference stockSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.STOCK.getPath());

    private String bookDocId;

    private String isbn10 = "";
    private String isbn13 = "";
    private String stockMsg = "";
    private MaterialTextView titleTxt, authorTxt, stockTxt, otherAuthorsTxt, subjectTxt, languageTxt, editionTxt, publisherTxt, publishedInTxt, ddsNumberTxt, isbn10Txt, isbn13Txt, moreTxt, toolbarTitleTxt, removeFromShouldTryTxt;
    private ImageView coverImage;
    private ImageButton backArrow;
    private MaterialButton notifyMeBtn;
    private ConstraintLayout constraintLayout;

    private UploadData uploadData;
    //local database
    private LocalSqlDatabase localSqlDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_should_try);

        bookDocId = getIntent().getStringExtra("BOOK_DOC_ID");

        localSqlDatabase = new LocalSqlDatabase(this);

        titleTxt = findViewById(R.id.shouldTryPgeTitleTxt);
        toolbarTitleTxt = findViewById(R.id.shouldTryPgeToolbarTitleTxt);
        authorTxt = findViewById(R.id.shouldTryPgeBookAuthorTxt);
        stockTxt = findViewById(R.id.shouldTryPgeStockTxt);
        moreTxt = findViewById(R.id.shouldTryPgeMoreTxt);
        constraintLayout = findViewById(R.id.shouldTryPgeConstraint);
        otherAuthorsTxt = findViewById(R.id.shouldTryPgeOtherAuthorsTxt);
        subjectTxt = findViewById(R.id.shouldTryPgeSubjectTxt);
        languageTxt = findViewById(R.id.shouldTryPgeLanguageTxt);
        editionTxt = findViewById(R.id.shouldTryPgeEditionTxt);
        publisherTxt = findViewById(R.id.shouldTryPgePublisherTxt);
        publishedInTxt = findViewById(R.id.shouldTryPgePublishedInTxt);
        ddsNumberTxt = findViewById(R.id.shouldTryPgeDdsNumberTxt);
        isbn10Txt = findViewById(R.id.shouldTryPgeISBN10Txt);
        isbn13Txt = findViewById(R.id.shouldTryPgeISBN13Txt);
        coverImage = findViewById(R.id.shouldTryPgeCoverImage);
        backArrow = findViewById(R.id.shouldTryPgeBackArrow);
        removeFromShouldTryTxt = findViewById(R.id.shouldTryPgeRemoveFromShouldTryTxt);
        notifyMeBtn = findViewById(R.id.shouldTryPgeNotifyMeBtn);


        moreTxt.setOnClickListener(this);
        backArrow.setOnClickListener(this);

        removeFromShouldTryTxt.setOnClickListener(this);

        notifyMeBtn.setText(WAIT_LIST_TOGGLE.ADD.getTag());
        notifyMeBtn.setOnClickListener(this);
        notifyMeBtn.setClickable(false);

        DownloadData downloadData = new DownloadData(this);
        downloadData.downloadBookDataFor(bookDocId);
        downloadData.downloadStockDataFor(bookDocId);
        downloadData.downloadGenreDataFor(bookDocId);
        downloadData.downloadIsbnDataFor(bookDocId);

        uploadData = new UploadData(this);


        stockSection.document(bookDocId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Stock stock = documentSnapshot.toObject(Stock.class);
                updateStockValue(stock);
            }
        });
    }

    private void updateStockValue(Stock stock) {
        this.onDownloadedStockData(stock);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.shouldTryPgeBackArrow) {
            Intent intent = new Intent();
            setResult(52, intent);
            finish();
        } else if (id == R.id.shouldTryPgeRemoveFromShouldTryTxt) {
            processRemoveFromShouldTry();
        } else if (id == R.id.shouldTryPgeNotifyMeBtn) {
            if (notifyMeBtn.getText().equals(WAIT_LIST_TOGGLE.ADD.getTag())) {
                processAddToWaitList();
            } else if (notifyMeBtn.getText().equals(WAIT_LIST_TOGGLE.REMOVE.getTag())) {
                processRemoveFromWaitList();
            }
        } else if (id == R.id.shouldTryPgeIsbn10LinearLayout) {
            copyISBN10();
        } else if (id == R.id.shouldTryPgeIsbn13LinearLayout) {
            copyISBN13();
        } else if (id == R.id.shouldTryPgeMoreTxt) {
            processShowMore();
        }

    }

    private void processShowMore() {
        if (moreTxt.getText().equals(MORE_TOGGLE.MORE.getTag())) {
            constraintLayout.setVisibility(View.VISIBLE);
            moreTxt.setText(MORE_TOGGLE.LESS.getTag());
        } else if (moreTxt.getText().equals(MORE_TOGGLE.LESS.getTag())) {
            constraintLayout.setVisibility(View.GONE);
            moreTxt.setText(MORE_TOGGLE.MORE.getTag());
        }
    }

    private void processRemoveFromShouldTry() {
        uploadData.uploadDataToRemoveFromShouldTry(bookDocId);
    }

    private void processAddToWaitList() {
        Map<String, Object> data = new HashMap<>();
        data.put("user_ID", User.getInstance().getUser_UID());
        data.put("bookDocID", bookDocId);
        data.put("process", true);
        uploadData.uploadDataToAddToWaitListRequest(data);
    }

    private void processRemoveFromWaitList() {
        Map<String, Object> data = new HashMap<>();
        data.put("user_ID", User.getInstance().getUser_UID());
        data.put("bookDocID", bookDocId);
        data.put("process", false);
        uploadData.uploadDataToRemoveFromWaitListRequest(data);
    }

    private void copyISBN10() {
        ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ISBN 10", isbn10);
        clipboard.setPrimaryClip(clip);
    }

    private void copyISBN13() {
        ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ISBN 13", isbn13);
        clipboard.setPrimaryClip(clip);
    }

    // snack bar method
    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(ShouldTryActivity.this, findViewById(R.id.shouldTryActivity), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(ShouldTryActivity.this, R.color.blue_100));
        snackbar.show();
    }

    //process 0 and process 1 functions
    @Override
    public void onStart() {
        super.onStart();
        if (User.getInstance() == null) {
            User user = null;
            user = localSqlDatabase.getCurrentUser();
        }
    }

    @Override
    public void onDownloadedBookData(Books book) {
        titleTxt.setText(book.getTitle());
        toolbarTitleTxt.setText(book.getTitle());
        String[] arr = book.getAuthor().split(",");
        if (arr.length > 1) {
            authorTxt.setText(book.getAuthor().split(",", 2)[0]);
            otherAuthorsTxt.setText(book.getAuthor().split(",", 2)[1]);
        } else {
            authorTxt.setText(book.getAuthor());
            otherAuthorsTxt.setText("Only One Author");
        }
        languageTxt.setText(book.getLanguage());
        publisherTxt.setText(book.getPublisher());
        publishedInTxt.setText(String.valueOf(book.getPublishedYear()));
        String editionMessage = book.getEdition() + " Edition";
        editionTxt.setText(editionMessage);

        Picasso.get().load(book.getImgUrl()).into(coverImage);
    }

    @Override
    public void onDownloadedStockData(Stock stock) {
        if (stock.getAvailable() > 0) {
            stockMsg = stock.getAvailable() + " copies";
            stockTxt.setText(stockMsg);
            notifyMeBtn.setClickable(false);
        } else {
            stockTxt.setText("Out Of Stock");
            notifyMeBtn.setClickable(true);
        }
    }

    @Override
    public void onDownloadedGenreData(Genre genre) {
        subjectTxt.setText(genre.getSubject());
        ddsNumberTxt.setText(genre.getDdsNumber());
    }

    @Override
    public void onDownloadedIsbnData(ISBN isbn) {
        isbn10 = isbn.getIsbn10();
        isbn13 = isbn.getIsbn13();
        isbn10Txt.setText(isbn10);
        isbn13Txt.setText(isbn13);
    }

    @Override
    public void onDownloadedAllBookData() {

    }

    @Override
    public void onDownloadFailed(String message) {
        callSnackBar(message);
    }


    @Override
    public void onUploadedToRequestToAddToWaitList() {
        callSnackBar("Added To WaitList");
        notifyMeBtn.setText(WAIT_LIST_TOGGLE.REMOVE.getTag());
    }

    @Override
    public void onUploadedToRequestToRemoveFromWaitList() {
        callSnackBar("Removed From WaitList");
        notifyMeBtn.setText(WAIT_LIST_TOGGLE.ADD.getTag());
    }

    @Override
    public void onUploadedToAddToShouldTry() {

    }

    @Override
    public void onUploadedToRemoveFromShouldTry() {
        callSnackBar("Removed From Should Try");
        Intent intent = new Intent();
        setResult(52, intent);
        finish();
    }

    @Override
    public void onUploadFailed(String message) {
        callSnackBar(message);
    }
}