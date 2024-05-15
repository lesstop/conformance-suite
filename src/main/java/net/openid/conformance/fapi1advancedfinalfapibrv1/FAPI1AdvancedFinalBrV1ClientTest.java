package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.Condition.ConditionResult;
import net.openid.conformance.condition.as.CheckForInvalidCharsInNonce;
import net.openid.conformance.condition.as.CheckForInvalidCharsInState;
import net.openid.conformance.condition.as.CheckNonceLength;
import net.openid.conformance.condition.as.CheckStateLength;
import net.openid.conformance.condition.as.CreateEffectiveAuthorizationRequestParameters;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-client-test",
	displayName = "FAPI1-Advanced-Final-Br-v1: client test",
	summary = "Tests a 'happy path' flow; the client should perform OpenID discovery from the displayed discoveryUrl, call the authorization endpoint (which will immediately redirect back), exchange the authorization code for an access token at the token endpoint and make a request to the accounts/payments/resources endpoint displayed..",
	profile = "FAPI1-Advanced-Final-Br-v1",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.scope",
		"client.redirect_uri",
		"client.certificate",
		"client.jwks"
	}
)

public class FAPI1AdvancedFinalBrV1ClientTest extends AbstractFAPI1AdvancedFinalBrV1ClientTest {

	@Override
	protected void addCustomValuesToIdToken(){
		//Do nothing
	}

	@Override
	protected void createAuthorizationEndpointResponse() {

		String isOpenIdScopeRequested = env.getString("request_scopes_contain_openid");

		if("yes".equals(isOpenIdScopeRequested)) {
			skipIfMissing(null, new String[] {"nonce"}, ConditionResult.INFO,
				CheckForInvalidCharsInNonce.class, ConditionResult.WARNING);
			skipIfMissing(null, new String[] {"nonce"}, ConditionResult.INFO,
				CheckNonceLength.class, ConditionResult.WARNING);
		} else {
			skipIfElementMissing(CreateEffectiveAuthorizationRequestParameters.ENV_KEY,
				CreateEffectiveAuthorizationRequestParameters.STATE, ConditionResult.INFO,
				CheckForInvalidCharsInState.class, ConditionResult.WARNING);
			skipIfElementMissing(CreateEffectiveAuthorizationRequestParameters.ENV_KEY,
				CreateEffectiveAuthorizationRequestParameters.STATE, ConditionResult.INFO,
				CheckStateLength.class, ConditionResult.WARNING);
		}

		super.createAuthorizationEndpointResponse();
	}
}
