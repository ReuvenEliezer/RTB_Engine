package com.iiq.rtbEngine.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiq.rtbEngine.db.CampaignsConfigDao;
import com.iiq.rtbEngine.db.CampaignsDao;
import com.iiq.rtbEngine.db.ProfilesDao;
import com.iiq.rtbEngine.models.CampaignConfig;
import com.iiq.rtbEngine.util.FilesUtil;


@Service
public class DbManager {

    @Autowired
    CampaignsConfigDao campaignsConfigDao;

    @Autowired
    CampaignsDao campaignsDao;

    @Autowired
    ProfilesDao profilesDao;

    private static final String CSV_DELIM = ",";
    private static final String BASE_PATH = "./";
    private static final String CAMPAIGNS_TABLE_INITIALIZATION_FILE = "campaigns_init.csv";
    private static final String CAMPAIGNS_CAPACITY_TABLE_INITIALIZATION_FILE = "campaign_config_init.csv";

    @PostConstruct
    public void init() {
        //populate Campaigns table from file
        initCampaignsTable();

        //populate Campaign capacity table from file
        initCampaignCapacityTable();

        //init Profile table
        initProfilesTable();

        iniCampaignsToAttributesMap();
    }

//    public Map<List<Integer>, Set<Integer>> attributesToCampaignsMap = new ConcurrentHashMap<>();
//
//    public Set<Integer> getCampaigns(Set<Integer> profileAttributes) {
//        List<Integer> profileAttributesSortedList = sortSet(profileAttributes);
//        return attributesToCampaignsMap.get(profileAttributesSortedList);
//    }
//
//    private List<Integer> sortSet(Set<Integer> profileAttributes) {
//        return profileAttributes.stream()
//                .sorted()
//                .collect(Collectors.toList());
//    }
//
//    private void iniCampaignsToAttributesMap() {
//        Map<Integer, List<Integer>> allCampaignAttributes = getAllCampaignAttributes();
//        for (Map.Entry<Integer, List<Integer>> entry : allCampaignAttributes.entrySet()) {
//            Collections.sort(entry.getValue());
//            Set<Integer> orDefault = attributesToCampaignsMap.getOrDefault(entry.getValue(), new HashSet<>());
//            orDefault.add(entry.getKey());
//            attributesToCampaignsMap.put(entry.getValue(), orDefault);
//        }
//    }

    public Map<Integer, Set<Integer>> attributeToCampaignsMap = new ConcurrentHashMap<>();

    public Set<Integer> getCampaigns(Set<Integer> profileAttributes) {
        Map<Integer, Set<Integer>> result = new HashMap<>();
        Iterator<Integer> attributesIterator = profileAttributes.iterator();
        while (attributesIterator.hasNext()) {
            Integer attribute = attributesIterator.next();
            Set<Integer> matchedCampaign = attributeToCampaignsMap.get(attribute);
            result.put(attribute, matchedCampaign);
        }

        Set<Integer> matchedCampaignsResult = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> entry : result.entrySet()) {
            if (entry.getValue() != null)
                matchedCampaignsResult.addAll(entry.getValue());
        }


