package com.wso2.openbanking.gateway.executor;

import com.wso2.openbanking.accelerator.gateway.executor.core.DefaultRequestRouter;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;

import java.util.List;

public class TVRequestRouter extends DefaultRequestRouter {

    @Override
    public List<OpenBankingGatewayExecutor> getExecutorsForRequest(OBAPIRequestContext requestContext) {
        if (requestContext.getMsgInfo().getResource().contains("/account-access-consents")) {
            return (List)this.getExecutorMap().get("Consent");
        } else if (requestContext.getMsgInfo().getResource().contains("/register")){
            return   (List)this.getExecutorMap().get("DCR");
        }else {
            return (List)this.getExecutorMap().get("Default");
        }
    }
}
