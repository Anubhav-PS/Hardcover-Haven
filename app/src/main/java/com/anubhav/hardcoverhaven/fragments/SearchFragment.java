package com.anubhav.hardcoverhaven.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.activities.BookListsActivity;
import com.anubhav.hardcoverhaven.adapters.DisplayBookListAdapter;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.interfaces.iOnSearchResultClicked;
import com.anubhav.hardcoverhaven.models.Books;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;


public class SearchFragment extends Fragment implements iOnSearchResultClicked {

    private static int TAG = 1;
    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference booksSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.BOOKS.getPath());
    private View rootView;
    private MaterialToolbar toolBar;
    private MaterialTextView tagWarningTxt, viewAllTxt;
    private RecyclerView recyclerView;
    private DisplayBookListAdapter displayBookListAdapter;


    private ArrayList<Books> allBooks;
    private String queryWord = "";
    private boolean fullSearch = false;
    private iOnSearchResultClicked onSearchResultClicked;

    public SearchFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ChipGroup chipGroup = rootView.findViewById(R.id.searchPgeChipGrp);
        tagWarningTxt = rootView.findViewById(R.id.searchPgeTagWarning);
        tagWarningTxt.setVisibility(View.INVISIBLE);
        viewAllTxt = rootView.findViewById(R.id.searchPgeViewAllTxt);
        viewAllTxt.setVisibility(View.INVISIBLE);

        recyclerView = rootView.findViewById(R.id.searchPgeRecyclerView);

        allBooks = new ArrayList<>();
        displayBookListAdapter = new DisplayBookListAdapter(allBooks, getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(displayBookListAdapter);


        TAG = 1;
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                tagWarningTxt.setVisibility(View.INVISIBLE);
                int id = checkedIds.get(0);
                if (id == R.id.searchPgeChipBookTitle) {
                    TAG = 1;
                } else if (id == R.id.searchPgeChipIsbn) {
                    TAG = 2;
                } else if (id == R.id.searchPgeChipBookSubject) {
                    TAG = 3;
                } else if (id == R.id.searchPgeChipAuthor) {
                    TAG = 4;
                }
            } else {
                TAG = 5;
                tagWarningTxt.setVisibility(View.VISIBLE);
            }
        });

        toolBar = rootView.findViewById(R.id.searchPgeToolbar);
        toolBar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            SearchView searchView = (SearchView) item.getActionView();
            searchView.setQueryHint("Search Books");
            //searchView.setPadding(0, 4, 16, 4);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (TAG != 5) {
                        processFullQuery(query);
                        fullSearch = true;
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (TAG != 5) {
                        processTypeQuery(newText);
                        fullSearch = false;
                    }
                    return false;
                }
            });

            searchView.setOnCloseListener(() -> {
                if (displayBookListAdapter != null) {
                    allBooks.clear();
                    recyclerView.setAdapter(null);
                }
                return false;
            });
            return false;
        });

        viewAllTxt.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), BookListsActivity.class);
            intent.putExtra("QUERY", queryWord);
            intent.putExtra("SEARCH_TAG", TAG);
            intent.putExtra("FULL",fullSearch);
            onSearchResultClicked.onSearchResultViewMoreClicked(intent);
        });
        return rootView;
    }


    private void processFullQuery(String word) {
        if (word.isEmpty())
            return;

        word = word.trim();
        queryWord = word;
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
        queryWord = word;
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
                    if (snapshots.size() > 4) {
                        viewAllTxt.setVisibility(View.VISIBLE);
                    } else {
                        viewAllTxt.setVisibility(View.INVISIBLE);
                    }
                    int id = 0;
                    for (DocumentSnapshot snapshot : snapshots) {
                        if (++id > 4) break;
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
                .make(requireContext(), rootView.findViewById(R.id.searchFragment), message, Snackbar.LENGTH_SHORT);
        snackbar.setTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue_100));
        snackbar.show();
    }


    @Override
    public void onSearchResultClicked(String bookID) {
        onSearchResultClicked.onSearchResultClicked(bookID);
    }

    @Override
    public void onSearchResultViewMoreClicked(Intent intent) {
        onSearchResultClicked.onSearchResultViewMoreClicked(intent);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            this.onSearchResultClicked = (iOnSearchResultClicked) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + "is not implementing iOnSearchResultClicked");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.onSearchResultClicked != null) {
            this.onSearchResultClicked = null;
        }

    }
}