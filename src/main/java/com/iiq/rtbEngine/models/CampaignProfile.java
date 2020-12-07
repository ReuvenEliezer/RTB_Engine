package com.iiq.rtbEngine.models;

import java.util.Objects;

public class CampaignProfile {

   private Integer profileId;
   private Integer campaignId;

    public CampaignProfile(Integer profileId, Integer campaignId) {
        this.profileId = profileId;
        this.campaignId = campaignId;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CampaignProfile that = (CampaignProfile) o;
        return Objects.equals(profileId, that.profileId) && Objects.equals(campaignId, that.campaignId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, campaignId);
    }

    @Override
    public String toString() {
        return "CampaignProfile{" +
                "profile=" + profileId +
                ", campaign=" + campaignId +
                '}';
    }
}
