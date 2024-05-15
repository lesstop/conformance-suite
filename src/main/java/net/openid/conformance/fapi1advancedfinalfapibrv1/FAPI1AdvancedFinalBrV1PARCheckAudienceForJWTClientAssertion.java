package net.openid.conformance.fapi1advancedfinalfapibrv1;

import net.openid.conformance.condition.client.AddPAREndpointAsAudToClientAuthenticationAssertionClaims;
import net.openid.conformance.condition.client.UpdateClientAuthenticationAssertionClaimsWithISSAud;
import net.openid.conformance.sequence.client.CreateJWTClientAuthenticationAssertionAndAddToPAREndpointRequest;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.ClientAuthType;
import net.openid.conformance.variant.FAPIAuthRequestMethod;
import net.openid.conformance.variant.VariantNotApplicable;


@PublishTestModule(
	testName = "fapi1-advanced-final-br-v1-par-test-pushed-authorization-url-as-audience-for-client-JWT-assertion",
	displayName = "PAR : try to use pushed authorization request endpoint url as audience for Client JWT Assertion",
	summary = "This test tries to use the pushed authorization request endpoint url as audience for Client JWT Assertion, the authorization server is expected to accept it",
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
@VariantNotApplicable(parameter = ClientAuthType.class, values = {
	"mtls"
})
public class FAPI1AdvancedFinalBrV1PARCheckAudienceForJWTClientAssertion extends AbstractFAPI1AdvancedFinalBrV1ServerTestModule {
	/*
	PAR-2.0
	Note that there's some potential ambiguity around the appropriate audience value to use when
	JWT client assertion based authentication is employed. To address that ambiguity the issuer
	identifier URL of the AS according to [RFC8414] SHOULD be used as the value of the audience.
	In order to facilitate interoperability the AS MUST accept its issuer identifier,
	token endpoint URL, or pushed authorization request endpoint URL as values that identify
	it as an intended audience.
	*/
	@Override
	protected void addClientAuthenticationToPAREndpointRequest() {
		call(((new CreateJWTClientAuthenticationAssertionAndAddToPAREndpointRequest()).replace(
			UpdateClientAuthenticationAssertionClaimsWithISSAud.class,
			condition(AddPAREndpointAsAudToClientAuthenticationAssertionClaims.class).requirement("PAR-2"))));
	}
}
