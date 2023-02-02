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

package com.wso2.openbanking.consent.extensions.authservlet;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.OBDefaultAuthServletImpl;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class TVOBAuthServletImpl implements OBAuthServletInterface {

    OBDefaultAuthServletImpl obDefaultAuthServlet = new OBDefaultAuthServletImpl();

    private static final String LOGO_URI = "logo_uri";
    private static final String TENANT_DOMAIN = "carbon.super";

    @Override
    public Map<String, Object> updateRequestAttribute(HttpServletRequest httpServletRequest, JSONObject jsonObject,
                                                      ResourceBundle resourceBundle) {
        Map<String, Object> returnMaps = obDefaultAuthServlet.updateRequestAttribute(httpServletRequest, jsonObject, resourceBundle);
        try {
            ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
            ServiceProvider serviceProvider = applicationManagementService.getServiceProvider(jsonObject.get("application").toString(), TENANT_DOMAIN);
            Optional<ServiceProviderProperty> optionalLogoUri = Arrays.stream(serviceProvider.getSpProperties())
                    .filter(serviceProviderProperty -> serviceProviderProperty.getName().equals(LOGO_URI))
                    .findFirst();
            optionalLogoUri.ifPresent(serviceProviderProperty -> returnMaps.put(LOGO_URI, serviceProviderProperty.getValue()));
            return returnMaps;
        } catch (IdentityApplicationManagementException e) {
            throw new OpenBankingRuntimeException(ResponseStatus.INTERNAL_SERVER_ERROR.toString(), e);
        }
    }

    @Override
    public Map<String, Object> updateSessionAttribute(HttpServletRequest httpServletRequest, JSONObject jsonObject,
                                                      ResourceBundle resourceBundle) {
        return obDefaultAuthServlet.updateSessionAttribute(httpServletRequest, jsonObject, resourceBundle);
    }

    @Override
    public Map<String, Object> updateConsentData(HttpServletRequest httpServletRequest) {
        return obDefaultAuthServlet.updateConsentData(httpServletRequest);
    }

    @Override
    public Map<String, String> updateConsentMetaData(HttpServletRequest httpServletRequest) {
        return obDefaultAuthServlet.updateConsentMetaData(httpServletRequest);
    }

    @Override
    public String getJSPPath() {
        return obDefaultAuthServlet.getJSPPath();
    }
}
