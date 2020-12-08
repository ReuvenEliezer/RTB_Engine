package com.iiq.rtbEngine.services;

import java.util.Set;

@FunctionalInterface
public interface BestCampaignSolution {

    String getBestCampaignIdResultByPriorityAndLowestId(Integer profileId, Set<Integer> allMatchedCampaignIdsList);

}
