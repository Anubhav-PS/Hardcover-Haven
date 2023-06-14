package com.anubhav.hardcoverhaven.models;

public class ISBN {

    private String bookDocId;
    private String isbn10;
    private String isbn13;

    public ISBN() {
        this.bookDocId = "";
        this.isbn10 = "";
        this.isbn13 = "";
    }

    public ISBN(String bookDocId, String isbn10, String isbn13) {
        this.bookDocId = bookDocId;
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
    }

    public String getBookDocId() {
        return bookDocId;
    }

    public void setBookDocId(String bookDocId) {
        this.bookDocId = bookDocId;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }
}