        Set<Integer> finalCampaignIds = new HashSet<>(matchedCampaignsResult);
        for (Integer campaignId : matchedCampaignsResult) {
            Set<Integer> campaignAttributes = getCampaignAttributes(campaignId);
            if (!isAttributesMatched(profileAttributes, campaignAttributes)) {
                finalCampaignIds.remove(campaignId);
            }
        }
        return finalCampaignIds;
    }

    private boolean isAttributesMatched(Set<Integer> profileAttributes, Set<Integer> campaignAttributes) {
        if (campaignAttributes == null || campaignAttributes.isEmpty()) return false;
        for (Integer integer : campaignAttributes) {
            if (!profileAttributes.contains(integer)) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> sortSet(Set<Integer> profileAttributes) {
        return profileAttributes.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    private void iniCampaignsToAttributesMap() {
        Map<Integer, List<Integer>> allCampaignAttributes = getAllCampaignAttributes();
        for (Map.Entry<Integer, List<Integer>> entry : allCampaignAttributes.entrySet()) {
            for (Integer attribute : entry.getValue()) {
                Set<Integer> campaigns = attributeToCampaignsMap.getOrDefault(attribute, new HashSet<>());
                campaigns.add(entry.getKey());
                attributeToCampaignsMap.put(attribute, campaigns);
            }
        }
    }


    private void initCampaignsTable() {
        // create table in DB
        campaignsDao.createTable();

        // read initialization file
        List<String> lines = FilesUtil.readLinesFromFile(BASE_PATH + CAMPAIGNS_TABLE_INITIALIZATION_FILE);

        // insert campaigns capacities into DB
        for (String line : lines) {
            String[] values = line.split(CSV_DELIM);
            campaignsDao.updateTable(values[0], values[1]);
        }
    }

    private void initCampaignCapacityTable() {

        // create table in DB
        campaignsConfigDao.createTable();

        // read initialization file
        List<String> lines = FilesUtil.readLinesFromFile(BASE_PATH + CAMPAIGNS_CAPACITY_TABLE_INITIALIZATION_FILE);

        // insert campaigns capacities into DB
        for (String line : lines) {
            String[] values = line.split(CSV_DELIM);
            campaignsConfigDao.updateTable(values[0], values[1], values[2]);
        }
    }

    private void initProfilesTable() {
        // create table in DB
        profilesDao.createTable();
    }

    /*****************************************************************************************************************************************
     ****************************************						DEVELOPER API							*********************************
     ****************************************************************************************************************************************/

    /**
     * An API for getting the capacity that is configured for a certain campaign
     *
     * @param campaignId
     * @return the capacity configured for the given campaign, or null in case the given campaignId does not
     * have any campaign configuration
     */
    public Integer getCampaignCapacity(int campaignId) {
        return campaignsConfigDao.getCampaignCapacity(campaignId);
    }

    /**
     * An API for getting the priority that is configured for a certain campaign
     *
     * @param campaignId
     * @return the priority configured for the given campaign, or null in case the given campaignId does not
     * have any campaign configuration
     */
    public Integer getCampaignPriority(int campaignId) {
        return campaignsConfigDao.getCampaignPriority(campaignId);
    }

    /**
     * An API for getting all the attributes that a certain campaign is targeting
     *
     * @param campaignId
     * @return a set of all the attributes that the given campaign targets, or an empty Set if the given
     * campaignId does not have any campaign configuration
     */
    public Set<Integer> getCampaignAttributes(int campaignId) {
        return campaignsDao.getCampaignAttributes(campaignId);
    }

    /**
     * An API for getting configuration object for a certain campaign
     *
     * @param campaignId
     * @return a CampaignConfig object containing the configuration entities for the given campaignId,
     * or null in case campaignId does not have any campaign configuration
     */
    public CampaignConfig getCampaignConfig(int campaignId) {
        return campaignsConfigDao.getCampaignConfig(campaignId);
    }

    /**
     * An API for getting all the attributes that match a certain profile
     *
     * @param profileId
     * @return a set of all the profile IDs that match the given profile, or an empty set in case the given profile
     * does not have any attribute IDs that match
     */
    public Set<Integer> getProfileAttributes(int profileId) {
        return profilesDao.getProfileAttributes(profileId);
    }

    /**
     * An API for updating Profiles table in DB
     *
     * @param profileId
     * @param attributeId
     */
    public void updateProfileAttribute(int profileId, int attributeId) {
        profilesDao.updateTable(profileId + "", attributeId + "");
    }

    /**
     * An API for retrieving from DB all the campaign attributes for every campaign.
     *
     * @return a Map s.t. key = campaign ID, value = set of campaign attribute IDs
     */
    public Map<Integer, List<Integer>> getAllCampaignAttributes() {
        return campaignsDao.getAllCampaignAttributes();
    }

    /**
     * An API for retrieving from DB the campaign configuration for every campaign.
     *
     * @return a Map s.t. key = campaign ID, value = campaign configuration object
     */
    public Map<Integer, CampaignConfig> getAllCampaignsConfigs() {
        return campaignsConfigDao.getAllCampaignsConfigs();
    }

}
