package io.fintechlabs.testframework.fapi;

import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.client.BuildPlainRedirectToAuthorizationEndpoint;
import io.fintechlabs.testframework.condition.client.EnsureInvalidRequestError;
import io.fintechlabs.testframework.condition.client.ExpectAuthorizationRequestWithoutRequestObjectErrorPage;
import io.fintechlabs.testframework.condition.client.ValidateErrorResponseFromAuthorizationEndpoint;

public abstract class AbstractFAPIRWID2EnsureAuthorizationRequestWithoutRequestObjectFails extends AbstractFAPIRWID2ServerTestModule {

	@Override
	protected void performAuthorizationFlow() {

		createAuthorizationRequest();

		createAuthorizationRedirect();

		String redirectTo = env.getString("redirect_to_authorization_endpoint");

		eventLog.log(getName(), args("msg", "Redirecting to authorization endpoint",
			"redirect_to", redirectTo,
			"http", "redirect"));

		eventLog.log(getName(), "Redirecting to url " + redirectTo);

		callAndStopOnFailure(ExpectAuthorizationRequestWithoutRequestObjectErrorPage.class, "FAPI-RW-5.2.2-1");

		setStatus(Status.WAITING);

		waitForPlaceholders();

		browser.goToUrl(redirectTo, env.getString("request_unverifiable_error"));
	}

	@Override
	protected void createAuthorizationRedirect() {

		callAndStopOnFailure(BuildPlainRedirectToAuthorizationEndpoint.class, "FAPI-RW-5.2.2-1");
	}


	@Override
	protected void onAuthorizationCallbackResponse() {
		/* If we get an error back from the authorisation server:
		 * - It must be a 'invalid_request' error
		 * - It must have the correct state we supplied
		 */

		callAndContinueOnFailure(ValidateErrorResponseFromAuthorizationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.2.6");
		callAndContinueOnFailure(EnsureInvalidRequestError.class, Condition.ConditionResult.FAILURE, "OIDCC-3.3.2.6");
		fireTestFinished();
	}
}
