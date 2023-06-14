package com.anubhav.hardcoverhaven.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.adapters.DisplayBookListAdapter;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.interfaces.iOnSearchResultClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class BookListsActivity extends AppCompatActivity implements iOnSearchResultClicked {


    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference booksSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.BOOKS.getPath());

    private RecyclerView recyclerView;
    private ImageButton backArrow;
    private MaterialTextView searchForTxt;
    private DisplayBookListAdapter displayBookListAdapter;
    private ArrayList<Books> allBooks;

    private String queryWord;
    private int TAG = 1;
    private boolean FULL = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_lists);

        queryWord = getIntent().getStringExtra("QUERY");
        TAG = getIntent().getIntExtra("SEARCH_TAG", 1);
        FULL = getIntent().getBooleanExtra("FULL", false);

        recyclerView = findViewById(R.id.bookListsPgeRecyclerView);
        backArrow = findViewById(R.id.bookListsBackArrow);
        searchForTxt = findViewById(R.id.bookListsPgeToolbarTitleTxt);

        searchForTxt.setText(queryWord);

        allBooks = new ArrayList<>();
        displayBookListAdapter = new DisplayBookListAdapter(allBooks, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(displayBookListAdapter);

        if (FULL) {
            processFullQuery(queryWord);
        } else {
            processTypeQuery(queryWord);
        }

        backArrow.setOnClickListener(v -> onBackPressed());

    }

    private void processFullQuery(String word) {
        if (word.isEmpty())
            return;

        word = word.trim();
        switch (TAG) {
            case 1:
                queryFullBookTitle(word);
                break;
            case 2:
                queryFullBookISBN(word);
                break;
            case 3:
                queryFullBookSubject(word);
                break;
            case 4:
                queryFullBookAuthor(word);
                break;
        }
    }

    private void queryFullBookAuthor(String word) {
        Query query = booksSection.whereEqualTo("author", word);
        downloadBooks(query);
    }

    private void queryFullBookSubject(String word) {

    }

    private void queryFullBookISBN(String word) {
        Query query = booksSection.whereEqualTo("bookDocID", word);
        downloadBooks(query);
    }

    private void queryFullBookTitle(String word) {
        Query query = booksSection.whereEqualTo("title", word);
        downloadBooks(query);
    }

    private void processTypeQuery(String word) {
        if (word.isEmpty())
            return;

        word = word.trim();
        switch (TAG) {
            case 1:
                queryTypeBookTitle(word);
                break;
            case 2:
                queryTypeBookISBN(word);
                break;
            case 3:
                queryTypeBookSubject(word);
                break;
            case 4:
                queryTypeBookAuthor(word);
                break;
        }
    }

    private void queryTypeBookAuthor(String word) {
        String lower = word;
        String upper = Character.toString((char) (word.charAt(0) + 1));
        Query query = booksSection.whereGreaterThanOrEqualTo("author", lower).whereLessThanOrEqualTo("author", upper);
        downloadBooks(query);
    }

    private void queryTypeBookSubject(String word) {

    }

    private void queryTypeBookISBN(String word) {
        String lower = word;
        String upper = Character.toString((char) (word.charAt(0) + 1));
        Query query = booksSection.whereGreaterThanOrEqualTo("bookDocID", lower).whereLessThanOrEqualTo("bookDocID", upper);
        downloadBooks(query);
    }

    private void queryTypeBookTitle(String word) {
        String lower = word;
        String upper = Character.toString((char) (word.charAt(0) + 1));
        Query query = booksSection.whereGreaterThanOrEqualTo("title", lower).whereLessThanOrEqualTo("title", upper);
        downloadBooks(query);
    }

    private void downloadBooks(Query query) {
        allBooks.clear();
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshots = task.getResult();
                if (snapshots.isEmpty()) {
                    //no results
                    callSnackBar("No books matched your search request!");
                } else {
                    for (DocumentSnapshot snapshot : snapshots) {
                        Books books = snapshot.toObject(Books.class);
                        allBooks.add(books);
                        displayBookListAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                callSnackBar(Objects.requireNonNull(task.getException()).getLocalizedMessage());
            }
        });
    }

    // snack bar method
    private void callSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(BookListsActivity.this, findViewById(R.id.bookListsActivity), message, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_100));
        snackbar.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int resultCode = 60;
        Intent intent = new Intent();
        setResult(resultCode, intent);
        finish();
    }


    @Override
    public void onSearchResultClicked(String bookID) {
        Intent intent = new Intent(BookListsActivity.this, ShowBookActivity.class);
        intent.putExtra("BOOK_DOC_ID", bookID);
        intent.putExtra("TAG", "BOOK_LIST_ACTIVITY");
        startActivity(intent);
    }

    @Override
    public void onSearchResultViewMoreClicked(Intent intent) {

    }
}