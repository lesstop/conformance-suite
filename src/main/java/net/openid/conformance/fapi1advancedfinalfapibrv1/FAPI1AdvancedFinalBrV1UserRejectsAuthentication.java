package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint;
import net.openid.conformance.condition.client.CheckMatchingCallbackParameters;
import net.openid.conformance.condition.client.CheckStateInAuthorizationResponse;
import net.openid.conformance.condition.client.CreateRandomStateValue;
import net.openid.conformance.condition.client.EnsureErrorFromAuthorizationEndpointResponse;
import net.openid.conformance.condition.client.ExpectAccessDeniedErrorFromAuthorizationEndpointDueToUserRejectingRequest;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.Command;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-user-rejects-authentication",
	displayName = "FAPI1-Advanced-Final-Br-v1: user rejects authentication",
	summary = "This test requires the user to reject the authentication or consent, for example by pressing the 'cancel' button on the login screen. It verifies that the user is redirected back to the relying party's redirect_uri with an 'access_denied' error.\n\nYou may need to clear any cookies for the authorization server before running this test, to remove any existing login session and hence ensure the user is offered the opportunity to reject the authentication.",
	profile = "FAPI1-Advanced-Final-Br-v1",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.scope",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"client2.client_id",
		"client2.scope",
		"client2.jwks",
		"mtls2.key",
		"mtls2.cert",
		"mtls2.ca",
		"resource.resourceUrl"
	}
)
public class FAPI1AdvancedFinalBrV1UserRejectsAuthentication extends AbstractFAPI1AdvancedFinalBrV1MultipleClient {

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestSteps() {
		// Add length state with 128
		Command cmd = new Command();
		cmd.putInteger("requested_state_length", 128);

		ConditionSequence conditionSequence = super.makeCreateAuthorizationRequestSteps()
			.insertBefore(CreateRandomStateValue.class, cmd);

		return conditionSequence;
	}

	@Override
	protected void onAuthorizationCallbackResponse() {

		callAndContinueOnFailure(CheckStateInAuthorizationResponse.class, Condition.ConditionResult.FAILURE);
		callAndContinueOnFailure(EnsureErrorFromAuthorizationEndpointResponse.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint.class, Condition.ConditionResult.WARNING, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(ExpectAccessDeniedErrorFromAuthorizationEndpointDueToUserRejectingRequest.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6", "RFC6749-4.1.2.1");

		if (!isSecondClient()) {
			performAuthorizationFlowWithSecondClient();
		} else {

			// Check if server return correct params as we requested in redirect_uri query part
			callAndStopOnFailure(CheckMatchingCallbackParameters.class);
			fireTestFinished();
		}
	}
}
