package net.openid.conformance.fapi2baselineid2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.as.AddInvalidExpiredExpValueToJarm;
import net.openid.conformance.testmodule.OIDFJSON;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantNotApplicable;

import java.time.Instant;


@PublishTestModule(
	testName = "fapi2-baseline-id2-client-test-ensure-jarm-with-expired-exp-fails",
	displayName = "FAPI2-Baseline-ID2: sends a JARM response with an expired exp claim.",
	summary = "This test should end with the client displaying an error message that the JARM response has an invalid/expired exp claim",
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
public class FAPI2BaselineID2ClientTestEnsureJarmWithExpiredExpFails extends AbstractFAPI2BaselineID2ClientExpectNothingAfterAuthorizationResponse {


	@Override
	protected void generateJARMResponseClaims() {
		super.generateJARMResponseClaims();
		callAndContinueOnFailure(AddInvalidExpiredExpValueToJarm.class);
	}

	@Override
	protected void addCustomValuesToIdToken(){
		//Do nothing
	}

	@Override
	protected String getAuthorizationResponseErrorMessage() {
		return "Added expired exp to JARM response";
	}
}
