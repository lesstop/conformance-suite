package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.as.AddInvalidCHashValueToIdToken;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantNotApplicable;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-client-test-invalid-chash",
	displayName = "FAPI1-Advanced-Final-Br-v1: client test - invalid c_hash in id_token from authorization_endpoint, should be rejected",
	summary = "This test should end with the client displaying an error message that the c_hash value in the id_token from the authorization_endpoint does not match the c_hash value in the request object",
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
@VariantNotApplicable(parameter = FAPIResponseMode.class, values = {"jarm"})
public class FAPI1AdvancedFinalBrV1ClientTestInvalidCHash extends AbstractFAPI1AdvancedFinalBrV1ClientExpectNothingAfterIdTokenIssued {

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(AddInvalidCHashValueToIdToken.class, "OIDCC-3.3.2-10");
	}

	@Override
	protected String getIdTokenFaultErrorMessage() {
		return "invalid c_hash value";
	}
}
