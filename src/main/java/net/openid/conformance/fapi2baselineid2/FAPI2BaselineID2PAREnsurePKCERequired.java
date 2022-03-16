package net.openid.conformance.fapi2baselineid2;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.EnsureInvalidRequestError;
import net.openid.conformance.condition.client.EnsurePARInvalidRequestError;
import net.openid.conformance.condition.client.ExpectPkceMissingErrorPage;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.VariantNotApplicable;

@PublishTestModule(
	testName = "fapi2-baseline-id2-par-ensure-pkce-required",
	displayName = "FAPI2-Baseline-ID2: ensure pkce is required when using pushed authorization requests",
	summary = "This test makes a FAPI authorization request without using PKCE (RFC7636), which must be rejected. FAPI2-Baseline-ID2 requires servers to reject PAR requests that do not use PKCE, clause 5.2.2-18. Depending on when the server chooses to verify the request, the refusal may be an error from the pushed authorization request endpoint, or an invalid_request error may be returned from the authorization endpoint, or an error may be shown to the user (a screenshot of which must be uploaded).",
	profile = "FAPI2-Baseline-ID2",
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
public class FAPI2BaselineID2PAREnsurePKCERequired extends AbstractFAPI2BaselineID2ExpectingAuthorizationEndpointPlaceholderOrCallback {

	@Override
	protected void createPlaceholder() {
		callAndStopOnFailure(ExpectPkceMissingErrorPage.class, "FAPI1-ADV-5.2.2-18");

		env.putString("error_callback_placeholder", env.getString("pkce_missing_error"));
	}

	@Override
	protected ConditionSequence makeCreateAuthorizationRequestSteps() {
		return new CreateAuthorizationRequestSteps(isSecondClient(),
			jarm,
			false,
			profileAuthorizationEndpointSetupSteps);
	}

	@Override
	protected void processParResponse() {
		// the server could reject this at the par endpoint, or at the authorization endpoint
		String key = "pushed_authorization_endpoint_response_http_status";
		Integer http_status = env.getInteger(key);
		if (http_status >= 200 && http_status < 300) {
			super.processParResponse();
			return;
		}

		callAndContinueOnFailure(EnsurePARInvalidRequestError.class, Condition.ConditionResult.FAILURE, "RFC7636-4.4.1");

		fireTestFinished();
	}

	@Override
	protected void onAuthorizationCallbackResponse() {
		callAndContinueOnFailure(EnsureInvalidRequestError.class, Condition.ConditionResult.FAILURE, "RFC7636-4.4.1");
		fireTestFinished();
	}

}
