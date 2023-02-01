/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 * For specific language governing the permissions and limitations under this
 * license, please see the license as well as any agreement you’ve entered into
 * with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.DefaultConsentManageHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;

/**
 * Consent Manage handler implementation for
 */
public class CustomConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(CustomConsentManageHandler.class);
    private static final ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();
    DefaultConsentManageHandler defaultConsentManageHandler = new DefaultConsentManageHandler();

    @Override
    public void handleFileUploadPost(ConsentManageData consentManageData) throws ConsentException {
        defaultConsentManageHandler.handleFileUploadPost(consentManageData);
    }

    @Override
    public void handleFileGet(ConsentManageData consentManageData) throws ConsentException {
        defaultConsentManageHandler.handleFileGet(consentManageData);
    }

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {
        defaultConsentManageHandler.handlePost(consentManageData);
        if (consentManageData.getRequestPath().startsWith("account-access-consents/")) {
            String consentId = consentManageData.getRequestPath().split("account-access-consents/")[1];
            try {
                JSONObject payload = (JSONObject) consentManageData.getPayload();
                ConsentResource consent = consentCoreService.getConsent(consentId, true);
                payload.put("is_one_off", Boolean.parseBoolean(consent.getConsentAttributes().get("is_one_off")));
                consentManageData.setResponsePayload(payload);
            } catch (ConsentManagementException var6) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, var6.getMessage());
            }
        }
    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {
        try {
            Object request = consentManageData.getPayload();
            if (request != null && !(request instanceof JSONArray) && consentManageData.getRequestPath().equals("account-access-consents")) {
                JSONObject requestObject = (JSONObject) request;
                boolean isValidRequest = true;

                if (requestObject.containsKey("Data") && requestObject.get("is_one_off_consent") instanceof Boolean
                        && !Boolean.parseBoolean(requestObject.get("is_one_off_consent").toString())) {
                    ArrayList<String> consentIdByConsentAttributeNameAndValue = null;

                    consentIdByConsentAttributeNameAndValue = consentCoreService.getConsentIdByConsentAttributeNameAndValue("is_one_off_consent", "false");

                    if (!consentIdByConsentAttributeNameAndValue.isEmpty()) {
                        log.error("Only one recurring consents can be created!");
                        isValidRequest = false;
                    }
                }

                if (isValidRequest) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent validation failed");
                }
            }
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent validation failed");
        }
        defaultConsentManageHandler.handlePost(consentManageData);
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {
        defaultConsentManageHandler.handleDelete(consentManageData);
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {
        defaultConsentManageHandler.handlePut(consentManageData);
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {
        defaultConsentManageHandler.handlePatch(consentManageData);
    }
}
