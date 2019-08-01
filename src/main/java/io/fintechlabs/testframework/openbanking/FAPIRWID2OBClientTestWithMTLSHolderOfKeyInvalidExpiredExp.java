package io.fintechlabs.testframework.openbanking;

import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.condition.as.AddInvalidExpiredExpValueToIdToken;
import io.fintechlabs.testframework.fapi.FAPIRWID2ClientTest;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.Variant;

@PublishTestModule(
	testName = "fapi-rw-id2-ob-client-test-with-mtls-holder-of-key-invalid-expired-exp",
	displayName = "FAPI-RW-ID2-OB: client test - expired exp value in id_token from authorization_endpoint, should be rejected (with MTLS)",
	summary = "This test should end with the client displaying an error message that the exp value in the id_token from the authorization_endpoint has expired more than 5 minutes in the past",
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
		FAPIRWID2ClientTest.variant_openbankinguk_privatekeyjwt
	}
)

public class FAPIRWID2OBClientTestWithMTLSHolderOfKeyInvalidExpiredExp extends AbstractFAPIRWID2OBClientExpectNothingAfterAuthorizationEndpoint {

	@Variant(name = variant_openbankinguk_mtls)
	public void setupOpenBankingUkMTLS() {
		super.setupOpenBankingUkMTLS();
	}

	@Override
	protected void addCustomValuesToIdToken() {

		callAndStopOnFailure(AddInvalidExpiredExpValueToIdToken.class, "OIDCC-3.1.3.7-9");
	}

	@Override
	protected Object authorizationCodeGrantType(String requestId) {

		throw new ConditionError(getId(), "Client has incorrectly called token_endpoint after receiving an id_token with an expired exp value from the authorization_endpoint.");

	}

}
