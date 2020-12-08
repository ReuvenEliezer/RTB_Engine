package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.CampaignProfile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractBestCampaignSolution implements BestCampaignSolution {

    private Map<CampaignProfile, AtomicInteger> profilePublishAtomicCountMap = new ConcurrentHashMap<>();
    //    private Map<Integer, Integer> profilePublishCountMap = new ConcurrentHashMap<>();

    @Autowired
    protected DbManager dbManager;

    protected final boolean isCampaignCapacityExceededUseAtomic(CampaignProfile campaignProfile, Integer campaignCapacity) {
        AtomicInteger profileTotalReturnCount = profilePublishAtomicCountMap.get(campaignProfile);
        if (profileTotalReturnCount == null) {
            profilePublishAtomicCountMap.put(campaignProfile, new AtomicInteger(1));
        } else if (profileTotalReturnCount.get() < campaignCapacity) {
            profileTotalReturnCount.getAndIncrement();
        } else {
//                profileCount.equals(campaignCapacity)
            return true;
        }
        return false;
    }

//    protected final boolean isCampaignCapacityExceeded(Integer profileId, Integer campaignCapacity) {
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
