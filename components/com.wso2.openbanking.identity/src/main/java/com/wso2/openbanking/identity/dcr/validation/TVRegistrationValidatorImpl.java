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

package com.wso2.openbanking.identity.dcr.validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.utils.ValidatorUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DefaultRegistrationValidatorImpl;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Sample implementation for dcr registration VALIDATOR class
 */
public class TVRegistrationValidatorImpl extends DefaultRegistrationValidatorImpl {

    private static final String LOGO_URI = "logo_uri";

    @Override
    public void validatePost(RegistrationRequest registrationRequest) throws DCRValidationException {
        String logoUri = null;
        if (registrationRequest.getSsaParameters().containsKey(LOGO_URI)) {
            logoUri = registrationRequest.getSsaParameters().get(LOGO_URI).toString();
        }

        if (logoUri == null || !this.isValidUrl(logoUri)) {
            throw new DCRValidationException(DCRCommonConstants.INVALID_META_DATA, "Invalid client meta data sent for logo_uri", "logo_uri sent is not allowed", new Throwable());
        }
    }

    @Override
    public String getRegistrationResponse(Map<String, Object> spMetaData) {
        if (Boolean.TRUE.equals(IdentityCommonUtil.getDCRModifyResponseConfig())) {
            String tlsCert = spMetaData.get("tls_cert").toString();
            String clientId = spMetaData.get("client_id").toString();
            if (!spMetaData.containsKey("registration_access_token")) {
                spMetaData.put("registration_access_token", ValidatorUtils.generateAccessToken(clientId, tlsCert));
            }

            spMetaData.put("registration_client_uri", ValidatorUtils.getRegistrationClientURI() + clientId);
        }

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(spMetaData);
        TVExtendedRegistrationResponse registrationResponse = gson.fromJson(jsonElement, TVExtendedRegistrationResponse.class);
        return gson.toJson(registrationResponse);
    }

    private boolean isValidUrl(String url){
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
