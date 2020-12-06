package com.iiq.rtbEngine.models;

import com.iiq.rtbEngine.controller.MainController;

import java.util.HashMap;
import java.util.Map;

public enum ActionTypeEnum {

    ATTRIBUTION_REQUEST(0),
    BID_REQUEST(1),
    ;

    private int id;
    private static Map<Integer, ActionTypeEnum> idToRequestMap = new HashMap<>();

    static {
        for (ActionTypeEnum actionType : ActionTypeEnum.values())
            idToRequestMap.put(actionType.getId(), actionType);
    }

    public int getId() {
        return this.id;
    }

    ActionTypeEnum(int id) {
        this.id = id;
    }

    public static ActionTypeEnum getActionTypeById(int id) {
        return idToRequestMap.get(id);
    }

}
