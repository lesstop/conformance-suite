package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.as.AddInvalidOpenBankingIntentIdToIdToken;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.variant.VariantNotApplicable;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-client-test-invalid-openbanking-intent-id",
	displayName = "FAPI1-Advanced-Final-Br-v1: client test  - invalid openbanking_intent_id, should be rejected",
	summary = "This test should end with the client displaying an error message that the openbanking_intent_id returned in id_token from authorization endpoint does not match the value sent in the request object",
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
@VariantNotApplicable(parameter = FAPI1FinalOPProfile.class, values = { "plain_fapi", "openbanking_brazil", "openinsurance_brazil", "openbanking_ksa" })
public class FAPI1AdvancedFinalBrV1ClientTestInvalidOpenBankingIntentId extends AbstractFAPI1AdvancedFinalBrV1ClientExpectNothingAfterIdTokenIssued {

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(AddInvalidOpenBankingIntentIdToIdToken.class, "OBSP-3.3");
	}

	@Override
	protected String getIdTokenFaultErrorMessage() {
		return "invalid openbanking_intent_id value";
	}
}
