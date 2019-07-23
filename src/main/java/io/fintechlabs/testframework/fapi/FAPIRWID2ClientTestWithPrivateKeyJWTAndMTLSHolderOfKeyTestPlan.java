package io.fintechlabs.testframework.fapi;

import io.fintechlabs.testframework.plan.PublishTestPlan;
import io.fintechlabs.testframework.plan.TestPlan;

@PublishTestPlan (
	testPlanName = "fapi-rw-id2-client-test-with-private-key-jwt-and-mtls-holder-of-key-test-plan",
	displayName = "FAPI-RW-ID2: Relying Party (client test) using private_key_jwt client authentication",
	profile = "FAPI-RW-ID2-Relying-Party-Client-Test",
	testModules = {
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKey.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidSHash.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidCHash.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidNonce.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidIss.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidAud.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidSecondaryAud.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidSignature.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidNullAlg.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidAlternateAlg.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidMissingExp.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidExpiredExp.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyIatIsWeekInPast.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidMissingAud.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidMissingIss.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidMissingNonce.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyInvalidMissingSHash.class,
		FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyValidAudAsArray.class
	},
	variants = {
		FAPIRWID2ClientTest.variant_privatekeyjwt
	}
)
public class FAPIRWID2ClientTestWithPrivateKeyJWTAndMTLSHolderOfKeyTestPlan implements TestPlan {

}
