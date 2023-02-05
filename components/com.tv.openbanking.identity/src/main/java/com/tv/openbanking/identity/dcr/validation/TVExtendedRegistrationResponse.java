package com.tv.openbanking.identity.dcr.validation;

import com.google.gson.annotations.SerializedName;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationResponse;

public class TVExtendedRegistrationResponse extends RegistrationResponse {
    @SerializedName(value = "logo_uri")
    private String logoUri;

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }
}
