/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */
package com.wso2.openbanking.identity.listener.application;

import com.wso2.openbanking.accelerator.common.config.TextFileReader;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import com.wso2.openbanking.accelerator.identity.listener.application.ApplicationUpdaterImpl;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TVApplicationUpdaterImpl extends ApplicationUpdaterImpl {

    @Override
    public void publishData(Map<String, Object> spMetaData, OAuthConsumerAppDTO oAuthConsumerAppDTO)
            throws OpenBankingException {
        Map<String, Object> dcrData = new HashMap<>();
        dcrData.put("applicationName", oAuthConsumerAppDTO.getApplicationName());
        OBDataPublisherUtil.publishData("DCRInputStream", "1.0.0", dcrData);
    }

    @Override
    public void setConditionalAuthScript(boolean isRegulatoryApp, ServiceProvider serviceProvider, LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig) throws OpenBankingException {
        if (isRegulatoryApp && localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig() == null) {
            TextFileReader textFileReader = TextFileReader.getInstance();

            try {
                String authScript = textFileReader.readFile("adaptive-authentication.js");
                if (StringUtils.isNotEmpty(authScript)) {
                    AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
                    scriptConfig.setContent(authScript);
                    scriptConfig.setEnabled(true);
                    localAndOutboundAuthenticationConfig.setAuthenticationScriptConfig(scriptConfig);
                }
            } catch (IOException var7) {
                throw new OpenBankingException("Error occurred while reading file", var7);
            }
        }
    }
}
