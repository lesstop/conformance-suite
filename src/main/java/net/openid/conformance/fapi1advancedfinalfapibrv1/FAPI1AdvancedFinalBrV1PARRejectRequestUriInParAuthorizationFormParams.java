package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AddBadRequestUriToRequestParameters;
import net.openid.conformance.condition.client.CallPAREndpoint;
import net.openid.conformance.condition.client.EnsurePARInvalidRequestOrInvalidRequestObjectOrRequestUriNotSupportedError;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPIAuthRequestMethod;
import net.openid.conformance.variant.VariantNotApplicable;

//PAR-2.1 : The request_uri authorization request parameter MUST NOT be provided in this case
@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-par-authorization-request-containing-request_uri-form-param",
	displayName = "PAR : The request_uri authorization request parameter MUST NOT be provided",
	summary = "This test sends a random request_uri form parameter in PAR authorization request and expects authorization server to return an error",
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
	"by_value"
})
public class FAPI1AdvancedFinalBrV1PARRejectRequestUriInParAuthorizationFormParams extends AbstractFAPI1AdvancedFinalBrV1ServerTestModule {

	@Override
	protected  void performParAuthorizationRequestFlow() {

		//"pushed_authorization_request_form_parameters"
		callAndStopOnFailure(AddBadRequestUriToRequestParameters.class);

		callAndStopOnFailure(CallPAREndpoint.class);

		callAndContinueOnFailure(EnsurePARInvalidRequestOrInvalidRequestObjectOrRequestUriNotSupportedError.class, Condition.ConditionResult.FAILURE, "PAR-2.1-2");

		fireTestFinished();
	}

}
