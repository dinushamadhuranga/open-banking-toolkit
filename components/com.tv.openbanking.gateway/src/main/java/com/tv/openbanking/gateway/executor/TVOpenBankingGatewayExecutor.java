package com.tv.openbanking.gateway.executor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Base64;

public class TVOpenBankingGatewayExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(TVOpenBankingGatewayExecutor.class);

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {
        if (!obapiRequestContext.isError()) {
            String authorization = obapiRequestContext.getMsgInfo().getHeaders().get("Authorization");
            String[] tokenStringArray = authorization.split("\\.");
            String base64EncodedBody = tokenStringArray[1];
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String tokenPayloadString = new String(decoder.decode(base64EncodedBody));
            JsonParser jsonParser = new JsonParser();
            JsonObject tokenPayloadJson = (JsonObject)jsonParser.parse(tokenPayloadString);
            if (!tokenPayloadJson.get("tpp_role").toString().contains("AISP")) {
                log.error("Error occurred while validating token roles");
                OpenBankingExecutorError error = new OpenBankingExecutorError("Forbidden", "invalid_scope", "Error occurred while validating roles", "403");
                ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
                executorErrors.add(error);
                obapiRequestContext.setError(true);
                obapiRequestContext.setErrors(executorErrors);
            }
        }
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
