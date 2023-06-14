package com.anubhav.hardcoverhaven.models;

import com.google.firebase.Timestamp;

public class Books {

    private String bookDocID;
    private String imgUrl;
    private String title;
    private String author;
    private String language;
    private String publisher;
    private long publishedYear;
    private String edition;
    private Timestamp addedOn;

    public Books() {
        this.imgUrl = "";
    }


    public Books(String bookDocID, String imgUrl, String title, String author, String language, String publisher, long publishedYear, String edition, Timestamp addedOn) {
        this.bookDocID = bookDocID;
        this.imgUrl = imgUrl;
        this.title = title;
        this.author = author;
        this.language = language;
        this.publisher = publisher;
        this.publishedYear = publishedYear;
        this.edition = edition;
        this.addedOn = addedOn;
    }

    public String getBookDocID() {
        return bookDocID;
    }

    public void setBookDocID(String bookDocID) {
        this.bookDocID = bookDocID;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public long getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(long publishedYear) {
        this.publishedYear = publishedYear;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public Timestamp getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Timestamp addedOn) {
        this.addedOn = addedOn;
    }
}
