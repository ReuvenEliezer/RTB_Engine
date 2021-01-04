package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.CampaignProfile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractBestCampaignSolution implements BestCampaignSolution {

    private Map<CampaignProfile, AtomicInteger> profilePublishToPublishAtomicCountMap = new ConcurrentHashMap<>();
    //    private Map<Integer, Integer> profilePublishCountMap = new ConcurrentHashMap<>();

    @Autowired
    protected DbManager dbManager;

    //using atomic for multi threaded for same profile (user)
    protected final boolean isCampaignProfileReachedMaxCapacity(CampaignProfile campaignProfile, Integer campaignCapacity) {
        if (campaignCapacity < 1)
            return true;
        AtomicInteger profileTotalReturnCount = profilePublishToPublishAtomicCountMap.computeIfAbsent(campaignProfile, s -> new AtomicInteger());
        if (profileTotalReturnCount.get() < campaignCapacity) {
            profileTotalReturnCount.getAndIncrement();
            return false;
        }
//                profileCount.equals(campaignCapacity)
        System.out.println(String.format("campaign id %s for profile (user-id) %s is reached the max capacity: %s ", campaignProfile.getCampaignId(), campaignProfile.getProfileId(), campaignCapacity));
        return true;
    }

//    protected final boolean isCampaignProfileReachedMaxCapacity(Integer profileId, Integer campaignCapacity) {
//        synchronized (profilesDao.getLockProfile(profileId)) {
//            Integer profileTotalReturnCount = profilePublishCountMap.get(profileId);
//
//            if (profileTotalReturnCount == null) {
//                profilePublishCountMap.put(profileId, 1);
//            } else if (profileTotalReturnCount < campaignCapacity) {
//                profilePublishCountMap.put(profileId, profileTotalReturnCount + 1);
//            } else {
////                profileCount.equals(campaignCapacity)
//                return true;
//            }
//        }
//        return false;
//    }

}
