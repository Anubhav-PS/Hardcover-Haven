package com.anubhav.hardcoverhaven.models;

public class Genre {

    private String bookDocId;
    private String subject;
    private String ddsGroup;
    private String ddsNumber;

    public Genre() {
        this.bookDocId = "";
        this.subject = "";
        this.ddsGroup = "";
        this.ddsNumber = "";
    }

    public Genre(String bookDocId, String subject, String ddsGroup, String ddsNumber) {
        this.bookDocId = bookDocId;
        this.subject = subject;
        this.ddsGroup = ddsGroup;
        this.ddsNumber = ddsNumber;
    }

    public String getBookDocId() {
        return bookDocId;
    }

    public void setBookDocId(String bookDocId) {
        this.bookDocId = bookDocId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDdsGroup() {
        return ddsGroup;
    }

    public void setDdsGroup(String ddsGroup) {
        this.ddsGroup = ddsGroup;
    }

    public String getDdsNumber() {
        return ddsNumber;
    }

    public void setDdsNumber(String ddsNumber) {
        this.ddsNumber = ddsNumber;
    }
}
