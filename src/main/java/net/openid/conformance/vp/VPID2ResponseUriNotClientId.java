package net.openid.conformance.vp;

import net.openid.conformance.condition.client.AddBadResponseUriToAuthorizationEndpointRequest;
import net.openid.conformance.condition.client.AddResponseUriToAuthorizationEndpointRequest;
import net.openid.conformance.condition.common.ExpectRedirectUriErrorPage;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.testmodule.TestFailureException;
import net.openid.conformance.variant.VPClientIdScheme;
import net.openid.conformance.variant.VariantNotApplicable;

@PublishTestModule(
	testName = "oid4vp-negative-test-response-uri-not-client-id",
	displayName = "OID4VP: response_uri not valid",
	summary = "Makes a request where the response_uri is not the client_id. The wallet should display an error, a screenshot of which must be uploaded.",
	profile = "OID4VP-ID2",
	configurationFields = {
		"client.presentation_definition"
	}
)
// For x509 dns the client_id we try would need to be on a different hostname; but even this is permitted by the specs in some cases:
// "If the Wallet can establish trust in the Client Identifier authenticated through the certificate, e.g. because the Client Identifier is contained in a list of trusted Client Identifiers, it may allow the client to freely choose the redirect_uri value."
// So we just don't do this test for x509_san_dns for now

// FIXME: can replace this with a test that uses an invalid client_id instead, which'd be valid for all client id schemes? or is that a new test?
@VariantNotApplicable(parameter = VPClientIdScheme.class, values={"x509_san_dns"})
public class VPID2ResponseUriNotClientId extends AbstractVPServerTest {
	@Override
	protected ConditionSequence createAuthorizationRequestSequence() {
		ConditionSequence createAuthorizationRequestSteps = super.createAuthorizationRequestSequence();

		createAuthorizationRequestSteps = createAuthorizationRequestSteps.
			replace(AddResponseUriToAuthorizationEndpointRequest.class, condition(AddBadResponseUriToAuthorizationEndpointRequest.class));

		return createAuthorizationRequestSteps;
	}

	@Override
	protected void createPlaceholder() {
		// FIXME use a better placeholder with a better message
		callAndStopOnFailure(ExpectRedirectUriErrorPage.class, "OID4VP-6.2");

		env.putString("error_callback_placeholder", env.getString("redirect_uri_error"));
	}

	@Override
	protected Object handleRequestUriRequest() {
		Object o = super.handleRequestUriRequest();
		setStatus(Status.RUNNING);
		createPlaceholder();
		waitForPlaceholders();
		setStatus(Status.WAITING);
		return o;
	}

	@Override
	protected Object handleDirectPost(String requestId) {
		throw new TestFailureException(getId(), "Direct post (response_uri) endpoint has been called but was not in the request");
	}
}
