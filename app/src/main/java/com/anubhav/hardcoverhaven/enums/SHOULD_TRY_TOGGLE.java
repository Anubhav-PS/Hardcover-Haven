package com.anubhav.hardcoverhaven.enums;

public enum SHOULD_TRY_TOGGLE {

    ADD("save for later"),
    REMOVE("remove from save later");

    private String tag;

    SHOULD_TRY_TOGGLE(String word) {
        this.tag = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}