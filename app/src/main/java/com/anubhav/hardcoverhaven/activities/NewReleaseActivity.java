package com.anubhav.hardcoverhaven.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.adapters.NewReleaseAdapter;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.interfaces.iOnFeatureItemClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class NewReleaseActivity extends AppCompatActivity implements iOnFeatureItemClicked {

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference booksSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.BOOKS.getPath());

    private RecyclerView recyclerView;
    private ImageButton backArrow;
    private NewReleaseAdapter newReleaseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_release);

        recyclerView = findViewById(R.id.newReleasesPgeRecyclerView);
        backArrow = findViewById(R.id.newReleasesBackArrow);

        newReleaseAdapter = new NewReleaseAdapter(queryNewReleases(), this, R.layout.cell_list_book);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(newReleaseAdapter);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(56, intent);
                finish();
            }
        });

    }

    private void downloadNewReleases() {
        queryNewReleases();
    }

    private FirestoreRecyclerOptions<Books> queryNewReleases() {

        Query query = booksSection.orderBy("addedOn");
        return new FirestoreRecyclerOptions.Builder<Books>().setQuery(query, Books.class).build();

    }

    @Override
    public void onNewReleaseClicked(String bookDocId) {
        Intent intent = new Intent(NewReleaseActivity.this, ShowBookActivity.class);
        intent.putExtra("BOOK_DOC_ID", bookDocId);
        intent.putExtra("TAG", "NEW_RELEASE_ACTIVITY");
        startActivity(intent);
    }

    @Override
    public void onWaitListClicked(String bookDocId) {

    }

    @Override
    public void onShouldTryClicked(String bookDocId) {

    }

    @Override
    public void onCurrentReadsClicked(Books bookDocId) {

    }

    @Override
    public void onNewReleaseLinearLayoutClicked() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadNewReleases();
        newReleaseAdapter = new NewReleaseAdapter(queryNewReleases(), this, R.layout.cell_list_book);
        recyclerView.setAdapter(newReleaseAdapter);
        newReleaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        newReleaseAdapter.stopListening();
    }
}