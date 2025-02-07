package net.openid.conformance.fapi1advancedfinal;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.condition.client.CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint;
import net.openid.conformance.condition.client.CheckStateInAuthorizationResponse;
import net.openid.conformance.condition.client.DetectWhetherErrorResponseIsInQueryOrFragment;
import net.openid.conformance.condition.client.EnsureErrorFromAuthorizationEndpointResponse;
import net.openid.conformance.condition.client.EnsurePARUnsupportedResponseTypeOrInvalidRequestOrUnauthorizedClientError;
import net.openid.conformance.condition.client.EnsureUnsupportedResponseTypeOrInvalidRequestError;
import net.openid.conformance.condition.client.RejectAuthCodeInUrlFragment;
import net.openid.conformance.condition.client.RejectAuthCodeInUrlQuery;
import net.openid.conformance.condition.client.RemoveAuthorizationEndpointRequestResponseMode;
import net.openid.conformance.condition.client.SetAuthorizationEndpointRequestResponseTypeToCode;
import net.openid.conformance.condition.common.ExpectResponseTypeErrorPage;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-ensure-response-type-code-fails",
	displayName = "FAPI1-Advanced-Final: ensure response_type code fails",
	summary = "This test uses response_type=code in the authorization request, which (as a JARM response has not been requested) is not permitted in FAPI1-Advanced - only the hybrid flow ('response_type=code id_token') or JARM ('response_type=code&response_mode=jwt') is allowed. The authorization server should show an error message that the response type is unsupported or the request is invalid (a screenshot of which should be uploaded) or the user should be redirected back to the conformance suite with a correct error response, or an error could be returned from the PAR endpoint.",
	profile = "FAPI1-Advanced-Final",
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
public class FAPI1AdvancedFinalEnsureResponseTypeCodeFails extends AbstractFAPI1AdvancedFinalPARExpectingAuthorizationEndpointPlaceholderOrCallback {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectResponseTypeErrorPage.class, "FAPI1-ADV-5.2.2-2");

		env.putString("error_callback_placeholder", env.getString("response_type_error"));
	}

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestObjectSteps() {
		return super.makeCreateAuthorizationRequestObjectSteps()
				.butFirst(condition(SetAuthorizationEndpointRequestResponseTypeToCode.class),
						condition(RemoveAuthorizationEndpointRequestResponseMode.class));
	}

	@Override
	protected void processParErrorResponse() {
		callAndContinueOnFailure(EnsurePARUnsupportedResponseTypeOrInvalidRequestOrUnauthorizedClientError.class, Condition.ConditionResult.FAILURE, "PAR-2.3");
	}

	@Override
	protected void processCallback() {

		eventLog.startBlock(currentClientString() + "Verify authorization endpoint error response");

		callAndContinueOnFailure(RejectAuthCodeInUrlQuery.class, ConditionResult.FAILURE, "FAPI1-ADV-5.2.2-2");
		callAndContinueOnFailure(RejectAuthCodeInUrlFragment.class, ConditionResult.FAILURE, "FAPI1-ADV-5.2.2-2");

		// It doesn't really matter if the error in the fragment or the query, the specs aren't entirely clear on the matter
		callAndStopOnFailure(DetectWhetherErrorResponseIsInQueryOrFragment.class);

		/* The error from the authorization server:
		 * - must be a 'unsupported_response_type' or "invalid_request" error
		 * - must have the correct state we supplied
		 */
		callAndContinueOnFailure(EnsureUnsupportedResponseTypeOrInvalidRequestError.class, Condition.ConditionResult.FAILURE, "OIDCC-3.3.2.6");

		callAndContinueOnFailure(CheckStateInAuthorizationResponse.class, Condition.ConditionResult.FAILURE);
		callAndContinueOnFailure(EnsureErrorFromAuthorizationEndpointResponse.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(CheckForUnexpectedParametersInErrorResponseFromAuthorizationEndpoint.class, Condition.ConditionResult.WARNING, "OIDCC-3.1.2.6");

		eventLog.endBlock();
		fireTestFinished();
	}
}
