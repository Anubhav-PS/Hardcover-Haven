package com.anubhav.hardcoverhaven.enums;

public enum WAIT_LIST_TOGGLE {

    ADD("Add To Wait List"),
    REMOVE("Remove From Wait List");

    private String tag;

    WAIT_LIST_TOGGLE(String word) {
        this.tag = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
