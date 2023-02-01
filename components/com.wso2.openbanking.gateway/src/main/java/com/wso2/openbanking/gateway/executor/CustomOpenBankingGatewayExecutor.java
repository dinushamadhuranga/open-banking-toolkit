/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.gateway.executor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;

/**
 * API Resource Access Validation executor
 * This executor validates the grant type
 */
public class CustomOpenBankingGatewayExecutor implements OpenBankingGatewayExecutor {

    private static final Log log = LogFactory.getLog(CustomOpenBankingGatewayExecutor.class);

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
            if (!tokenPayloadJson.get("scope").toString().contains("accounts")) {
                log.error("Error occurred while validating token scopes");
                this.handleBadRequestError(obapiRequestContext, "Error occurred while validating scopes");
            }

            JsonObject requestPayloadJson = (JsonObject) jsonParser.parse(obapiRequestContext.getRequestPayload());
            String expDateVal = requestPayloadJson.get("ExpirationDateTime").toString();
            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
            OffsetDateTime currDate = OffsetDateTime.now(expDate.getOffset());
            if (expDate.compareTo(currDate) > 0) {
                log.error("Provided expiry date is: " + expDate + " current date is: " + currDate);
                this.handleBadRequestError(obapiRequestContext, "Error occurred while validating ExpirationDateTime");
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

    private void handleBadRequestError(OBAPIRequestContext obapiRequestContext, String message) {
        OpenBankingExecutorError error = new OpenBankingExecutorError("Forbidden", "invalid_scope", message, "403");
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        obapiRequestContext.setError(true);
        obapiRequestContext.setErrors(executorErrors);
    }
}
