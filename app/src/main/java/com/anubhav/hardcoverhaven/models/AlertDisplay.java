package com.anubhav.hardcoverhaven.models;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AlertDisplay {

    private String title;
    private String message;
    private Context context;
    private MaterialAlertDialogBuilder builder;

    public AlertDisplay(String title, String message, Context context) {
        this.title = title;
        this.message = message;
        this.context = context;
        builder = new MaterialAlertDialogBuilder(this.context);
    }

    public AlertDisplay(String message) {
        this.message = message;
    }

    public void displayAlert() {
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", (dialogInterface, i) -> {
        });
        builder.show();
    }

    public void display() {
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void displayAlert(String positiveBtn) {
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveBtn, (dialogInterface, i) -> {
        });
    }

    public void displayAlert(String positiveBtn, String negativeBtn) {
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveBtn, (dialogInterface, i) -> {
        });
        builder.setNegativeButton(negativeBtn, (dialog, which) -> {
        });
    }

    public void show() {
        this.builder.show();
    }

    public MaterialAlertDialogBuilder getBuilder() {
        return this.builder;
    }

}
