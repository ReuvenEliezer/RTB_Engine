package com.iiq.rtbEngine.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.iiq.rtbEngine.services.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.iiq.rtbEngine.models.CampaignConfig;

import javax.annotation.Resource;

@Component
public class CampaignsConfigDao {
    @Autowired
    private H2DB h2Db;

    @Resource
    private CampaignsConfigDao self;

    private static final String CACHE_NAMES = "CAMPAIGN_CONFIG";

    @CacheEvict(allEntries = true, cacheNames = {CACHE_NAMES})
    public void cacheEvict() {
        System.out.println("clearing CampaignsConfigDao cache");
    }

    private static final String CAMPAIGN_CONFIG_TABLE_NAME = "campaign_config";

    private static final String CAMPAIGN_ID_COLUMN = "campaign_id";
    private static final String CAPACITY_COLUMN = "capacity";
    private static final String PRIORITY_COLUMN = "priority";
    private static final String CREATE_CAMPAIGN_CONFIG_TABLE = "CREATE TABLE " + CAMPAIGN_CONFIG_TABLE_NAME +
            "(" + CAMPAIGN_ID_COLUMN + " INTEGER not NULL, " +
            " " + CAPACITY_COLUMN + " INTEGER, " +
            " " + PRIORITY_COLUMN + " INTEGER, " +
            " PRIMARY KEY ( " + CAMPAIGN_ID_COLUMN + " ))";
    private static final String UPDATE_STATMENT = "INSERT INTO " + CAMPAIGN_CONFIG_TABLE_NAME + " VALUES (%s, %s, %s)";
    private static final String SELECT_CONFIG_STATEMENT = "SELECT * FROM " + CAMPAIGN_CONFIG_TABLE_NAME + " where " + CAMPAIGN_ID_COLUMN + " = %s";
    private static final String SELECT_ALL_CONFIGS_STATEMENT = "SELECT * FROM " + CAMPAIGN_CONFIG_TABLE_NAME;

    public void createTable() {
        try {
            h2Db.executeUpdate(CREATE_CAMPAIGN_CONFIG_TABLE);
            self.cacheEvict();
        } catch (SQLException e) {
            System.out.println("Error while trying to create table " + CAMPAIGN_CONFIG_TABLE_NAME);
            e.printStackTrace();
        }
    }

    public void updateTable(String campaignId, String capacity, String priority) {
        try {
            h2Db.executeUpdate(String.format(UPDATE_STATMENT, campaignId, capacity, priority));
            self.cacheEvict();
        } catch (SQLException e) {
            System.out.println("Error while trying to update table " + CAMPAIGN_CONFIG_TABLE_NAME + " with campaignId=" + campaignId + " capacity" + capacity + " priority=" + priority);
            e.printStackTrace();
        }
    }

    @Cacheable(cacheNames = {CACHE_NAMES})
    public Integer getCampaignCapacity(int campaignId) {
        System.out.println("getCampaignCapacity for campaignId: "+campaignId);

        Integer capacity = null;
        try {
            List<Map<String, String>> result = h2Db.executeQuery(String.format(SELECT_CONFIG_STATEMENT, campaignId + ""), CAPACITY_COLUMN);
            if (result == null)
                return null;

            for (Map<String, String> row : result)
                capacity = Integer.parseInt(row.get(CAPACITY_COLUMN));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return capacity;
    }

    @Cacheable(cacheNames = {CACHE_NAMES})
    public Integer getCampaignPriority(int campaignId) {
        System.out.println("getCampaignPriority for campaignId: "+campaignId);
        Integer capacity = null;
        try {
            List<Map<String, String>> result = h2Db.executeQuery(String.format(SELECT_CONFIG_STATEMENT, campaignId + ""), PRIORITY_COLUMN);
            if (result == null)
                return null;

            for (Map<String, String> row : result)
                capacity = Integer.parseInt(row.get(PRIORITY_COLUMN));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return capacity;
    }

    public CampaignConfig getCampaignConfig(int campaignId) {
        try {
            List<Map<String, String>> result = h2Db.executeQuery(SELECT_CONFIG_STATEMENT, PRIORITY_COLUMN, CAPACITY_COLUMN);
            if (result == null)
                return null;

            for (Map<String, String> row : result) {
                Integer priority = Integer.parseInt(row.get(PRIORITY_COLUMN));
                Integer capacity = Integer.parseInt(row.get(CAPACITY_COLUMN));
                return new CampaignConfig(priority, capacity);
            }
        } catch (Exception e) {
            System.out.println("Error while trying to retrieve campaign configurations from DB for campaign " + campaignId);
            e.printStackTrace();
        }
        return null;
    }

	public Map<Integer, CampaignConfig> getAllCampaignsConfigs() {
        Map<Integer, CampaignConfig> res = new HashMap<>();

        try {
            List<Map<String, String>> result = h2Db.executeQuery(SELECT_ALL_CONFIGS_STATEMENT, CAMPAIGN_ID_COLUMN, PRIORITY_COLUMN, CAPACITY_COLUMN);
            if (result == null)
                return null;

            for (Map<String, String> row : result) {
                Integer campaignId = Integer.parseInt(row.get(CAMPAIGN_ID_COLUMN));
                Integer priority = Integer.parseInt(row.get(PRIORITY_COLUMN));
                Integer capacity = Integer.parseInt(row.get(CAPACITY_COLUMN));
                res.put(campaignId, new CampaignConfig(priority, capacity));
            }
        } catch (Exception e) {
            System.out.println("Error while trying to retrieve all campaigns configurations from DB");
            e.printStackTrace();
        }

        return res;
    }
}
