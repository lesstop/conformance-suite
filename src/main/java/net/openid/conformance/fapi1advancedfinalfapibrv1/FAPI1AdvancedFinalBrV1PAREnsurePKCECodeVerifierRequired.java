package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.CallTokenEndpointAndReturnFullResponse;
import net.openid.conformance.condition.client.CheckErrorDescriptionFromTokenEndpointResponseErrorContainsCRLFTAB;
import net.openid.conformance.condition.client.CheckErrorFromTokenEndpointResponseErrorInvalidGrant;
import net.openid.conformance.condition.client.CheckTokenEndpointHttpStatus400;
import net.openid.conformance.condition.client.CheckTokenEndpointReturnedJsonContentType;
import net.openid.conformance.condition.client.ValidateErrorDescriptionFromTokenEndpointResponseError;
import net.openid.conformance.condition.client.ValidateErrorFromTokenEndpointResponseError;
import net.openid.conformance.condition.client.ValidateErrorUriFromTokenEndpointResponseError;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIAuthRequestMethod;
import net.openid.conformance.variant.VariantNotApplicable;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-ensure-pkce-code-verifier-required",
	displayName = "FAPI1-Advanced-Final-Br-v1: Ensure PKCE code_verifier required",
	summary = "This test authenticates as normal except that when calling the token endpoint it omits the 'code_verifier' parameter. The test must end with the token endpoint returning an 'invalid_grant' error, as PKCE is required and requires the code_verifier parameter.",
	profile = "FAPI1-Advanced-Final-Br-v1",
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
@VariantNotApplicable(parameter = FAPIAuthRequestMethod.class, values = {
	"by_value" // PKCE is only required by FAPI1-Adv when using PAR
})
public class FAPI1AdvancedFinalBrV1PAREnsurePKCECodeVerifierRequired extends AbstractFAPI1AdvancedFinalBrV1PerformTokenEndpoint {

	@Override
	protected void addPkceCodeVerifier() {
	}

	@Override
	protected void requestAuthorizationCode() {
		/* expect an 'invalid_grant' error */
		callAndStopOnFailure(CallTokenEndpointAndReturnFullResponse.class, Condition.ConditionResult.FAILURE, "FAPI1-BASE-5.2.2-19");
		callAndContinueOnFailure(CheckTokenEndpointHttpStatus400.class, Condition.ConditionResult.FAILURE, "RFC6749-5.2");
		callAndContinueOnFailure(CheckTokenEndpointReturnedJsonContentType.class, Condition.ConditionResult.FAILURE, "OIDCC-3.1.3.4");
		callAndContinueOnFailure(CheckErrorFromTokenEndpointResponseErrorInvalidGrant.class, Condition.ConditionResult.FAILURE, "RFC7636-4.6", "RFC6749-5.2");
		callAndContinueOnFailure(ValidateErrorFromTokenEndpointResponseError.class, Condition.ConditionResult.FAILURE, "RFC6749-5.2");
		callAndContinueOnFailure(CheckErrorDescriptionFromTokenEndpointResponseErrorContainsCRLFTAB.class, Condition.ConditionResult.WARNING, "RFC6749-5.2");
		callAndContinueOnFailure(ValidateErrorDescriptionFromTokenEndpointResponseError.class, Condition.ConditionResult.FAILURE,"RFC6749-5.2");
		callAndContinueOnFailure(ValidateErrorUriFromTokenEndpointResponseError.class, Condition.ConditionResult.FAILURE,"RFC6749-5.2");

		fireTestFinished();
	}

}
