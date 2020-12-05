package com.iiq.rtbEngine.services;

import com.iiq.rtbEngine.models.RequestParams;

@FunctionalInterface
public interface ActionHandler {
    String doAction(RequestParams requestParams);
}
