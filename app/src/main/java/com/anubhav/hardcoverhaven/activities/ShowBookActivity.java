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
import com.anubhav.hardcoverhaven.enums.SHOULD_TRY_TOGGLE;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ShowBookActivity extends AppCompatActivity implements View.OnClickListener, DownloadData.iOnDownloadedDataCallback, UploadData.iOnUploadedDataCallback {

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference stockSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.STOCK.getPath());


    private String bookDocId;

    private String isbn10 = "";
    private String isbn13 = "";
    private String stockMsg = "";
    private MaterialTextView titleTxt, authorTxt, stockTxt, otherAuthorsTxt, subjectTxt, languageTxt, editionTxt, publisherTxt, publishedInTxt, ddsNumberTxt, isbn10Txt, isbn13Txt, saveForLaterTxt, moreTxt, toolbarTitleTxt;
    private ImageView coverImage;
    private ImageButton backArrow;
    private MaterialButton notifyMeBtn;
    private ConstraintLayout constraintLayout;
    private String tag;

    private UploadData uploadData;
    //local database
    private LocalSqlDatabase localSqlDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book);

        bookDocId = getIntent().getStringExtra("BOOK_DOC_ID");
        tag = getIntent().getStringExtra("TAG");

        localSqlDatabase = new LocalSqlDatabase(this);

        titleTxt = findViewById(R.id.showBookTitleTxt);
        toolbarTitleTxt = findViewById(R.id.showBookPgeToolbarTitleTxt);
        authorTxt = findViewById(R.id.showBookAuthorTxt);
        stockTxt = findViewById(R.id.showBookStockTxt);
        moreTxt = findViewById(R.id.showBookPgeMoreTxt);
        constraintLayout = findViewById(R.id.showBookPgeConstraint);
        otherAuthorsTxt = findViewById(R.id.showBookOtherAuthorsTxt);
        subjectTxt = findViewById(R.id.showBookSubjectTxt);
        languageTxt = findViewById(R.id.showBookLanguageTxt);
        editionTxt = findViewById(R.id.showBookEditionTxt);
        publisherTxt = findViewById(R.id.showBookPublisherTxt);
        publishedInTxt = findViewById(R.id.showBookPublishedInTxt);
        ddsNumberTxt = findViewById(R.id.showBookDdsNumberTxt);
        isbn10Txt = findViewById(R.id.showBookISBN10Txt);
        isbn13Txt = findViewById(R.id.showBookISBN13Txt);
        coverImage = findViewById(R.id.showBookCoverImage);
        backArrow = findViewById(R.id.showBookBackArrow);
        saveForLaterTxt = findViewById(R.id.showBookPgeSaveForLaterTxt);
        notifyMeBtn = findViewById(R.id.showBookNotifyMeBtn);

        moreTxt.setOnClickListener(this);
        backArrow.setOnClickListener(this);

        saveForLaterTxt.setText(SHOULD_TRY_TOGGLE.ADD.getTag());
        saveForLaterTxt.setOnClickListener(this);

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

        if (id == R.id.showBookBackArrow) {
            onBackPressed();
        } else if (id == R.id.showBookPgeSaveForLaterTxt) {
            if (saveForLaterTxt.getText().equals(SHOULD_TRY_TOGGLE.ADD.getTag())) {
                processAddToShouldTry();
            } else if (saveForLaterTxt.getText().equals(SHOULD_TRY_TOGGLE.REMOVE.getTag())) {
                processRemoveFromShouldTry();
            }
        } else if (id == R.id.showBookNotifyMeBtn) {
            if (notifyMeBtn.getText().equals(WAIT_LIST_TOGGLE.ADD.getTag())) {
                processAddToWaitList();
            } else if (notifyMeBtn.getText().equals(WAIT_LIST_TOGGLE.REMOVE.getTag())) {
                processRemoveFromWaitList();
            }
        } else if (id == R.id.showBookIsbn10LinearLayout) {
            copyISBN10();
        } else if (id == R.id.showBookIsbn13LinearLayout) {
            copyISBN13();
        } else if (id == R.id.showBookPgeMoreTxt) {
            processShowMore();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int resultCode = 50;
        Intent intent = new Intent();
        if (tag.equalsIgnoreCase("SEARCH_FRAGMENT")) {
            resultCode = 58;
            setResult(resultCode, intent);
            finish();
        } else if (tag.equalsIgnoreCase("NEW_RELEASE_ACTIVITY")) {
            resultCode = 56;
            setResult(resultCode, intent);
            finish();
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

    private void processAddToShouldTry() {
        uploadData.uploadDataToAddToShouldTry(bookDocId);
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
                .make(ShowBookActivity.this, findViewById(R.id.showBookActivity), message, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(ShowBookActivity.this, R.color.blue_100));
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
        callSnackBar("Added To Should Try");
        saveForLaterTxt.setText(SHOULD_TRY_TOGGLE.REMOVE.getTag());
    }

    @Override
    public void onUploadedToRemoveFromShouldTry() {
        callSnackBar("Removed From Should Try");
    }

    @Override
    public void onUploadFailed(String message) {
        callSnackBar(message);
    }
}