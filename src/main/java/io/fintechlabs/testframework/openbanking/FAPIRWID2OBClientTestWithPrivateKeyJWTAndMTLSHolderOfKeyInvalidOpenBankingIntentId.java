package io.fintechlabs.testframework.openbanking;

import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.condition.as.AddInvalidOpenBankingIntentIdToIdToken;
import io.fintechlabs.testframework.fapi.FAPIRWID2ClientTest;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.Variant;

@PublishTestModule(
	testName = "fapi-rw-id2-ob-client-test-with-private-key-jwt-and-mtls-holder-of-key-invalid-openbanking-intent-id",
	displayName = "FAPI-RW-ID2-OB: client test  - invalid openbanking_intent_id, should be rejected (with private_key_jwt and MTLS)",
	summary = "This test should end with the client displaying an error message that the openbanking_intent_id returned in id_token from authorization endpoint does not match the value sent in the request object",
	profile = "FAPI-RW-ID2-OB",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.scope",
		"client.redirect_uri",
		"client.certificate",
		"client.jwks",
	},
	notApplicableForVariants = {
		FAPIRWID2ClientTest.variant_mtls,
		FAPIRWID2ClientTest.variant_privatekeyjwt,
		FAPIRWID2ClientTest.variant_openbankinguk_mtls
	}
)

public class FAPIRWID2OBClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidOpenBankingIntentId extends AbstractFAPIRWID2OBClientExpectNothingAfterAuthorizationEndpoint {

	@Variant(name = variant_openbankinguk_privatekeyjwt)
	public void setupOpenBankingUkPrivateKeyJwt() {
		super.setupOpenBankingUkPrivateKeyJwt();
	}

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(AddInvalidOpenBankingIntentIdToIdToken.class, "OBSP-3.3");
	}

	@Override
	protected Object authorizationCodeGrantType(String requestId) {

		throw new ConditionError(getId(), "Client has incorrectly called token_endpoint after receiving an id_token with an invalid openbanking_intent_id value from the authorization_endpoint.");

	}

}
