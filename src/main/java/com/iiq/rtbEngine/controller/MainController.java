package com.iiq.rtbEngine.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iiq.rtbEngine.db.ProfilesDao;
import com.iiq.rtbEngine.models.ActionTypeEnum;
import com.iiq.rtbEngine.models.RequestParams;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import com.iiq.rtbEngine.services.ActionHandler;
import com.iiq.rtbEngine.services.DbManager;
import com.iiq.rtbEngine.util.CommonConfig;
import com.iiq.rtbEngine.util.WsAddressConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(WsAddressConstants.apiLogicUrl)
public class MainController {

    @Autowired
    private DbManager dbManager;

    @Autowired
    private ProfilesDao profilesDao;

    private Map<ActionTypeEnum, ActionHandler> actionHandlerMap = new HashMap<>();
    private Map<Integer, Integer> profilePublishCountMap = new ConcurrentHashMap<>();

//    private Map<Integer, Object> profileToLockMap = new HashMap<>();

    @PostConstruct
    public void init() {
        //initialize stuff after application finished start up
        actionHandlerMap.put(ActionTypeEnum.ATTRIBUTION_REQUEST, this::handleAttributionRequest);
        actionHandlerMap.put(ActionTypeEnum.BID_REQUEST, this::handleBidRequest);
    }

    @GetMapping()
    public String getRequest(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(name = CommonConfig.ACTION_TYPE_VALUE) int actionTypeId,
                             @RequestParam(name = CommonConfig.ATTRIBUTE_ID_VALUE, required = false) Integer attributeId,
                             @RequestParam(name = CommonConfig.PROFILE_ID_VALUE, required = false) Integer profileId) {
        //GOOD LUCK! (;
        ActionTypeEnum actionType = ActionTypeEnum.getActionTypeById(actionTypeId);
        ActionHandler actionHandler = actionHandlerMap.get(actionType);
        if (actionHandler != null) {
            return actionHandler.doAction(new RequestParams(attributeId, profileId));
        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return String.format("actionTypeId %s not supported", actionTypeId);
    }

    private String handleAttributionRequest(RequestParams requestParams) {
        Integer attributeId = requestParams.getAttributeId();
        Integer profileId = requestParams.getProfileId();
        dbManager.updateProfileAttribute(profileId, attributeId);
        return "";
    }

    private String handleBidRequest(RequestParams requestParams) {
        Integer profileId = requestParams.getProfileId();

        Set<Integer> profileAttributes = dbManager.getProfileAttributes(profileId);
        if (profileAttributes.isEmpty())
            return ResponseTypeEnum.UNMATCHED.getValue();

//        List<Integer> campaignIdListResult = getAllMatchedCampaignIds(profileAttributes);
        Set<Integer> allMatchedCampaignIdsList = getAllMatchedCampaignIdsByMap(profileAttributes);

        if (allMatchedCampaignIdsList == null || allMatchedCampaignIdsList.isEmpty())
            return ResponseTypeEnum.UNMATCHED.getValue();

        Integer campaignIdResult = getCampaignIdResult(allMatchedCampaignIdsList);

        Integer campaignCapacity = dbManager.getCampaignCapacity(campaignIdResult);
        if (campaignCapacity == null) {
            //no limited
            return campaignIdResult.toString();
        }

        if (isCampaignCapacityExceeded(profileId, campaignCapacity))
            return ResponseTypeEnum.CAPPED.getValue();

        return campaignIdResult.toString();
    }

    private boolean isCampaignCapacityExceeded(Integer profileId, Integer campaignCapacity) {
        synchronized (profilesDao.getLockProfile(profileId)) {
            Integer profileTotalReturnCount = profilePublishCountMap.get(profileId);

            if (profileTotalReturnCount == null) {
                profilePublishCountMap.put(profileId, 1);
            } else if (profileTotalReturnCount < campaignCapacity) {
                profilePublishCountMap.put(profileId, profileTotalReturnCount + 1);
            } else {
//                profileCount.equals(campaignCapacity)
                return true;
            }
        }
        return false;
    }

    private Integer getCampaignIdResult(Set<Integer> campaignIdListResult) {
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

    private List<Integer> getAllMatchedCampaignIds(Set<Integer> profileAttributes) {
        Map<Integer, List<Integer>> allCampaignAttributes = dbManager.getAllCampaignAttributes();
        List<Integer> campaignIdListResult = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> campaignAttributesEntry : allCampaignAttributes.entrySet()) {
            List<Integer> campaignAttributes = campaignAttributesEntry.getValue();
            if (isAttributesMatched(profileAttributes, campaignAttributes)) {
                campaignIdListResult.add(campaignAttributesEntry.getKey());
            }
        }
        return campaignIdListResult;
    }

    private Set<Integer> getAllMatchedCampaignIdsByMap(Set<Integer> profileAttributes) {
        return dbManager.getCampaigns(profileAttributes);

    }

    private boolean isAttributesMatched(Set<Integer> profileAttributes, List<Integer> campaignAttributes) {
        if (campaignAttributes == null || campaignAttributes.isEmpty()) return false;
        for (Integer integer : campaignAttributes) {
            if (!profileAttributes.contains(integer)) {
                return false;
            }
        }
        return true;
    }


}
