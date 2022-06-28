package net.openid.conformance.fapi2baselineid2;

import net.openid.conformance.condition.as.RemoveAudFromJarm;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantNotApplicable;


@PublishTestModule(
	testName = "fapi2-baseline-id2-client-test-ensure-jarm-without-aud-fails",
	displayName = "FAPI2-Baseline-ID2: sends a JARM response without the aud claim.",
	summary = "This test should end with the client displaying an error message that the JARM response is missing the aud claim",
	profile = "FAPI2-Baseline-ID2",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.scope",
		"client.redirect_uri",
		"client.certificate",
		"client.jwks"
	}
)

@VariantNotApplicable(parameter = FAPIResponseMode.class, values = { "plain_response" })
public class FAPI2BaselineID2ClientTestEnsureJarmWithoutAudFails extends AbstractFAPI2BaselineID2ClientExpectNothingAfterAuthorizationResponse {


	@Override
	protected void addCustomValuesToJarmResponse() {
		callAndContinueOnFailure(RemoveAudFromJarm.class);
	}

	@Override
	protected void addCustomValuesToIdToken(){
		//Do nothing
	}

	@Override
	protected String getAuthorizationResponseErrorMessage() {
		return "Removed aud from JARM response";
	}
}
