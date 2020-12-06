package com.iiq.rtbEngine.db;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProfilesDao {

    @Autowired
    private H2DB h2Db;

    @Resource
    private ProfilesDao self;

    private static final String CACHE_NAMES = "PROFILES";

    @CacheEvict(allEntries = true, cacheNames = {CACHE_NAMES})
    public void cacheEvict() {
        System.out.println("clearing ProfilesDao cache");
    }

    private static final String PROFILES_TABLE_NAME = "profiles";

    private static final String ATTRIBUTE_ID_COLUMN = "attribute_id";
    private static final String PROFILE_ID_COLUMN = "profile_id";
    private static final String CREATE_PROFILES_TABLE = "CREATE TABLE " + PROFILES_TABLE_NAME +
            "(" + PROFILE_ID_COLUMN + " INTEGER not NULL, " +
            ATTRIBUTE_ID_COLUMN + " INTEGER, " +
            " PRIMARY KEY ( " + PROFILE_ID_COLUMN + ", " + ATTRIBUTE_ID_COLUMN + "))";
    private static final String UPDATE_STATMENT = "MERGE INTO " + PROFILES_TABLE_NAME + " KEY(" + PROFILE_ID_COLUMN + ", " + ATTRIBUTE_ID_COLUMN + ") VALUES (%s, %s)";
    private static final String SELECT_STATEMENT = "SELECT * FROM " + PROFILES_TABLE_NAME + " where " + PROFILE_ID_COLUMN + " = %s";

    public void createTable() {
        try {
            h2Db.executeUpdate(CREATE_PROFILES_TABLE);
            self.cacheEvict();
        } catch (SQLException e) {
            System.out.println("Error while trying to create table " + PROFILES_TABLE_NAME);
            e.printStackTrace();
        }
    }

    private Map<Integer, Object> profileToLockMap = new ConcurrentHashMap<>();

    public Object getLockProfile(Integer profileId) {
        if (!profileToLockMap.containsKey(profileId))
            profileToLockMap.put(Integer.valueOf(profileId), new Object());
        return profileToLockMap.get(profileId);
    }

    public void updateTable(String profileId, String attributeId) {
        try {
            h2Db.executeUpdate(String.format(UPDATE_STATMENT, profileId, attributeId, profileId, attributeId));
            profileToLockMap.putIfAbsent(Integer.valueOf(profileId), new Object());
            self.cacheEvict();
        } catch (SQLException e) {
            System.out.println("Error while trying to update table " + PROFILES_TABLE_NAME + " with profileId=" + profileId + " attributeId" + attributeId);
            e.printStackTrace();
        }
    }

    @Cacheable(cacheNames = {CACHE_NAMES})
    public Set<Integer> getProfileAttributes(int profileId) {
        System.out.println("getProfileAttributes for profileId: " + profileId);

        Set<Integer> attributes = new HashSet<>();
        try {
            List<Map<String, String>> result = h2Db.executeQuery(String.format(SELECT_STATEMENT, profileId + ""), ATTRIBUTE_ID_COLUMN);
            if (result == null)
                return attributes;

            for (Map<String, String> row : result) {
                Integer attributeId = Integer.parseInt(row.get(ATTRIBUTE_ID_COLUMN));
                attributes.add(attributeId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attributes;
    }
}
