package io.fintechlabs.testframework.fapi;

import io.fintechlabs.testframework.plan.PublishTestPlan;
import io.fintechlabs.testframework.plan.TestPlan;

@PublishTestPlan(
	testPlanName = "fapi-rw-id2-with-private-key-and-mtls-holder-of-key-test-plan",
	displayName = "FAPI-RW-ID2: Authorization server test using private_key_jwt client authentication",
	profile = "FAPI-RW-ID2-OpenID-Provider-Authorization-Server-Test",
	testModules = {
		// Normal well behaved client cases
		FAPIRWID2DiscoveryEndpointVerification.class,
		FAPIRWID2WithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2UserRejectsAuthenticationWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureServerAcceptsRequestObjectWithMultipleAudWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureAuthorizationRequestWithoutStateSuccessWithPrivateKeyAndMTLSHolderOfKey.class,

		// Possible failure case
		FAPIRWID2EnsureResponseModeQueryWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureDifferentNonceInsideAndOutsideRequestObjectWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRegisteredRedirectUriWithPrivateKeyAndMTLSHolderOfKey.class,

		// Negative tests for request objects
		FAPIRWID2EnsureRequestObjectWithoutExpFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRequestObjectWithoutScopeFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRequestObjectWithoutStateWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRequestObjectWithoutNonceFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRequestObjectWithoutRedirectUriFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureExpiredRequestObjectFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRequestObjectWithBadAudFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureSignedRequestObjectWithRS256FailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRequestObjectSignatureAlgorithmIsNotNoneWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureMatchingKeyInAuthorizationRequestWithPrivateKeyAndMTLSHolderOfKey.class,

		// Negative tests for authorization request
		FAPIRWID2EnsureAuthorizationRequestWithoutRequestObjectFailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureRedirectUriInAuthorizationRequestWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureResponseTypeCodeFailsWithPrivateKeyAndMTLSHolderOfKey.class,

		// Negative tests for token endpoint
		FAPIRWID2EnsureClientIdInTokenEndpointWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureMTLSHolderOfKeyRequiredWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureAuthorizationCodeIsBoundToClientWithPrivateKeyAndMTLSHolderOfKey.class,

		// Private key specific tests
		FAPIRWID2EnsureSignedClientAssertionWithRS256FailsWithPrivateKeyAndMTLSHolderOfKey.class,
		FAPIRWID2EnsureClientAssertionInTokenEndpointWithPrivateKeyAndMTLSHolderOfKey.class,
		
		//Refresh token tests
		FAPIRWID2RefreshTokenTestWithPrivateKeyAndMTLSHolderOfKey.class		
	}
)
public class FAPI_RW_ID2_WithPrivateKeyAndMTLSHolderOfKeyTestPlan implements TestPlan {

}
