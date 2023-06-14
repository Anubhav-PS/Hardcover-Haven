package com.anubhav.hardcoverhaven.models;

public class WaitList {
    private Books books;
    private Stock stock;

    public WaitList() {
    }

    public WaitList(Books books, Stock stock) {
        this.books = books;
        this.stock = stock;
    }

    public Books getBooks() {
        return books;
    }

    public void setBooks(Books books) {
        this.books = books;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}
