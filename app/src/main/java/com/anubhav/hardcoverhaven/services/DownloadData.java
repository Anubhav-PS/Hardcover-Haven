package com.anubhav.hardcoverhaven.services;

import com.anubhav.hardcoverhaven.enums.Path;
import com.anubhav.hardcoverhaven.models.Books;
import com.anubhav.hardcoverhaven.models.Genre;
import com.anubhav.hardcoverhaven.models.ISBN;
import com.anubhav.hardcoverhaven.models.Stock;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DownloadData {

    //firebase fire store declaration
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference booksSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.BOOKS.getPath());
    private final CollectionReference genreSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.GENRE.getPath());
    private final CollectionReference isbnSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.ISBN.getPath());
    private final CollectionReference stockSection = db.collection(Path.HARDCOVER.getPath()).document(Path.ENTRY.getPath()).collection(Path.STOCK.getPath());
    private final String errorDownloadFailed = "Download failed,Try again later";
    private final String errorDocumentNotExist = "Document does not exist";
    private DownloadData.iOnDownloadedDataCallback callback;


    public DownloadData(iOnDownloadedDataCallback callback) {
        this.callback = callback;
    }

    public void downloadBookDataFor(String bookId) {
        booksSection
                .document(bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Books books = documentSnapshot.toObject(Books.class);
                            callback.onDownloadedBookData(books);
                        } else {
                            callback.onDownloadFailed(errorDocumentNotExist);
                        }
                    } else {
                        callback.onDownloadFailed(errorDownloadFailed);
                    }
                });
    }

    public void downloadGenreDataFor(String bookId) {

        genreSection
                .document(bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Genre genre = documentSnapshot.toObject(Genre.class);
                            callback.onDownloadedGenreData(genre);
                        } else {
                            callback.onDownloadFailed(errorDocumentNotExist);
                        }
                    } else {
                        callback.onDownloadFailed(errorDownloadFailed);
                    }
                });

    }

    public void downloadIsbnDataFor(String bookId) {
        isbnSection.document(bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            ISBN isbn = documentSnapshot.toObject(ISBN.class);
                            callback.onDownloadedIsbnData(isbn);
                        } else {
                            callback.onDownloadFailed(errorDocumentNotExist);
                        }
                    } else {
                        callback.onDownloadFailed(errorDownloadFailed);
                    }
                });

    }

    public void downloadStockDataFor(String bookId) {
        stockSection.document(bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Stock stock = documentSnapshot.toObject(Stock.class);
                            callback.onDownloadedStockData(stock);
                        } else {
                            callback.onDownloadFailed(errorDocumentNotExist);
                        }
                    } else {
                        callback.onDownloadFailed(errorDownloadFailed);
                    }
                });
    }


    public interface iOnDownloadedDataCallback {

        void onDownloadedBookData(Books book);

        void onDownloadedStockData(Stock stock);

        void onDownloadedGenreData(Genre genre);

        void onDownloadedIsbnData(ISBN isbn);

        void onDownloadedAllBookData();

        void onDownloadFailed(String message);

    }


}
