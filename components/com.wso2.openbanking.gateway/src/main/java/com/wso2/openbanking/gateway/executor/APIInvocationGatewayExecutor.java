package com.wso2.openbanking.gateway.executor;

import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;

import java.util.Map;

public class APIInvocationGatewayExecutor implements OpenBankingGatewayExecutor {
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {
        boolean isUserPresent = obapiRequestContext.getMsgInfo().getHeaders().containsKey("PSU-IP-Address");
        Map<String, Object> analyticsData = obapiRequestContext.getAnalyticsData();
        analyticsData.put("user-present", isUserPresent);
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {
    }

    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {
    }
}
