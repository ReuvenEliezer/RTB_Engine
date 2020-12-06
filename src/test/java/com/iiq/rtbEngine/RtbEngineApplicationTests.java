package com.iiq.rtbEngine;

import com.iiq.rtbEngine.models.ActionTypeEnum;
import com.iiq.rtbEngine.models.ResponseTypeEnum;
import com.iiq.rtbEngine.models.UrlParamEnum;
import com.iiq.rtbEngine.services.DbManager;
import com.iiq.rtbEngine.util.WsAddressConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

//import org.junit.runner.RunWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = RtbEngineApplication.class)
//@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RtbEngineApplicationTests {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private DbManager dbManager;

    @Test
    void profileAttributeTest() {
        UrlParamEnum actionType = UrlParamEnum.ACTION_TYPE;
        ActionTypeEnum attributionRequest = ActionTypeEnum.ATTRIBUTION_REQUEST;
        UrlParamEnum profileName = UrlParamEnum.PROFILE_ID;
        UrlParamEnum attributeName = UrlParamEnum.ATTRIBUTE_ID;

        Integer attributeId = 4;
        Integer profileId = 3;

        String result = restTemplate.getForObject(WsAddressConstants.apiFullUrl + "?" + actionType.getValue() + "=" + attributionRequest.getId() + "&" +
                profileName.getValue() + "=" + profileId + "&" + attributeName.getValue() + "=" + attributeId, String.class);
        Assertions.assertNull(result);
    }

    @Test
    void bidTest() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String s : cacheNames) {
            cacheManager.getCache(s).clear();

        }
        UrlParamEnum actionType = UrlParamEnum.ACTION_TYPE;
        ActionTypeEnum bidRequest = ActionTypeEnum.BID_REQUEST;
        UrlParamEnum profileName = UrlParamEnum.PROFILE_ID;

        Integer profileId = 3;

        String result = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals(ResponseTypeEnum.UNMATCHED.getValue(), result);
    }


    @Test
    void profileAttributeUnmatchedTest() {
        dbManager.updateProfileAttribute(3, 20);
        dbManager.updateProfileAttribute(3, 21);

        UrlParamEnum actionType = UrlParamEnum.ACTION_TYPE;
        ActionTypeEnum bidRequest = ActionTypeEnum.BID_REQUEST;
        UrlParamEnum profileName = UrlParamEnum.PROFILE_ID;

        Integer profileId = 3;
        String result = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals(ResponseTypeEnum.UNMATCHED.getValue(), result);
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
        UrlParamEnum actionType = UrlParamEnum.ACTION_TYPE;
        ActionTypeEnum bidRequest = ActionTypeEnum.BID_REQUEST;
        UrlParamEnum profileName = UrlParamEnum.PROFILE_ID;

        Integer profileId = 3;
        String result = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", result);
        String result1 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", result1);
        String result2 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", result2);
        String result3 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals(ResponseTypeEnum.CAPPED.getValue(), result3, "the max capacity is reached");

        profileId = 4;
        String resultProfile4 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", resultProfile4);
        String resultProfile41 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", resultProfile41);
        String resultProfile42 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals("103", resultProfile42);
        String resultProfile43 = restTemplate.getForObject(String.format(WsAddressConstants.apiFullUrl +"?%s=%s&%s=%s", actionType.getValue(), bidRequest.getId(), profileName.getValue(), profileId), String.class);
        Assertions.assertEquals(ResponseTypeEnum.CAPPED.getValue(), resultProfile43, "the max capacity is reached");
    }

}
