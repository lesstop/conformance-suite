package net.openid.conformance.openbanking_brasil.testmodules.account.testmodule.v2;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.client.CallProtectedResource;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.testmodules.AbstractOBBrasilFunctionalTestModule;
import net.openid.conformance.openbanking_brasil.testmodules.account.BuildAccountsConfigResourceUrlFromConsentUrl;
import net.openid.conformance.openbanking_brasil.testmodules.account.PrepareAllAccountRelatedConsentsForHappyPathTest;
import net.openid.conformance.openbanking_brasil.testmodules.support.*;
import net.openid.conformance.testmodule.PublishTestModule;
import net.openid.conformance.variant.FAPI1FinalOPProfile;
import net.openid.conformance.variant.VariantHidesConfigurationFields;

@PublishTestModule(
	testName = "account-api-page-size-too-big-test-v2",
	displayName = "Validate that a request for Accounts with page-size > 1000 returns HTTP 422",
	summary = "Validates the structure of all account API resources V2\n" +
		"\u2022 Creates a Consent with the complete set of the accounts permission group (\"ACCOUNTS_READ\", \"ACCOUNTS_TRANSACTIONS_READ\", \"RESOURCES_READ\")\n" +
		"\u2022 Expects a success 201 - Expects a success on Redirect as well \n" +
		"\u2022 Calls GET Accounts API V2\n" +
		"\u2022 Expects a 200 response \n" +
		"\u2022 Calls GET Accounts API V2 with page size=1001\n" +
		"\u2022 Expects a 422 response",
	profile = OBBProfile.OBB_PROFILE,
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.brazilCpf"
	}
)
@VariantHidesConfigurationFields(parameter = FAPI1FinalOPProfile.class, value = "openbanking_brazil", configurationFields = {
	"client.org_jwks"
})
public class AccountsApiPageSizeTooLargeTestModuleV2 extends AbstractOBBrasilFunctionalTestModule {
	@Override
	protected void configureClient() {
		callAndStopOnFailure(BuildAccountsConfigResourceUrlFromConsentUrl.class);
		super.configureClient();
	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {
		callAndStopOnFailure(AddAccountScope.class);
		callAndStopOnFailure(PrepareAllAccountRelatedConsentsForHappyPathTest.class);
	}

	@Override
	protected void validateResponse() {
		callAndStopOnFailure(ClearRequestObjectFromEnvironment.class);
		callAndStopOnFailure(SetProtectedResourceUrlPageSize1001.class);
		callAndStopOnFailure(SetResourceMethodToGet.class);
		callAndStopOnFailure(ClearContentTypeHeaderForResourceEndpointRequest.class);
		callAndStopOnFailure(CallProtectedResource.class);
		callAndStopOnFailure(EnsureResponseCodeWas422.class);

	}

}
