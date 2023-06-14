package com.anubhav.hardcoverhaven.interfaces;

import com.anubhav.hardcoverhaven.models.Books;

public interface iOnFeatureItemClicked {
    void onNewReleaseClicked(String bookDocId);

    void onWaitListClicked(String bookDocId);

    void onShouldTryClicked(String bookDocId);

    void onCurrentReadsClicked(Books bookDocId);

    void onNewReleaseLinearLayoutClicked();
}
