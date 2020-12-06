package com.iiq.rtbEngine.models;

import com.iiq.rtbEngine.util.CommonConfig;

public enum UrlParamEnum {

    ACTION_TYPE(CommonConfig.ACTION_TYPE_VALUE),
    ATTRIBUTE_ID(CommonConfig.ATTRIBUTE_ID_VALUE),
    PROFILE_ID(CommonConfig.PROFILE_ID_VALUE),
    ;

    private String value;

    UrlParamEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
