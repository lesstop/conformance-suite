package net.openid.conformance.openbanking_brasil.testmodules;

import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.*;
import net.openid.conformance.openbanking_brasil.OBBProfile;
import net.openid.conformance.openbanking_brasil.resourcesAPI.EnumResourcesStatus;
import net.openid.conformance.openbanking_brasil.resourcesAPI.EnumResourcesType;
import net.openid.conformance.openbanking_brasil.resourcesAPI.PrepareUrlForResourcesCall;
import net.openid.conformance.openbanking_brasil.resourcesAPI.ResourcesResponseValidator;
import net.openid.conformance.openbanking_brasil.testmodules.support.*;
import net.openid.conformance.sequence.ConditionSequence;
import net.openid.conformance.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "accounts-resources-test-multiple-consents",
	displayName = "Validates that the server has correctly implemented the rules set for joint accounts that require multiple consents for data to be shared.",
	summary =
		"\u2022 Create a CONSENT with only ACCOUNTS_READ, ACCOUNTS_BALANCES_READ and RESOURCES_READ Permissions using the CPF and CNPJ provided for joint accounts\n" +
		"\u2022 Expect a Success 201\n" +
		"\u2022 Redirect the user to authorize the Consent - Redirect URI must contain accounts, resources and consents scopes\n" +
		"\u2022 Expect a Successful authorization with an authorization code created\n" +
		"\u2022 Call the RESOURCES API with the authorized consent\n" +
		"\u2022 Expect a 200 - Validate that one Account Resource has been returned and it is on the state AWAITING_AUTHORIZATION\n" +
		"\u2022 Call the ACCOUNTS API\n" +
		"\u2022 Expect a 200 - Make sure the Server returns a 200 with an empty list on the object\n" +
		"\u2022 Call the ACCOUNTS BALANCES API with the Account ID of the Account on AWAITING_AUTHORIZATION\n" +
		"\u2022 Expect a 403 - Validate that the field response.errors.code is STATUS_RESOURCE_AWAITING_AUTHORIZATION\n" +
		"\u2022 POLL the GET RESOURCES API for 5 minutes, one call every 30 seconds.\n" +
		"\u2022 Continue Polling until the Account Resource returned is on the status AVAILABLE\n" +
		"\u2022 Call the ACCOUNTS API\n" +
		"\u2022 Expect a 200 - Make sure the Account Resource is now returned on the API response\n",
	profile = OBBProfile.OBB_PROFIlE_PHASE2,
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.jwks",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.consentUrl",
		"resource.resourceUrl",
		"resource.brazilCpfJointAccount",
		"resource.brazilCnpjJointAccount",
		"consent.productType"
	}
)
public class AccountsApiResourcesMultipleConsentsTestModule extends AbstractOBBrasilFunctionalTestModule{

	private static final String RESOURCE_TYPE = EnumResourcesType.ACCOUNT.name();
	private static final String RESOURCE_STATUS = EnumResourcesStatus.PENDING_AUTHORISATION.name();

	@Override
	protected void configureClient() {
		super.configureClient();
		env.putBoolean("continue_test", true);
		callAndContinueOnFailure(EnsureJointAccountCpfOrCnpjIsPresent.class, Condition.ConditionResult.WARNING);
		if(!env.getBoolean("continue_test")){
			fireTestFinished();
		}
	}

	@Override
	protected void requestProtectedResource() {
		// Call Resources API
		callAndStopOnFailure(PrepareUrlForResourcesCall.class);
		runInBlock("Call Resources API", () -> call(getPreCallProtectedResourceSequence()));

		runInBlock("Validate Resources response", () -> {
			callAndStopOnFailure(ResourcesResponseValidator.class, Condition.ConditionResult.FAILURE);
			callAndStopOnFailure(EnsureResponseHasLinks.class);
			callAndStopOnFailure(EnsureOnlyOneResourceWasReturned.class);

			env.putString("resource_type", RESOURCE_TYPE);
			env.putString("resource_status", RESOURCE_STATUS);
			callAndStopOnFailure(ExtractResourceIdOfResourcesWithSpecifiedTypeAndStatus.class);
		});

		env.putString("protected_resource_url", env.getString("base_resource_url"));
		super.requestProtectedResource(); // Call Accounts API
	}

	@Override
	protected void validateResponse() {
		// Accounts Validation
		callAndStopOnFailure(EnsureAccountListIsEmpty.class);
		callAndStopOnFailure(EnsureResponseHasLinks.class);

		// Call ACCOUNTS BALANCES API
		callAndStopOnFailure(PrepareUrlForFetchingAccountBalances.class);
		runInBlock("Call Accounts Balances API", () -> call(getPreCallProtectedResourceSequence()
			.replace(EnsureResponseCodeWas200.class, condition(EnsureResponseCodeWas403.class))));
		runInBlock("Validate Accounts Balances response", () -> {
			callAndStopOnFailure(ResourceErrorMetaValidator.class);

		});



	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {
		callAndStopOnFailure(AddAccountScope.class);
		callAndStopOnFailure(AddResourcesScope.class);
		callAndStopOnFailure(PrepareResourceAccountBalancesReadOnlyConsentPermissions.class);
	}

	protected ConditionSequence getPreCallProtectedResourceSequence() {
		return sequenceOf(
			condition(CreateEmptyResourceEndpointRequestHeaders.class),
			condition(AddFAPIAuthDateToResourceEndpointRequest.class),
			condition(AddIpV4FapiCustomerIpAddressToResourceEndpointRequest.class),
			condition(CreateRandomFAPIInteractionId.class),
			condition(AddFAPIInteractionIdToResourceEndpointRequest.class),
			condition(CallProtectedResource.class),
			condition(EnsureResponseCodeWas200.class),
			condition(CheckForDateHeaderInResourceResponse.class),
			condition(CheckForFAPIInteractionIdInResourceResponse.class),
			condition(EnsureResourceResponseReturnedJsonContentType.class)
		);
	}
}
