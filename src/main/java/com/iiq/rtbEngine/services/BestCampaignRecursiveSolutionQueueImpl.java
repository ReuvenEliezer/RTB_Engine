package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.CampaignProfile;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BestCampaignRecursiveSolutionQueueImpl extends AbstractBestCampaignSolution {


    @Override
    public String getBestCampaignIdResultByPriorityAndLowestId(Integer profileId, Set<Integer> allMatchedCampaignIdsList) {
        if (allMatchedCampaignIdsList.isEmpty())
            return ResponseTypeEnum.CAPPED.getValue();
        Integer campaignIdResult = getBestCampaignIdResultByPriorityAndLowestIdByRecursive(allMatchedCampaignIdsList);
        Integer campaignCapacity = dbManager.getCampaignCapacity(campaignIdResult);
        if (campaignCapacity != null) {
            if (isCampaignCapacityExceededUseAtomic(new CampaignProfile(profileId, campaignIdResult), campaignCapacity)) {
                allMatchedCampaignIdsList.remove(campaignIdResult);
                return getBestCampaignIdResultByPriorityAndLowestId(profileId, allMatchedCampaignIdsList);
            }
        }
        return campaignIdResult.toString();
    }

    private Integer getBestCampaignIdResultByPriorityAndLowestIdByRecursive(Set<Integer> campaignIdListResult) {
        Integer campaignIdResult = null;
        Integer campaignPriority = null;

        for (Integer campaignId : campaignIdListResult) {
            if (campaignIdResult == null) {
                campaignIdResult = campaignId;
                campaignPriority = dbManager.getCampaignPriority(campaignId);
            } else {
                Integer currentPriority = dbManager.getCampaignPriority(campaignId);
                if (campaignPriority == null) {
                    throw new IllegalArgumentException("not defined a priority for campaignId: " + campaignId);
                }
                if (currentPriority > campaignPriority) {
                    campaignPriority = currentPriority;
                    campaignIdResult = campaignId;
                } else if (currentPriority.equals(campaignPriority)) {
                    if (campaignId < campaignIdResult) {
                        campaignIdResult = campaignId;
                    }
                }
            }
        }
        return campaignIdResult;
    }

}
