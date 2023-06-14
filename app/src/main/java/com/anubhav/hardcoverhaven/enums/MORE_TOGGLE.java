package com.anubhav.hardcoverhaven.enums;

public enum MORE_TOGGLE {

    MORE("+ more"),
    LESS("- less");

    private String tag;

    MORE_TOGGLE(String word) {
        this.tag = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}