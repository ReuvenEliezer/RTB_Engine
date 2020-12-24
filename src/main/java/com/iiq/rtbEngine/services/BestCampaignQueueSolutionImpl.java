package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.CampaignProfile;
import com.iiq.rtbEngine.models.CampaignRank;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

//@Primary
@Component
public class BestCampaignQueueSolutionImpl extends AbstractBestCampaignSolution {

    @Override
    public String getBestCampaignIdResultByPriorityAndLowestId(Integer profileId, Set<Integer> allMatchedCampaignIdsList) {
        Comparator<CampaignRank> lowestCampaignIdComparing = Comparator.comparing(CampaignRank::getCampaignId);
        Comparator<CampaignRank> highestCampaignPriorityComparing = Comparator.comparing(CampaignRank::getCampaignPriority).reversed();

        //sorted by highest priority and lowest id
        PriorityBlockingQueue<CampaignRank> campaignQueue = new PriorityBlockingQueue(allMatchedCampaignIdsList.size(),
                highestCampaignPriorityComparing.thenComparing(lowestCampaignIdComparing));

        for (Integer campaignId : allMatchedCampaignIdsList) {
            Integer campaignPriority = dbManager.getCampaignPriority(campaignId);
            campaignQueue.add(new CampaignRank(campaignId, campaignPriority));
        }

        System.out.println("campaignQueue: "+campaignQueue.toString());

        while (!campaignQueue.isEmpty()) {
            CampaignRank campaignRank = campaignQueue.poll();
            System.out.println("poll campaignRank: "+campaignRank.toString());
            CampaignProfile campaignProfile = new CampaignProfile(profileId, campaignRank.getCampaignId());
            Integer campaignCapacity = dbManager.getCampaignCapacity(campaignRank.getCampaignId());
            if (!isCampaignCapacityExceeded(campaignProfile, campaignCapacity)) {
                return String.valueOf(campaignRank.getCampaignId());
            }
        }
        return ResponseTypeEnum.CAPPED.getValue();
    }
}
