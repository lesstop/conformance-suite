package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.AddScopeToDynamicRegistrationRequest;
import net.openid.conformance.condition.client.CheckScopesFromDynamicRegistrationEndpointDoNotExceedRequestedScopes;
import net.openid.conformance.condition.client.ReverseScopeOrderInDynamicRegistrationEndpointRequest;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-brazildcr-happy-flow-variant-2",
	displayName = "FAPI1-Advanced-Final-Br-v1: Brazil DCR happy flow variant 2",
	summary = "Obtain a software statement from the Brazil directory (using the client MTLS certificate and directory client id provided in the test configuration), register a new client on the target authorization server and perform an authorization flow. The registration request has the members of the 'scope' string in a different order to the other happy flow variant test.",
	profile = "FAPI1-Advanced-Final-Br-v1",
	configurationFields = {
		"server.discoveryUrl",
		"client.scope",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"directory.discoveryUrl",
		"directory.client_id",
		"directory.apibase",
		"resource.resourceUrl"
	}
)
public class FAPI1AdvancedFinalBrV1BrazilDCRHappyFlowVariant2 extends AbstractFAPI1AdvancedFinalBrV1BrazilDCR {

	@Override
	protected void callRegistrationEndpoint() {

		callAndStopOnFailure(AddScopeToDynamicRegistrationRequest.class, "RFC7591-2");
		callAndStopOnFailure(ReverseScopeOrderInDynamicRegistrationEndpointRequest.class, "RFC7591-2", "RFC6749-3.3");

		super.callRegistrationEndpoint();

		callAndContinueOnFailure(CheckScopesFromDynamicRegistrationEndpointDoNotExceedRequestedScopes.class, Condition.ConditionResult.FAILURE, "BrazilOBDCR-7.1.1", "RFC7591-2", "RFC7591-3.2.1");

	}
}
