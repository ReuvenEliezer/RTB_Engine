package com.iiq.rtbEngine.models;

public class RequestParams {
    private Integer attributeId;
    private Integer profileId;

    public RequestParams(Integer attributeId, Integer profileId) {
        this.attributeId = attributeId;
        this.profileId = profileId;
    }

    public RequestParams(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Integer attributeId) {
        this.attributeId = attributeId;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    @Override
    public String toString() {
        return "RequestParams{" +
                "attributeId=" + attributeId +
                ", profileId=" + profileId +
                '}';
    }
}
