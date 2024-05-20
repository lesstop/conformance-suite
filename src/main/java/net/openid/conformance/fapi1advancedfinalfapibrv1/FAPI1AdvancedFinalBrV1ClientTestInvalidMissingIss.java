package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.as.RemoveIssFromIdToken;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-client-test-invalid-missing-iss",
	displayName = "FAPI1-Advanced-Final-Br-v1: client test - missing iss value in id_token from authorization_endpoint, should be rejected",
	summary = "This test should end with the client displaying an error message that the iss value in the id_token from the authorization_endpoint is missing",
	profile = "FAPI1-Advanced-Final-Br-v1",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.scope",
		"client.redirect_uri",
		"client.certificate",
		"client.jwks"
	}
)

public class FAPI1AdvancedFinalBrV1ClientTestInvalidMissingIss extends AbstractFAPI1AdvancedFinalBrV1ClientExpectNothingAfterIdTokenIssued {

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(RemoveIssFromIdToken.class, "OIDCC-3.1.3.7-2");
	}

	@Override
	protected String getIdTokenFaultErrorMessage() {
		return "missing iss value";
	}


}
