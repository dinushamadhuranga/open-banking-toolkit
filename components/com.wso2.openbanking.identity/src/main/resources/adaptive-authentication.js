var psuChannel = 'Online Banking';

var onLoginRequest = function(context) {
    publishAuthData(context, "AuthenticationAttempted", {
        'psuChannel': psuChannel
    });
    executeStep(1, {
        onSuccess: function(context) {
            Log.info("Authentication Successful");
            publishAuthData(context, "AuthenticationSuccessful", {
                'psuChannel': psuChannel
            });
            OTPFlow(context);
        },
        onFail: function(context) {
            Log.info("Authentication Failed");
            publishAuthData(context, "AuthenticationFailed", {
                'psuChannel': psuChannel
            });
        }
    });
};

var OTPFlow = function(context) {
    executeStep(2, {
        //OTP-authentication
        onSuccess: function(context) {
            context.selectedAcr = "urn:openbanking:psd2:sca";
            publishAuthData(context, "AuthenticationSuccessful", {
                'psuChannel': psuChannel
            });
        },
        onFail: function(context) {
            publishAuthData(context, "AuthenticationFailed", {
                'psuChannel': psuChannel
            });
            OTPFlow(context);
        }
    });
};