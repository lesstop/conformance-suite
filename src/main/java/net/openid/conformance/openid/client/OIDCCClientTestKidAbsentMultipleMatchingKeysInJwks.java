package net.openid.conformance.openid.client;

import net.openid.conformance.condition.as.OIDCCGenerateServerJWKsMultipleSigningsKeyWithNoKeyIds;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "oidcc-client-kid-absent-multiple-jwks",
	displayName = "OIDCC: Relying party test. Server JWKS contains multiple possible keys but no 'kid's ",
	summary = "Identify that the 'kid' value is missing from the JOSE header and that the Issuer publishes " +
		"multiple keys in its JWK Set document (referenced by 'jwks_uri'). " +
		"The RP can do one of two things; " +
		"reject the ID Token since it can not by using the kid determined which key to use to verify the signature. " +
		"Or it can just test all possible keys and hit upon one that works, which it will in this case." +
		" Corresponds to rp-id_token-kid-absent-multiple-jwks test in the old test suite",
	profile = "OIDCC",
	configurationFields = {
		"waitTimeoutSeconds"
	}
)
public class OIDCCClientTestKidAbsentMultipleMatchingKeysInJwks extends AbstractOIDCCClientTestExpectingNothingInvalidIdToken {

	@Override
	protected void configureServerJWKS() {
		callAndStopOnFailure(OIDCCGenerateServerJWKsMultipleSigningsKeyWithNoKeyIds.class);
	}

}
