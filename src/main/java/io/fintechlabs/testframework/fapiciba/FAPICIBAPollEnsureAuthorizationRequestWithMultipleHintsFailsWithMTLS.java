package io.fintechlabs.testframework.fapiciba;

import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.Variant;

@PublishTestModule(
	testName = "fapi-ciba-poll-ensure-authorization-request-with-multiple-hints-fails-with-mtls",
	displayName = "FAPI-CIBA: Poll mode - try sending two hints value to authorization endpoint request, should return an error (MTLS client authentication)",
	summary = "This test should return an error that try sending two hints value to authorization endpoint request",
	profile = "FAPI-CIBA",
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
public class FAPICIBAPollEnsureAuthorizationRequestWithMultipleHintsFailsWithMTLS extends AbstractFAPICIBAEnsureAuthorizationRequestWithMultipleHintsFailsWithMTLS {
	@Variant(name = "mtls")
	public void setupMTLS() {
		// FIXME: add private key variant
	}

	@Override
	protected void cleanupAfterBackchannelRequestShouldHaveFailed() {
		pollCleanupAfterBackchannelRequestShouldHaveFailed();
	}

	@Override
	protected void modeSpecificAuthorizationEndpointRequest() {
		/* Nothing to do */
	}

}
