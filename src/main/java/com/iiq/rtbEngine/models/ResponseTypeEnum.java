package com.iiq.rtbEngine.models;

public enum ResponseTypeEnum {

    UNMATCHED("unmatched"),
    CAPPED("capped"),
    ;

    private String value;

    ResponseTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
