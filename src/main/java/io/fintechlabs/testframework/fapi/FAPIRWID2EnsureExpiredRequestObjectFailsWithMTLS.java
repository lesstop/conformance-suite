package io.fintechlabs.testframework.fapi;

import io.fintechlabs.testframework.condition.client.AddClientIdToTokenEndpointRequest;
import io.fintechlabs.testframework.condition.client.CreateTokenEndpointRequestForAuthorizationCodeGrant;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.Variant;

@PublishTestModule(
	testName = "fapi-rw-id2-ensure-expired-request-object-fails-with-mtls",
	displayName = "FAPI-RW-ID2: ensure expired request object fails (with MTLS authentication)",
	summary = "This test should end with the authorisation server showing an error message that the request object is invalid (a screenshot of which should be uploaded) or with the user being redirected back to the conformance suite with a correct error response.",
	profile = "FAPI-RW-ID2",
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
		"resource.resourceUrl",
		"resource.institution_id"
	},
	notApplicableForVariants = {
		FAPIRWID2.variant_privatekeyjwt,
		FAPIRWID2.variant_openbankinguk_mtls,
		FAPIRWID2.variant_openbankinguk_privatekeyjwt
	}
)
public class FAPIRWID2EnsureExpiredRequestObjectFailsWithMTLS extends AbstractFAPIRWID2EnsureExpiredRequestObjectFails {

	@Variant(name = variant_mtls)
	public void setupMTLS() {
		super.setupMTLS();
	}

	public FAPIRWID2EnsureExpiredRequestObjectFailsWithMTLS() {
		super(new StepsConfigurationFAPI());
	}

	@Override
	protected void createAuthorizationCodeRequest() {
		callAndStopOnFailure(CreateTokenEndpointRequestForAuthorizationCodeGrant.class);

		callAndStopOnFailure(AddClientIdToTokenEndpointRequest.class);
	}

}
