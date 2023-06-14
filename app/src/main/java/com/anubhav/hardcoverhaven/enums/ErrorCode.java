package com.anubhav.hardcoverhaven.enums;

public enum ErrorCode {

    RA002("RA002","Account couldn't be created!"),
    RA004("RA004","User details couldn't be uploaded!"),
    RA006("RA006","Verification link couldn't not be sent!"),

    EPF001("EPF001","Updating user detail failed!"),

    DF001("DF001","User Details couldn't be erased!"),
    DF002("DF002","User Account ID couldn't be deleted!"),


    OTH("NO CODE"," ");

    final String errorCode;
    final String errorMessage;

    ErrorCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "Error "+this.errorCode+" ,"+this.errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}




