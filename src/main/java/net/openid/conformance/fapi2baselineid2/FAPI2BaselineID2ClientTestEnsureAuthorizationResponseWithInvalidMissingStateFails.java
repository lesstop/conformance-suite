package net.openid.conformance.fapi2baselineid2;

import com.google.common.base.Strings;
import net.openid.conformance.condition.as.AddInvalidStateToAuthorizationEndpointResponseParams;
import net.openid.conformance.condition.as.CreateAuthorizationEndpointResponseParams;
import net.openid.conformance.condition.as.RemoveStateFromAuthorizationEndpointResponseParams;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.testmodule.TestFailureException;
import net.openid.conformance.variant.FAPIResponseMode;
import net.openid.conformance.variant.VariantNotApplicable;


@PublishTestModule(
	testName = "fapi2-baseline-id2-client-test-ensure-authorization-response-with-invalid-missing-state-fails",
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

public class FAPI2BaselineID2ClientTestEnsureAuthorizationResponseWithInvalidMissingStateFails extends AbstractFAPI2BaselineID2ClientTest {

	protected boolean removedState = false;

	@Override
	protected void addCustomValuesToAuthorizationResponse() {
		String state = env.getString(CreateAuthorizationEndpointResponseParams.ENV_KEY, CreateAuthorizationEndpointResponseParams.STATE);
		if(!Strings.isNullOrEmpty(state)) {
			callAndContinueOnFailure(RemoveStateFromAuthorizationEndpointResponseParams.class);
			removedState = true;
		}
	}

	@Override
	protected void addCustomValuesToIdToken(){
		//Do nothing
	}

	@Override
	protected void createAuthorizationEndpointResponse() {
		super.createAuthorizationEndpointResponse();
		if(removedState) {
			startWaitingForTimeout();
		}
	}

	@Override
	protected Object tokenEndpoint(String requestId) {
		if(removedState) {
			throw new TestFailureException(getId(), "Client has incorrectly called token_endpoint after receiving an invalid authorization response (" +
				getAuthorizationResponseErrorMessage() + ")");
		} else {
			return super.tokenEndpoint(requestId);
		}
	}

	@Override
	protected Object userinfoEndpoint(String requestId) {
		if(removedState) {
			throw new TestFailureException(getId(), "Client has incorrectly called userinfo_endpoint after receiving an invalid authorization response (" +
				getAuthorizationResponseErrorMessage() + ")");
		} else {
			return super.userinfoEndpoint(requestId);
		}
	}

	@Override
	protected Object accountsEndpoint(String requestId) {
		if(removedState) {
			throw new TestFailureException(getId(), "Client has incorrectly called accounts endpoint after receiving an invalid authorization response (" +
				getAuthorizationResponseErrorMessage() + ")");
		} else {
			return super.accountsEndpoint(requestId);
		}
	}

	protected String getAuthorizationResponseErrorMessage() {
		return "Removed state from the authorization response";
	}
}
