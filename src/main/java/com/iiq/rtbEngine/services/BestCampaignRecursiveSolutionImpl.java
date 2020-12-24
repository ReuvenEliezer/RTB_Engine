package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.CampaignProfile;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;

@Primary
@Component
public class BestCampaignRecursiveSolutionImpl extends AbstractBestCampaignSolution {


    @Override
    public String getBestCampaignIdResultByPriorityAndLowestId(Integer profileId, Set<Integer> allMatchedCampaignIdsList) {
        if (allMatchedCampaignIdsList.isEmpty())
            return ResponseTypeEnum.CAPPED.getValue();
        Integer campaignIdResult = getBestCampaignIdResultByPriorityAndLowestId(allMatchedCampaignIdsList);
        Integer campaignCapacity = dbManager.getCampaignCapacity(campaignIdResult);
        if (campaignCapacity != null) {
            if (isCampaignProfileReachedMaxCapacity(new CampaignProfile(profileId, campaignIdResult), campaignCapacity)) {
                allMatchedCampaignIdsList.remove(campaignIdResult);
                return getBestCampaignIdResultByPriorityAndLowestId(profileId, allMatchedCampaignIdsList);
            }
        }
        return campaignIdResult.toString();
    }

    private Integer getBestCampaignIdResultByPriorityAndLowestId(Set<Integer> campaignIdListResult) {
        Integer campaignIdResult = null;
        Integer campaignPriority = null;

        for (Integer campaignId : campaignIdListResult) {
            Integer currentPriority = dbManager.getCampaignPriority(campaignId);
            if (campaignIdResult == null || currentPriority > campaignPriority) {
                campaignPriority = currentPriority;
                campaignIdResult = campaignId;
            } else if (currentPriority.equals(campaignPriority)) {
                if (campaignId < campaignIdResult) {
                    campaignIdResult = campaignId;
                }
            }
        }
        return campaignIdResult;
    }

}
