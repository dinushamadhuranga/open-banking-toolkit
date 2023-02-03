package com.wso2.openbanking.consent.extensions.authorize.impl;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.DefaultConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TVConsentPersistStep implements ConsentPersistStep {

    private static final Log logger = LogFactory.getLog(TVConsentPersistStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {
        DefaultConsentPersistStep defaultConsentPersistStep = new DefaultConsentPersistStep();
        defaultConsentPersistStep.execute(consentPersistData);

        if (consentPersistData.getApproval()) {
            try {
                //
                URL url = new URL("http://localhost:8080/send-otp-notification");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Accept", "application/json");

                if (httpURLConnection.getResponseCode() != 200) {
                    throw new OpenBankingException("Failed sending SMS OTP : "
                            + httpURLConnection.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (httpURLConnection.getInputStream())));

                String response;
                if (logger.isDebugEnabled()) {
                    logger.debug("Response from SMS Server.... \n");
                    while ((response = br.readLine()) != null) {
                        logger.debug(response);
                    }
                }
                httpURLConnection.disconnect();
            } catch (IOException | OpenBankingException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
            }
        }
    }
}
