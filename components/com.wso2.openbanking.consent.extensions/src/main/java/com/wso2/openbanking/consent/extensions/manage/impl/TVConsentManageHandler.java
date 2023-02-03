package com.wso2.openbanking.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.DefaultConsentManageHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class TVConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(TVConsentManageHandler.class);
    private static final ConsentCoreService consentCoreService = new ConsentCoreServiceImpl();
    DefaultConsentManageHandler defaultConsentManageHandler = new DefaultConsentManageHandler();

    private static final String ACCOUNT_CONSENT_CREATE_PATH = "account-access-consents";
    private static final String ACCOUNT_CONSENT_GET_PATH = "account-access-consents/";
    private static final String IS_ONE_OFF_CONSENT = "is_one_off_consent";
    private static final String SUPER_USER = "admin@wso2.com@carbon.super";

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
        if (consentManageData.getRequestPath().startsWith(ACCOUNT_CONSENT_GET_PATH)) {
            String consentId = consentManageData.getRequestPath().split(ACCOUNT_CONSENT_GET_PATH)[1];
            try {
                JSONObject payload = (JSONObject) consentManageData.getPayload();
                ConsentResource consent = consentCoreService.getConsent(consentId, true);
                payload.put(IS_ONE_OFF_CONSENT, Boolean.parseBoolean(consent.getConsentAttributes().get(IS_ONE_OFF_CONSENT)));
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
            if (request != null && !(request instanceof JSONArray) && consentManageData.getRequestPath().equals(ACCOUNT_CONSENT_CREATE_PATH)) {
                JSONObject requestObject = (JSONObject) request;
                boolean isValidRequest = true;

                if (requestObject.containsKey("Data") && requestObject.get(IS_ONE_OFF_CONSENT) instanceof Boolean
                        && !Boolean.parseBoolean(requestObject.get(IS_ONE_OFF_CONSENT).toString())) {
                    ArrayList<String> clientIds = new ArrayList<>(Collections.singletonList(consentManageData.getClientId()));
                    ArrayList<String> consentTypes = new ArrayList<>(Collections.singletonList("accounts"));
                    ArrayList<String> consentStatuses = new ArrayList<>(Collections.singletonList("authorised"));
                    ArrayList<String> userIds = new ArrayList<>(Collections.singletonList(SUPER_USER));

                    ArrayList<DetailedConsentResource> detailedConsentResources = consentCoreService.searchDetailedConsents(null, clientIds, consentTypes, consentStatuses, userIds, null, null, null, 0, false);

                    for (DetailedConsentResource detailedConsentResource : detailedConsentResources) {
                        String isOneOffConsent = detailedConsentResource.getConsentAttributes().get(IS_ONE_OFF_CONSENT);
                        if (isOneOffConsent != null && !Boolean.parseBoolean(isOneOffConsent)) {
                            log.error("Only one recurring consents can be created!");
                            isValidRequest = false;
                        }
                    }
                }

                if (!isValidRequest) {
                    log.error("Only one recurring consents can be created!");
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent validation failed");
                }

                JSONObject response = (JSONObject) request;
                String consentID = UUID.randomUUID().toString();
                ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(), requestObject.toJSONString(), "accounts", "awaitingAuthorisation");
                requestedConsent.setConsentID(consentID);
                Map<String, String> consentAttributes = new HashMap<>();
                consentAttributes.put(IS_ONE_OFF_CONSENT, requestObject.get(IS_ONE_OFF_CONSENT).toString());
                requestedConsent.setConsentAttributes(consentAttributes);
                DetailedConsentResource createdConsent = consentCoreService.createAuthorizableConsent(requestedConsent, (String) null, "created", "authorization", true);

                JSONObject data = (JSONObject) response.get("Data");
                data.put("ConsentId", createdConsent.getConsentID());
                response.put("Data", data);
                consentManageData.setResponsePayload(response);
                consentManageData.setResponseStatus(ResponseStatus.CREATED);
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request path invalid");
            }
        } catch (ConsentManagementException e) {
            log.error("Error while validating the request");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent validation failed");
        }
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
