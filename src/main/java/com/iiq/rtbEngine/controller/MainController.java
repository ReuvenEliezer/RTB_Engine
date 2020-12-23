package com.iiq.rtbEngine.controller;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iiq.rtbEngine.db.ProfilesDao;
import com.iiq.rtbEngine.models.ActionTypeEnum;
import com.iiq.rtbEngine.models.RequestParams;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import com.iiq.rtbEngine.services.ActionHandler;
import com.iiq.rtbEngine.services.BestCampaignSolution;
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

    @Autowired
    private Map<String, BestCampaignSolution> stringBestCampaignSolutionMap;

    @Autowired
    private BestCampaignSolution bestCampaignSolution;

    private Map<ActionTypeEnum, ActionHandler> actionHandlerMap = new HashMap<>();

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

    /**
     * this add attributeId to profileId.
     * for each profileId may have one or many attributes
     *
     * @param requestParams
     * @return
     */
    private String handleAttributionRequest(RequestParams requestParams) {
        Integer attributeId = requestParams.getAttributeId();
        Integer profileId = requestParams.getProfileId();
        dbManager.updateProfileAttribute(profileId, attributeId);
        return "";
    }

    /**
     * attribute is a sector that the profile (user) have the interested with it.
     * for example:
     * for (profile)user #1 - have the attributes 1,2 - when the #1 is Sport Sectors and #2 is a Shopping
     * a campaign is contains one or more Sectors..
     *
     * @param requestParams - only profile id
     * @return: the campaign that all his attributes matched to the profile attribute.
     * for example: if campaign #1 contains attribute 1,2 and the profile have the attributes 1,2,3 - the campaign is matched.
     * but the other campaign with attributes 1,2,3,4 - not matched because of the attribute #4 is not in profile attributes.
     * <p>
     * if matched more than one campaign for a profile - the system will be return the campaign with high priority,
     * if have more than one campaign with the same priority - the system will be return the campaign with the lowest campaign id.
     * Constraint - for each campaign - have a max capacity.
     * <p>
     * in case of the max capacity will be arrived
     * - the system will be return the next campaign that matched on the above conditions
     * (matched attributes and highest priority, lowest campaign id, and not reached the max capacity)
     * <p>
     * the max capacity flag required for each profile:
     * for example - if profile #3 arrived the max capacity of the specific campaign -
     * this is not effected on the others profile - in the other words - the max capacity will be managed for each profile.
     * <p>
     * in the part 2 tack - we need to consider that method may be to call for same profile in the same time. = we need to keep all the above conditions.
     */
    private String handleBidRequest(RequestParams requestParams) {

        Integer profileId = requestParams.getProfileId();

        Set<Integer> profileAttributes = dbManager.getProfileAttributes(profileId);
        if (profileAttributes.isEmpty())
            return ResponseTypeEnum.UNMATCHED.getValue();

//        List<Integer> campaignIdListResult = getAllMatchedCampaignIds(profileAttributes);
        Set<Integer> allMatchedCampaignIdsList = getAllMatchedCampaignIdsByMap(profileAttributes);

        if (allMatchedCampaignIdsList.isEmpty())
            return ResponseTypeEnum.UNMATCHED.getValue();


//        String bestCampaignIdResultByPriorityAndLowestId = null;
//        for (BestCampaignSolution bestCampaignSolution : stringBestCampaignSolutionMap.values()) {
//            bestCampaignIdResultByPriorityAndLowestId = bestCampaignSolution.getBestCampaignIdResultByPriorityAndLowestId(profileId, allMatchedCampaignIdsList);
//            System.out.println("bid campaign result: " + bestCampaignIdResultByPriorityAndLowestId);
//        }
        return bestCampaignSolution.getBestCampaignIdResultByPriorityAndLowestId(profileId, allMatchedCampaignIdsList);
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
