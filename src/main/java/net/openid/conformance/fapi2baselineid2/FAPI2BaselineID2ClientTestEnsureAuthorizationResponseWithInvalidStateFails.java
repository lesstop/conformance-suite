package net.openid.conformance.fapi2baselineid2;

import net.openid.conformance.condition.as.AddInvalidAudValueToJarm;
import net.openid.conformance.condition.as.AddInvalidStateToAuthorizationEndpointResponseParams;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.testmodule.TestFailureException;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantNotApplicable;


@PublishTestModule(
	testName = "fapi2-baseline-id2-client-test-ensure-authorization-response-with-invalid-state-fails",
	displayName = "FAPI2-Baseline-ID2: sends an authorization endpoint response with an invalid state value.",
	summary = "This test should end with the client displaying an error message that the state value in the authorization endpoint response is invalid",
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

public class FAPI2BaselineID2ClientTestEnsureAuthorizationResponseWithInvalidStateFails extends AbstractFAPI2BaselineID2ClientExpectNothingAfterAuthorizationResponse {

	@Override
	protected void addCustomValuesToAuthorizationResponse() {
		callAndContinueOnFailure(AddInvalidStateToAuthorizationEndpointResponseParams.class);
	}

	@Override
	protected void addCustomValuesToIdToken(){
		//Do nothing
	}
	@Override
	protected void createAuthorizationEndpointResponse() {
		super.createAuthorizationEndpointResponse();
		startWaitingForTimeout();
	}

	@Override
	protected Object tokenEndpoint(String requestId) {
		throw new TestFailureException(getId(), "Client has incorrectly called token_endpoint after receiving an invalid authorization response (" +
			getAuthorizationResponseErrorMessage()+ ")");
	}

	@Override
	protected Object userinfoEndpoint(String requestId) {
		throw new TestFailureException(getId(), "Client has incorrectly called userinfo_endpoint after receiving an invalid authorization response (" +
			getAuthorizationResponseErrorMessage() + ")");
	}

	@Override
	protected Object accountsEndpoint(String requestId) {
		throw new TestFailureException(getId(), "Client has incorrectly called accounts endpoint after receiving an invalid authorization response (" +
			getAuthorizationResponseErrorMessage() + ")");
	}

	@Override
	protected String getAuthorizationResponseErrorMessage() {
		return "Added invalid state to the authorization response";
	}
}
