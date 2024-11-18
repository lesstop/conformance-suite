package net.openid.conformance.vpverifier;

import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.condition.as.CheckForInvalidCharsInNonce;
import net.openid.conformance.condition.as.CheckNonceLength;
import net.openid.conformance.testmodule.PublishTestModule;

/**
 * the default happy path test
 */
@PublishTestModule(
	testName = "oidcc-client-test",
	displayName = "OIDCC: Relying party test, success case",
	summary = "The client is expected to make an authentication request " +
		"(also a token request and a userinfo request where applicable)" +
		"using the selected response_type and other configuration options. ",
	profile = "OIDCC",
	configurationFields = {
	}
)
public class VPID2VerifierTestHappy extends AbstractVPID2VerifierTest {

	@Override
	protected void extractNonceFromAuthorizationEndpointRequestParameters() {
		super.extractNonceFromAuthorizationEndpointRequestParameters();

		skipIfMissing(null, new String[] {"nonce"}, ConditionResult.INFO,
			CheckForInvalidCharsInNonce.class, ConditionResult.WARNING);
		skipIfMissing(null, new String[] {"nonce"}, ConditionResult.INFO,
			CheckNonceLength.class, ConditionResult.WARNING);
	}
}
