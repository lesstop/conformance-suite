package net.openid.conformance.fapi2baselineid2;

import net.openid.conformance.condition.as.InvalidateJarmSignature;
import net.openid.conformance.condition.as.SignJarmWithNullAlgorithm;
import net.openid.conformance.condition.as.jarm.SignJARMResponse;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantNotApplicable;


@PublishTestModule(
	testName = "fapi2-baseline-id2-client-test-ensure-jarm-signature-is-not-none",
	displayName = "FAPI2-Baseline-ID2: sends a JARM response with a signature signed with the none algorithm.",
	summary = "This test should end with the client displaying an error message that the JARM signature and/or algorithm is invalid",
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
public class FAPI2BaselineID2ClientTestEnsureJarmSignatureAlgIsNotNone extends AbstractFAPI2BaselineID2ClientExpectNothingAfterAuthorizationResponse {


	@Override
	protected void createJARMResponse() {
		generateJARMResponseClaims();
		callAndStopOnFailure(SignJarmWithNullAlgorithm.class);
		encryptJARMResponse();
	}


	@Override
	protected void addCustomValuesToIdToken(){
		//Do nothing
	}

	@Override
	protected String getAuthorizationResponseErrorMessage() {
		return "Signed JARM response with algorithm NONE";
	}
}
