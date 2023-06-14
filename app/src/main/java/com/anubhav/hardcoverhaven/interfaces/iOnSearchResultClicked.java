package com.anubhav.hardcoverhaven.interfaces;

import android.content.Intent;

public interface iOnSearchResultClicked {
    void onSearchResultClicked(String bookID);
    void onSearchResultViewMoreClicked(Intent intent);
}
