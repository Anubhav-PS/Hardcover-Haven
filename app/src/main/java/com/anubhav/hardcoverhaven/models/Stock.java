package com.anubhav.hardcoverhaven.models;

public class Stock {
    private String bookDocID;
    private long totalStock;
    private long available;
    private boolean inQueue;

    public Stock() {
    }

    public Stock(String bookDocID, long totalStock, long available, boolean inQueue) {
        this.bookDocID = bookDocID;
        this.totalStock = totalStock;
        this.available = available;
        this.inQueue = inQueue;
    }

    public String getBookDocID() {
        return bookDocID;
    }

    public void setBookDocID(String bookDocID) {
        this.bookDocID = bookDocID;
    }

    public long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(long totalStock) {
        this.totalStock = totalStock;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }
}
