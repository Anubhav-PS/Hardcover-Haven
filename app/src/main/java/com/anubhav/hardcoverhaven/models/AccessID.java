package com.anubhav.hardcoverhaven.models;

import com.google.firebase.Timestamp;

public class AccessID {
    private String accessID;
    private String applicationID;
    private String bookDocID;
    private Timestamp checkIn;
    private Timestamp checkOut;

    public AccessID() {
    }

    public AccessID(String accessID, String applicationID, String bookDocID, Timestamp checkIn, Timestamp checkOut) {
        this.accessID = accessID;
        this.applicationID = applicationID;
        this.bookDocID = bookDocID;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public String getAccessID() {
        return accessID;
    }

    public void setAccessID(String accessID) {
        this.accessID = accessID;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getBookDocID() {
        return bookDocID;
    }

    public void setBookDocID(String bookDocID) {
        this.bookDocID = bookDocID;
    }

    public Timestamp getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(Timestamp checkIn) {
        this.checkIn = checkIn;
    }

    public Timestamp getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(Timestamp checkOut) {
        this.checkOut = checkOut;
    }
}
