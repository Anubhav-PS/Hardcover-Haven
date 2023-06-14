package com.anubhav.hardcoverhaven.interfaces;



import com.anubhav.hardcoverhaven.models.Books;
import com.anubhav.hardcoverhaven.models.Genre;
import com.anubhav.hardcoverhaven.models.ISBN;
import com.anubhav.hardcoverhaven.models.Stock;

import java.util.List;

public interface iOnDataDownloaded {
    void onBooksDataDownloaded(Books books);

    void onStockDataDownloaded(Stock stock);

    void onISBNDataDownloaded(ISBN isbn);

    void onGenreDataDownloaded(Genre genre);

    void onWaitListBookIdsDownloaded(List<String> list);

    void onShouldTryBookIdsDownloaded(List<String> list);

    void onCurrentReadsBookIdsDownloaded(List<String> list);
}
