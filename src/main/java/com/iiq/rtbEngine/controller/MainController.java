package com.iiq.rtbEngine.controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iiq.rtbEngine.models.RequestParams;
import com.iiq.rtbEngine.services.ActionHandler;
import com.iiq.rtbEngine.services.DbManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {

    private static final String ACTION_TYPE_VALUE = "act";
    private static final String ATTRIBUTE_ID_VALUE = "atid";
    private static final String PROFILE_ID_VALUE = "pid";

    private static final String UNMATCHED = "unmatched";
    private static final String CAPPED = "capped";

    public enum UrlParam {
        ACTION_TYPE(ACTION_TYPE_VALUE),
        ATTRIBUTE_ID(ATTRIBUTE_ID_VALUE),
        PROFILE_ID(PROFILE_ID_VALUE),
        ;

        private final String value;

        private UrlParam(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ActionType {
        ATTRIBUTION_REQUEST(0),
        BID_REQUEST(1),
        ;

        private int id;
        private static Map<Integer, ActionType> idToRequestMap = new HashMap<>();

        static {
            for (ActionType actionType : ActionType.values())
                idToRequestMap.put(actionType.getId(), actionType);
        }

        public int getId() {
            return this.id;
        }

        private ActionType(int id) {
            this.id = id;
        }

        public static ActionType getActionTypeById(int id) {
            return idToRequestMap.get(id);
        }

    }

    @Autowired
    private DbManager dbManager;

    private Map<ActionType, ActionHandler> actionHandlerMap = new HashMap<>();

    private Map<Integer, Object> profileToLockMap = new HashMap<>();


    @PostConstruct
    public void init() {
        //initialize stuff after application finished start up
        actionHandlerMap.put(ActionType.ATTRIBUTION_REQUEST, this::handleAttributionRequest);
        actionHandlerMap.put(ActionType.BID_REQUEST, this::handleBidRequest);
    }

    Map<Integer, Integer> profilePublishCountMap = new ConcurrentHashMap<>();

    private String handleBidRequest(RequestParams requestParams) {
        Integer profileId = requestParams.getProfileId();

        Set<Integer> profileAttributes = dbManager.getProfileAttributes(profileId);
        if (profileAttributes.isEmpty())
            return UNMATCHED;

//        List<Integer> campaignIdListResult = getAllMatchedCampaignIds(profileAttributes);
        Set<Integer> campaignIdListResult = getAllMatchedCampaignIdsByMap(profileAttributes);

        if (campaignIdListResult == null || campaignIdListResult.isEmpty())
            return UNMATCHED;

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

        Integer campaignCapacity = dbManager.getCampaignCapacity(campaignIdResult);
        if (campaignCapacity == null) {
            //no limited
            return campaignIdResult.toString();
        }

        synchronized (profileToLockMap.getOrDefault(profileId, new Object()) ) {
            Integer profileTotalReturnCount = profilePublishCountMap.get(profileId);

            if (profileTotalReturnCount == null) {
                profilePublishCountMap.put(profileId, 1);
            } else if (profileTotalReturnCount < campaignCapacity) {
                profilePublishCountMap.put(profileId, profileTotalReturnCount + 1);
            } else {
//                profileCount.equals(campaignCapacity)
                return CAPPED;
            }
        }

        return campaignIdResult.toString();
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

    private String handleAttributionRequest(RequestParams requestParams) {
        Integer attributeId = requestParams.getAttributeId();
        Integer profileId = requestParams.getProfileId();
        dbManager.updateProfileAttribute(profileId, attributeId);
        return "";
    }

    @GetMapping("/api")
    public String getRequest(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(name = ACTION_TYPE_VALUE) int actionTypeId,
                             @RequestParam(name = ATTRIBUTE_ID_VALUE, required = false) Integer attributeId,
                             @RequestParam(name = PROFILE_ID_VALUE, required = false) Integer profileId) {
        //GOOD LUCK! (;
        ActionType actionType = ActionType.getActionTypeById(actionTypeId);
        ActionHandler actionHandler = actionHandlerMap.get(actionType);
        if (actionHandler != null) {
            return actionHandler.doAction(new RequestParams(attributeId, profileId));
        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return String.format("actionTypeId %s not supported", actionTypeId);
    }

}
