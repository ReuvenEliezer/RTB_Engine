package com.iiq.rtbEngine;

import com.iiq.rtbEngine.controller.MainController;
import com.iiq.rtbEngine.services.DbManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = RtbEngineApplication.class)
//@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RtbEngineApplicationTests {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DbManager dbManager;

    @Test
    void profileAttributeTest() {
        MainController.UrlParam actionType = MainController.UrlParam.ACTION_TYPE;
        MainController.ActionType attributionRequest = MainController.ActionType.ATTRIBUTION_REQUEST;
        MainController.UrlParam profileName = MainController.UrlParam.PROFILE_ID;
        MainController.UrlParam attributeName = MainController.UrlParam.ATTRIBUTE_ID;

        Integer attributeId = 4;
        Integer profileId = 3;

        String result = restTemplate.getForObject("http://localhost:8080/api?" + actionType.getValue() + "=" + attributionRequest.getId() + "&" +
                profileName.getValue() + "=" + profileId + "&" + attributeName.getValue() + "=" + attributeId, String.class);
        Assertions.assertNull(result);
    }

    @Test
    void bidTest() {
        MainController.UrlParam actionType = MainController.UrlParam.ACTION_TYPE;
        MainController.ActionType bidRequest = MainController.ActionType.BID_REQUEST;
        MainController.UrlParam profileName = MainController.UrlParam.PROFILE_ID;

        Integer profileId = 3;

        String result = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("unmatched", result);
    }


    @Test
    void profileAttributeUnmatchedTest() {
        dbManager.updateProfileAttribute(3, 20);
        dbManager.updateProfileAttribute(3, 21);

        MainController.UrlParam actionType = MainController.UrlParam.ACTION_TYPE;
        MainController.ActionType bidRequest = MainController.ActionType.BID_REQUEST;
        MainController.UrlParam profileName = MainController.UrlParam.PROFILE_ID;

        Integer profileId = 3;
        String result = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("unmatched", result);
    }

    @Test
    void bidPart2_Test() {
        dbManager.updateProfileAttribute(3, 20);
        dbManager.updateProfileAttribute(3, 21);
        dbManager.updateProfileAttribute(3, 22);
        dbManager.updateProfileAttribute(3, 220);

        dbManager.updateProfileAttribute(4, 20);
        dbManager.updateProfileAttribute(4, 21);
        dbManager.updateProfileAttribute(4, 22);
        dbManager.updateProfileAttribute(4, 220);
        MainController.UrlParam actionType = MainController.UrlParam.ACTION_TYPE;
        MainController.ActionType bidRequest = MainController.ActionType.BID_REQUEST;
        MainController.UrlParam profileName = MainController.UrlParam.PROFILE_ID;

        Integer profileId = 3;
        String result = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", result);
        String result1 = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", result1);
        String result2 = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("capped", result2, "the max capacity is reached");

        profileId = 4;
        String resultProfile4 = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", resultProfile4);
        String resultProfile41 = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", resultProfile41);
        String resultProfile42 = restTemplate.getForObject(String.format("http://localhost:8080/api?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("capped", resultProfile42, "the max capacity is reached");
    }

}
