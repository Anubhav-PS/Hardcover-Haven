package com.anubhav.hardcoverhaven.enums;

public enum Path {

    HARDCOVER("HARDCOVER"),
    HARDCOVER_HAVEN("HARDCOVER_HAVEN"),
    ENTRY("ENTRY"),

    FILES("FILES"),

    BOOKS("BOOKS"),
    GENRE("GENRE"),
    ISBN("ISBN"),
    STOCK("STOCK"),

    WAIT_LIST("WAIT_LIST"),
    WAIT_LIST_REQUEST("WAIT_LIST_REQUEST"),

    SHOULD_TRY("SHOULD_TRY"),
    CURRENT_READS("CURRENT_READS"),
    ACCESS_ID("ACCESS_ID"),


    ACCOUNTS("ACCOUNTS"),
    ACCOUNT_Q("ACCOUNT_Q"),
    STUDENTS("STUDENTS"),

    FEEDBACKS("FEEDBACKS"),
    ISSUES("ISSUES"),
    SUGGESTIONS("SUGGESTIONS"),
    BUGS("BUGS"),

    FCM_TOKEN("FCM_TOKEN");


    final String path;

    Path(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
