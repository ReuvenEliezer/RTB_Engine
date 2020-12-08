package com.iiq.rtbEngine.models;

import java.util.Objects;

public class CampaignRank {

    int campaignId;
    int campaignPriority;

    public CampaignRank(int campaignId, int campaignPriority) {
        this.campaignId = campaignId;
        this.campaignPriority = campaignPriority;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public int getCampaignPriority() {
        return campaignPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CampaignRank that = (CampaignRank) o;
        return campaignId == that.campaignId && campaignPriority == that.campaignPriority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(campaignId, campaignPriority);
    }

    @Override
    public String toString() {
        return "CampaignRank{" +
                "campaignId=" + campaignId +
                ", campaignPriority=" + campaignPriority +
                '}';
    }
}
