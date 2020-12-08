package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.CampaignProfile;
import com.iiq.rtbEngine.models.CampaignRank;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

@Component
public class BestCampaignQueueSolutionImpl extends AbstractBestCampaignSolution {

    @Override
    public String getBestCampaignIdResultByPriorityAndLowestId(Integer profileId, Set<Integer> allMatchedCampaignIdsList) {
        Comparator<CampaignRank> lowestCampaignIdComparing = Comparator.comparing(CampaignRank::getCampaignId).reversed();
        Comparator<CampaignRank> highestCampaignPriorityComparing = Comparator.comparing(CampaignRank::getCampaignPriority).reversed();
        PriorityBlockingQueue<CampaignRank> campaignQueue = new PriorityBlockingQueue(allMatchedCampaignIdsList.size(),
                highestCampaignPriorityComparing.thenComparing(lowestCampaignIdComparing));

        for (Integer campaignId : allMatchedCampaignIdsList) {
            Integer campaignPriority = dbManager.getCampaignPriority(campaignId);
            campaignQueue.add(new CampaignRank(campaignId, campaignPriority));
        }

        while (!campaignQueue.isEmpty()) {
            CampaignRank campaignRank = campaignQueue.poll();
            CampaignProfile campaignProfile = new CampaignProfile(profileId, campaignRank.getCampaignId());
            Integer campaignCapacity = dbManager.getCampaignCapacity(campaignRank.getCampaignId());
            if (!isCampaignCapacityExceededUseAtomic(campaignProfile, campaignCapacity)) {
                return String.valueOf(campaignRank.getCampaignId());
            }
        }
        return ResponseTypeEnum.CAPPED.getValue();
    }
}
