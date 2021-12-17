package net.openid.conformance.openbanking_brasil.testmodules;

import net.openid.conformance.condition.client.CreatePaymentRequestEntityClaims;
import net.openid.conformance.condition.client.FAPIBrazilCreatePaymentConsentRequest;
import net.openid.conformance.openbanking_brasil.testmodules.support.OpenBankingBrazilPreAuthorizationErrorAgnosticSteps;
import net.openid.conformance.openbanking_brasil.testmodules.support.OptionallyAllow201Or422;
import net.openid.conformance.openbanking_brasil.testmodules.support.payments.*;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;



@PublishTestModule(
	testName = "payments-api-qrdn-good-proxy-test-legacy",
	displayName = "Payments Consents API test module for QRDN local instrument which expects an ACCC status",
	summary = "The test will use the user provided QRDN fields: Payment consent request JSON with QRDN embedded;Initiators CNPJ for QRDN test;Remittance information for QRDN test, to create the request_body for both the Post Consents and the Post Payments of this test. The Dynamic QRCode must be created by the organisation by using the PIX Tester environment and all the creditor details must be aligned with what is supplied on this field ",
	profile = "hide",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.resourceUrl",
		"resource.brazilCpf",
		"resource.brazilPaymentConsent",
		"resource.brazilPixPayment",
		"resource.brazilQrdnPaymentConsent",
		"resource.brazilQrdnCnpj",
		"resource.brazilQrdnRemittance"
	}
)
public class PaymentsConsentsApiQRDNHappyLegacyTestModule extends AbstractOBBrasilQrCodePaymentFunctionalTestModule {

	@Override
	protected ConditionSequence createOBBPreauthSteps() {
		eventLog.log(getName(), "Payments scope present - protected resource assumed to be a payments endpoint");
		ConditionSequence steps = new OpenBankingBrazilPreAuthorizationErrorAgnosticSteps(addTokenEndpointClientAuthentication)
			.replace(FAPIBrazilCreatePaymentConsentRequest.class, condition(SelectPaymentConsentWithQrdnCode.class))
			.replace(OptionallyAllow201Or422.class, condition(EnsureConsentResponseCodeWas201.class));
		return steps;
	}

	@Override
	protected void postProcessResourceSequence(ConditionSequence pixSequence) {
		pixSequence.replace(CreatePaymentRequestEntityClaims.class, condition(CreatePaymentRequestEntityClaimsFromQrdnConfig.class));
	}

	@Override
	protected void configureDictInfo() {
		callAndStopOnFailure(SelectQRDNCodeLocalInstrumentWithQrdnConfig.class);
		callAndStopOnFailure(SelectQRDNCodePixLocalInstrument.class);
		callAndStopOnFailure(ValidateQrdnConfig.class);
	}
}
