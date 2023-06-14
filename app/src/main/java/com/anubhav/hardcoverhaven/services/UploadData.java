package com.anubhav.hardcoverhaven.services;

import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UploadData {

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference booksSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.BOOKS.getPath());
    private final CollectionReference genreSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.GENRE.getPath());
    private final CollectionReference isbnSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.ISBN.getPath());
    private final CollectionReference stockSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.STOCK.getPath());

    private final CollectionReference waitListRequestSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.WAIT_LIST_REQUEST.getPath());
    private final CollectionReference shouldTrySection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.SHOULD_TRY.getPath());

    private final String errorDownloadFailed = "Upload failed,Try again later";
    private final String errorDocumentNotExist = "Document does not exist";
    private UploadData.iOnUploadedDataCallback callback;


    public UploadData(UploadData.iOnUploadedDataCallback callback) {
        this.callback = callback;
    }

    public void uploadDataToAddToWaitListRequest(Map<String, Object> data) {

        waitListRequestSection
                .document()
                .set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onUploadedToRequestToAddToWaitList();
                    } else {
                        callback.onUploadFailed(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });

    }

    public void uploadDataToRemoveFromWaitListRequest(Map<String, Object> data) {

        waitListRequestSection
                .document()
                .set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onUploadedToRequestToRemoveFromWaitList();
                    } else {
                        callback.onUploadFailed(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });

    }

    public void uploadDataToAddToShouldTry(String bookId) {
        shouldTrySection.document(User.getInstance().getUser_UID())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            shouldTrySection.document(User.getInstance().getUser_UID())
                                    .update("bookDocId", FieldValue.arrayUnion(bookId)).addOnCompleteListener(task12 -> {
                                        if (task12.isSuccessful()) {
                                            callback.onUploadedToAddToShouldTry();
                                        } else {
                                            callback.onUploadFailed(Objects.requireNonNull(task12.getException()).getMessage());
                                        }
                                    });
                        } else {
                            Map<String, Object> data = new HashMap<>();
                            data.put("bookDocId", List.of(bookId));
                            shouldTrySection.document(User.getInstance().getUser_UID())
                                    .set(data).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            callback.onUploadedToAddToShouldTry();
                                        } else {
                                            callback.onUploadFailed(Objects.requireNonNull(task1.getException()).getMessage());
                                        }
                                    });
                        }
                    }
                });
    }

    public void uploadDataToRemoveFromShouldTry(String bookId) {

        DocumentReference documentReference = shouldTrySection.document(User.getInstance().getUser_UID());

        documentReference
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            documentReference.update("bookDocId", FieldValue.arrayRemove(bookId)).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    callback.onUploadedToRemoveFromShouldTry();
                                } else {
                                    callback.onUploadFailed(Objects.requireNonNull(task1.getException()).getMessage());
                                }
                            });
                        } else {
                            callback.onUploadFailed(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });

    }

    public interface iOnUploadedDataCallback {

        void onUploadedToRequestToAddToWaitList();

        void onUploadedToRequestToRemoveFromWaitList();

        void onUploadedToAddToShouldTry();

        void onUploadedToRemoveFromShouldTry();

        void onUploadFailed(String message);

    }
}
