package com.anubhav.hardcoverhaven.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.hardcoverhaven.R;
import com.anubhav.hardcoverhaven.adapters.CurrentReadsAdapter;
import com.anubhav.hardcoverhaven.adapters.NewReleaseAdapter;
import com.anubhav.hardcoverhaven.adapters.ShouldTryAdapter;
import com.anubhav.hardcoverhaven.adapters.WaitListAdapter;
import com.anubhav.hardcoverhaven.database.LocalSqlDatabase;
import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.interfaces.iOnDataDownloaded;
import com.anubhav.hardcoverhaven.interfaces.iOnFeatureItemClicked;
import com.anubhav.hardcoverhaven.models.AccessID;
import com.anubhav.hardcoverhaven.models.Books;
import com.anubhav.hardcoverhaven.models.Genre;
import com.anubhav.hardcoverhaven.models.ISBN;
import com.anubhav.hardcoverhaven.models.Stock;
import com.anubhav.hardcoverhaven.models.User;
import com.anubhav.hardcoverhaven.models.WaitList;
import com.anubhav.hardcoverhaven.services.UploadData;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomePageFragment extends Fragment implements iOnDataDownloaded, iOnFeatureItemClicked, View.OnClickListener, UploadData.iOnUploadedDataCallback {


    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final CollectionReference booksSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.BOOKS.getPath());
    private final CollectionReference stockSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.STOCK.getPath());

    private final CollectionReference waitListSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.WAIT_LIST.getPath());
    private final CollectionReference shouldTrySection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.SHOULD_TRY.getPath());
    private final CollectionReference currentReadsSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.CURRENT_READS.getPath());
    private final CollectionReference accessIDSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.ACCESS_ID.getPath());

    // firebase declaration
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private ArrayList<WaitList> allWaitListBooks;
    private ArrayList<Books> allShouldTryBooks;
    private ArrayList<Books> allcurrentReadsBooks;
    private NewReleaseAdapter newReleaseAdapter;
    private WaitListAdapter waitListAdapter;
    private ShouldTryAdapter shouldTryAdapter;
    private CurrentReadsAdapter currentReadsAdapter;
    private Dialog dialog;
    private RecyclerView newReleaseRecycler, waitListRecycler, shouldTryRecycler, currentReadsRecycler;
    private ProgressBar waitListProgressBar, currentReadsProgressBar;
    private LinearLayout newReleaseLinearLayout;
    private ListenerRegistration waitListListener, shouldTryListener, currentReadsListener;

    private boolean waitList = false, shouldTry = false, currentReads = false;


    //local database
    private LocalSqlDatabase localSqlDatabase;

    private UploadData uploadData;

    private iOnFeatureItemClicked onFeatureMenuClicked;

    private iOnDataDownloaded onDataDownloaded;

    private View view;

    private String bookIdForProcessStatus = "";


    public HomePageFragment() {
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
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        //firebase instantiation
        firebaseAuth = FirebaseAuth.getInstance();
        //firebase authState listener definition
        authStateListener = firebaseAuth -> firebaseUser = firebaseAuth.getCurrentUser();


        localSqlDatabase = new LocalSqlDatabase(getContext());

        newReleaseRecycler = view.findViewById(R.id.homePageNewReleaseRecycler);

        waitListRecycler = view.findViewById(R.id.homePageWaitListRecycler);
        waitListProgressBar = view.findViewById(R.id.homePgeFragmentWaitListProgressBar);

        shouldTryRecycler = view.findViewById(R.id.homePageShouldTryRecycler);

        currentReadsRecycler = view.findViewById(R.id.homePgeCurrentReadsRecyclerView);
        currentReadsProgressBar = view.findViewById(R.id.homePgeFragmentCurrentReadsProgressBar);

        newReleaseLinearLayout = view.findViewById(R.id.homePageNewReleaseLinearLayout);
        newReleaseLinearLayout.setOnClickListener(this);




        onDataDownloaded = this;
        uploadData = new UploadData(this);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allWaitListBooks = new ArrayList<>();
        allShouldTryBooks = new ArrayList<>();
        allcurrentReadsBooks = new ArrayList<>();

        setUpWaitListener();
        setUpShouldTryListener();
        setUpCurrentReadsListener();

        newReleaseAdapter = new NewReleaseAdapter(queryNewReleases(), this, R.layout.cell_front_display_books);
        newReleaseRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        newReleaseRecycler.setHasFixedSize(true);
        newReleaseRecycler.setAdapter(newReleaseAdapter);

        waitListAdapter = new WaitListAdapter(allWaitListBooks, getContext(), this);
        waitListRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        waitListRecycler.setHasFixedSize(true);
        waitListRecycler.setAdapter(waitListAdapter);


        shouldTryAdapter = new ShouldTryAdapter(allShouldTryBooks, getContext(), this);
        shouldTryRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        shouldTryRecycler.setHasFixedSize(true);
        shouldTryRecycler.setAdapter(shouldTryAdapter);

        currentReadsAdapter = new CurrentReadsAdapter(allcurrentReadsBooks, getContext(), this);
        currentReadsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        currentReadsRecycler.setHasFixedSize(true);
        currentReadsRecycler.setAdapter(currentReadsAdapter);

    }

    private void downloadOldQuery() {
    }

    private void downloadNewQuery() {
    }

    private void setUpWaitListener() {
        waitListListener = waitListSection.document(User.getInstance().getUser_UID()).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Handle document change here
                downloadAllWaitListIds();
            } else {

            }
        });
    }

    private void setUpShouldTryListener() {
        shouldTryListener = shouldTrySection.document(User.getInstance().getUser_UID()).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Handle document change here
                downloadAllShouldTryIds();
            } else {

            }
        });
    }

    private void setUpCurrentReadsListener() {
        currentReadsListener = currentReadsSection.document(User.getInstance().getApplicationID()).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Handle document change here
                downloadAllCurrentReadIds();
            } else {

            }
        });
    }

    private void downloadNewReleases() {
        queryNewReleases();
    }

    public void downloadAllWaitListIds() {
        waitListProgressBar.setVisibility(View.VISIBLE);
        waitListSection
                .document(User.getInstance().getUser_UID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allWaitListBooks.clear();
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            List<String> bookIds = (List<String>) documentSnapshot.get("bookDocIDs");
                            onDataDownloaded.onWaitListBookIdsDownloaded(bookIds);
                        } else {
                            //display no books
                            waitListProgressBar.setVisibility(View.INVISIBLE);
                            waitListAdapter.notifyDataSetChanged();
                        }
                    } else {
                        waitListProgressBar.setVisibility(View.INVISIBLE);
                        //display error
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitListProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public void downloadAllShouldTryIds() {
        shouldTrySection
                .document(User.getInstance().getUser_UID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allShouldTryBooks.clear();
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {

                            List<String> bookIds = (List<String>) documentSnapshot.get("bookDocId");
                            onDataDownloaded.onShouldTryBookIdsDownloaded(bookIds);
                        } else {
                            //display no books
                            shouldTryAdapter.notifyDataSetChanged();
                        }
                    } else {
                        //display error
                    }
                });
    }

    private void downloadAllCurrentReadIds() {

        currentReadsSection
                .document(User.getInstance().getApplicationID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        allcurrentReadsBooks.clear();
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            List<String> bookIds = (List<String>) documentSnapshot.get("bookDocIDs");
                            assert bookIds != null;
                            onDataDownloaded.onCurrentReadsBookIdsDownloaded(bookIds);
                        } else {
                            //display no books
                            currentReadsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        //display error
                    }
                });
    }

    private FirestoreRecyclerOptions<Books> queryNewReleases() {

        Query query = booksSection.limit(12).orderBy("addedOn");
        return new FirestoreRecyclerOptions.Builder<Books>().setQuery(query, Books.class).build();

    }

    @Override
    public void onResume() {
        super.onResume();
        downloadNewReleases();
        newReleaseAdapter = new NewReleaseAdapter(queryNewReleases(), this, R.layout.cell_front_display_books);
        newReleaseRecycler.setAdapter(newReleaseAdapter);
        newReleaseAdapter.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        newReleaseAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        newReleaseAdapter.stopListening();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            this.onFeatureMenuClicked = (iOnFeatureItemClicked) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + "is not implementing iOnFeatureMenuClicked");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.onFeatureMenuClicked != null) {
            this.onFeatureMenuClicked = null;
        }

    }

    @Override
    public void onBooksDataDownloaded(Books books) {

    }

    @Override
    public void onStockDataDownloaded(Stock stock) {

    }

    @Override
    public void onISBNDataDownloaded(ISBN isbn) {

    }

    @Override
    public void onGenreDataDownloaded(Genre genre) {

    }

    @Override
    public void onWaitListBookIdsDownloaded(List<String> list) {
        for (String val : list) {
            booksSection.document(val).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Books books = documentSnapshot.toObject(Books.class);
                        stockSection.document(val).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot documentSnapshot1 = task1.getResult();
                                if (documentSnapshot1.exists()) {
                                    Stock stock = documentSnapshot1.toObject(Stock.class);
                                    WaitList waitList = new WaitList(books, stock);
                                    allWaitListBooks.add(waitList);
                                    waitListAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                } else {
                    waitListProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        waitListProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onShouldTryBookIdsDownloaded(List<String> list) {
        for (String val :
                list) {
            booksSection.document(val).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Books books = documentSnapshot.toObject(Books.class);
                        assert books != null;
                        allShouldTryBooks.add(books);
                        shouldTryAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onCurrentReadsBookIdsDownloaded(List<String> list) {
        for (String val :
                list) {
            booksSection.document(val).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Books books = documentSnapshot.toObject(Books.class);
                        assert books != null;
                        allcurrentReadsBooks.add(books);
                        currentReadsAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onNewReleaseClicked(String bookDocId) {
        onFeatureMenuClicked.onNewReleaseClicked(bookDocId);
    }

    @Override
    public void onWaitListClicked(String bookDocId) {
        processStatusSelection(bookDocId);
    }

    @Override
    public void onShouldTryClicked(String bookDocId) {
        onFeatureMenuClicked.onShouldTryClicked(bookDocId);
    }

    @Override
    public void onCurrentReadsClicked(Books books) {
        processShowCurrentReadDialog(books);
    }

    @Override
    public void onNewReleaseLinearLayoutClicked() {

    }

    private void processStatusSelection(String bookId) {

        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choose_status);
        dialog.setCanceledOnTouchOutside(true);

        MaterialTextView addToWaitListTxt, moveToReadLaterTxt;
        addToWaitListTxt = dialog.findViewById(R.id.dialogChooseStatusRemoveFromWaitListTxt);
        moveToReadLaterTxt = dialog.findViewById(R.id.dialogChooseStatusMoveToReadLaterTxt);

        bookIdForProcessStatus = bookId;

        addToWaitListTxt.setOnClickListener(this);
        moveToReadLaterTxt.setOnClickListener(this);


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void processShowCurrentReadDialog(Books book) {
        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_current_read_display);
        dialog.setCanceledOnTouchOutside(true);

        MaterialTextView titleTxt, authorTxt, fineTxt, daysLeftTxt;
        ImageView coverImg;
        ProgressBar progressBar;
        titleTxt = dialog.findViewById(R.id.dialogCurrentReadsTitleTxt);
        authorTxt = dialog.findViewById(R.id.dialogCurrentReadsAuthorTxt);
        fineTxt = dialog.findViewById(R.id.dialogCurrentReadsFineAmountTxt);
        daysLeftTxt = dialog.findViewById(R.id.dialogCurrentReadsDaysLeftTxt);
        progressBar = dialog.findViewById(R.id.dialogCurrentReadsProgressBar);
        coverImg = dialog.findViewById(R.id.dialogCurrentReadsCoverImg);

        progressBar.setVisibility(View.VISIBLE);

        titleTxt.setText(book.getTitle());
        authorTxt.setText(book.getAuthor());
        Picasso.get().load(book.getImgUrl()).into(coverImg);
        String applicationID = User.getInstance().getApplicationID();

        Date today = new Date();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        accessIDSection
                .whereEqualTo("bookDocID", book.getBookDocID())
                .whereEqualTo("applicationID", applicationID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            AccessID accessID = documentSnapshot.toObject(AccessID.class);
                            assert accessID != null;
                            long milis = today.getTime() - accessID.getCheckOut().toDate().getTime();
                            long days_difference = (milis / (1000 * 60 * 60 * 24)) % 365;
                            String fineAmount = "₹ 0";
                            String daysLeft = "";
                            if (days_difference > 7) {
                                long extra = days_difference - 7;
                                daysLeft = "Over due by " + extra + " days";
                                fineAmount = "₹ " + (extra * 2);
                            } else if (days_difference == 7) {
                                daysLeft = "Due today";
                            } else {
                                long extra = 7 - days_difference;
                                daysLeft = extra + " days left";
                            }
                            fineTxt.setText(fineAmount);
                            daysLeftTxt.setText(daysLeft);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                });


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dialogChooseStatusRemoveFromWaitListTxt) {
            processRemoveFromWaitList(bookIdForProcessStatus);
        } else if (id == R.id.dialogChooseStatusMoveToReadLaterTxt) {
            uploadData.uploadDataToAddToShouldTry(bookIdForProcessStatus);
        }else if (id ==R.id.homePageNewReleaseLinearLayout){
            onFeatureMenuClicked.onNewReleaseLinearLayoutClicked();
        }
    }

    private void processRemoveFromWaitList(String bookId) {
        Map<String, Object> data = new HashMap<>();
        data.put("user_ID", User.getInstance().getUser_UID());
        data.put("bookDocID", bookId);
        data.put("process", false);
        uploadData.uploadDataToRemoveFromWaitListRequest(data);
    }

    @Override
    public void onUploadedToRequestToAddToWaitList() {

    }

    @Override
    public void onUploadedToRequestToRemoveFromWaitList() {
        dialog.dismiss();
    }

    @Override
    public void onUploadedToAddToShouldTry() {
        dialog.dismiss();
    }

    @Override
    public void onUploadedToRemoveFromShouldTry() {

    }

    @Override
    public void onUploadFailed(String message) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (waitListListener != null) {
            waitListListener.remove();
            waitListListener = null;
        }

        if (shouldTryListener != null) {
            shouldTryListener.remove();
            shouldTryListener = null;
        }

        if (currentReadsListener != null) {
            currentReadsListener.remove();
            currentReadsListener = null;
        }

    }

}