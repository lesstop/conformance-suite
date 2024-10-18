package net.openid.conformance.openid.federation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.Condition;
import net.openid.conformance.condition.client.EnsureContentTypeJson;
import net.openid.conformance.condition.client.EnsureHttpStatusCodeIs200;
import net.openid.conformance.testmodule.AbstractTestModule;
import net.openid.conformance.testmodule.OIDFJSON;
import net.openid.conformance.variant.ClientRegistration;
import net.openid.conformance.variant.ServerMetadata;
import net.openid.conformance.variant.VariantConfigurationFields;
import net.openid.conformance.variant.VariantParameters;

import java.util.ArrayList;
import java.util.List;

import static net.openid.conformance.openid.federation.EntityUtils.appendWellKnown;
import static net.openid.conformance.openid.federation.EntityUtils.stripWellKnown;

@VariantParameters({
	ServerMetadata.class,
	ClientRegistration.class
})
@VariantConfigurationFields(parameter = ServerMetadata.class, value = "static", configurationFields = {
	"federation.entity_configuration"
})
public abstract class AbstractOpenIDFederationTest extends AbstractTestModule {

	@Override
	public void configure(JsonObject config, String baseUrl, String externalUrlOverride, String baseMtlsUrl) {
		env.putString("base_url", baseUrl);
		env.putString("base_mtls_url", baseMtlsUrl);
		env.putObject("config", config);

		callAndStopOnFailure(ValidateEntityIdentifier.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");
		callAndStopOnFailure(ValidateTrustAnchor.class, Condition.ConditionResult.FAILURE, "OIDFED-1.2");

		String entityIdentifier = env.getString("config", "federation.entity_identifier");
		eventLog.startBlock("Fetch Entity Configuration for %s".formatted(entityIdentifier));
		if (ServerMetadata.STATIC.equals(getVariant(ServerMetadata.class))) {
			// This case is perhaps not applicable in the general case,
			// but f ex the leaf entities in the Swedish sandbox federation
			// do not publish their own entity configurations.
			callAndStopOnFailure(GetStaticEntityStatement.class, Condition.ConditionResult.FAILURE);
			callAndStopOnFailure(ExtractEntityStatementUrlFromConfig.class, Condition.ConditionResult.FAILURE);
			callAndStopOnFailure(SetPrimaryEntityStatement.class, Condition.ConditionResult.FAILURE);
		} else {
			callAndStopOnFailure(ExtractEntityStatementUrlFromConfig.class, Condition.ConditionResult.FAILURE);
			callAndStopOnFailure(CallFederationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
			callAndStopOnFailure(SetPrimaryEntityStatement.class, Condition.ConditionResult.FAILURE);
			validateEntityStatementResponse();
		}
		eventLog.endBlock();

		setStatus(Status.CONFIGURED);
		fireSetupDone();
	}

	protected void validateEntityStatement() {
		String entityStatementUrl = env.getString("federation_endpoint_url");

		eventLog.startBlock("Validate basic claims in Entity Statement for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractBasicClaimsFromEntityStatement.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		env.putString("expected_iss", stripWellKnown(entityStatementUrl));
		env.putString("expected_sub", stripWellKnown(entityStatementUrl));
		call(sequence(ValidateEntityStatementBasicClaimsSequence.class));
		eventLog.endBlock();

		eventLog.startBlock("Validate JWKs and signature in Entity Statement for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractJWKsFromEntityStatement.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		call(sequence(ValidateEntityStatementSignatureSequence.class));
		eventLog.endBlock();

		eventLog.startBlock("Validate metadata in Entity Statement for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateEntityStatementMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");
		eventLog.endBlock();

		eventLog.startBlock("Validate Federation Entity metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateFederationEntityMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");
		eventLog.endBlock();

		eventLog.startBlock("Validate OpenID Connect Relying Party metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractOpenIDConnectRelyingPartyMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.2");
		validateOpenIDRelyingPartyMetadata();
		eventLog.endBlock();

		eventLog.startBlock("Validate OpenID Connect OpenID Provider metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ExtractOpenIDProviderMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
		validateOpenIdProviderMetadata();
		eventLog.endBlock();

		eventLog.startBlock("Validate OAuth Authorization Server metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateOAuthAuthorizationServerMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.4");
		eventLog.endBlock();

		eventLog.startBlock("Validate OAuth Client metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateOAuthClientMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.5");
		eventLog.endBlock();

		eventLog.startBlock("Validate OAuth Protected Resource metadata for %s".formatted(entityStatementUrl));
		callAndContinueOnFailure(ValidateOAuthProtectedResourceMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.6");
		eventLog.endBlock();
	}

	protected void validateEntityStatementResponse() {
		env.mapKey("endpoint_response", "federation_http_response");
		call(sequence(ValidateEntityStatementResponseSequence.class));
		env.unmapKey("endpoint_response");
	}

	protected void validateOpenIDRelyingPartyMetadata() {
		if (env.containsObject("openid_relying_party_metadata")) {
			env.mapKey("client", "openid_relying_party_metadata");
			call(sequence(ValidateOpenIDRelyingPartyMetadataSequence.class));
			callAndContinueOnFailure(ValidateClientRegistrationTypes.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.2");
			env.unmapKey("client");
			env.removeObject("openid_relying_party_metadata");
		}
	}

	protected void validateOpenIdProviderMetadata() {
		if (env.containsObject("openid_provider_metadata")) {
			env.mapKey("server", "openid_provider_metadata");
			call(new ValidateDiscoveryMetadataSequence(getVariant(ClientRegistration.class)));
			callAndContinueOnFailure(ValidateClientRegistrationTypesSupported.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "client_registration_types_supported", Condition.ConditionResult.INFO,
				ValidateFederationRegistrationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "client_registration_types_supported", Condition.ConditionResult.INFO,
				ValidateRequestAuthenticationMethodsSupported.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			skipIfElementMissing("openid_provider_metadata", "request_authentication_methods_supported", Condition.ConditionResult.INFO,
				ValidateRequestAuthenticationSigningAlgValuesSupported.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.3");
			env.unmapKey("server");
			env.removeObject("openid_provider_metadata");
		}
	}

	protected void validateAbsenceOfMetadataPolicy() {
		String entity = env.getString("federation_endpoint_url");
		eventLog.startBlock("Validate that Entity Statement for %s does not have a metadata_policy".formatted(entity));
		callAndContinueOnFailure(ValidateAbsenceOfMetadataPolicy.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		eventLog.endBlock();
	}

	protected void validateImmediateSuperiors() {
		String entity = env.getString("federation_endpoint_url");
		String anchor = env.getString("config", "federation.trust_anchor");
		// authority_hints is REQUIRED in Entity Configurations of the Entities that have at least one Superior above them,
		// such as Leaf and Intermediate Entities. This claim MUST NOT be present in Entity Configurations of Trust Anchors with no Superiors.
		if (!entity.startsWith(anchor)) {
			eventLog.startBlock("Validate authority hints in Entity Statement for %s".formatted(entity));
			callAndContinueOnFailure(ValidateAuthorityHints.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		} else {
			eventLog.startBlock("Validate absence of authority hints in Entity Statement for configured trust anchor %s".formatted(entity));
			callAndContinueOnFailure(ValidateAbsenceOfAuthorityHints.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
		}
		validateSubordinateStatements();
		eventLog.endBlock();
	}

	protected void validateSubordinateStatements() {
		JsonElement authorityHintsElement = env.getElementFromObject("federation_response_body", "authority_hints");
		if (authorityHintsElement != null) {
			JsonArray authorityHints = authorityHintsElement.getAsJsonArray();
			for (JsonElement authorityHintElement : authorityHints) {
				String authorityHint = OIDFJSON.getString(authorityHintElement);
				String authorityHintUrl = appendWellKnown(authorityHint);

				// Get the entity statement for the superior
				env.putString("federation_endpoint_url", authorityHintUrl);
				callAndStopOnFailure(CallFederationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
				validateEntityStatementResponse();
				validateEntityStatement();

				eventLog.startBlock("Validating subordinate statement by immediate superior %s".formatted(authorityHint));

				// Verify that the primary entity is present in the list endpoint result
				callAndContinueOnFailure(ExtractFederationListEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");
				callAndContinueOnFailure(CallListEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-8.2.1");

				env.mapKey("endpoint_response", "federation_list_endpoint_response");
				callAndContinueOnFailure(EnsureHttpStatusCodeIs200.class, Condition.ConditionResult.FAILURE, "OIDFED-8.2.2");
				callAndContinueOnFailure(EnsureContentTypeJson.class, Condition.ConditionResult.FAILURE, "OIDFED-8.2.2");
				env.unmapKey("endpoint_response");

				callAndContinueOnFailure(VerifyPrimaryEntityPresenceInSubordinateListing.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1");

				// Get the entity statement from the Superior's fetch endpoint
				env.putString("expected_sub", env.getString("primary_entity_statement_iss"));
				callAndContinueOnFailure(ExtractFederationFetchEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
				callAndContinueOnFailure(AppendSubToFederationEndpointUrl.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");
				callAndStopOnFailure(CallFederationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.1");

				env.mapKey("endpoint_response", "federation_http_response");
				callAndContinueOnFailure(EnsureHttpStatusCodeIs200.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.2");
				callAndContinueOnFailure(EnsureContentTypeEntityStatementJwt.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1.2");
				env.unmapKey("endpoint_response");

				call(sequence(ValidateEntityStatementSignatureSequence.class));

				callAndContinueOnFailure(ExtractBasicClaimsFromEntityStatement.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
				call(sequence(ValidateEntityStatementBasicClaimsSequence.class));

				callAndContinueOnFailure(ValidateEntityStatementMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");
				// No authority hints in subordinate statements
				callAndContinueOnFailure(ValidateAbsenceOfAuthorityHints.class, Condition.ConditionResult.FAILURE, "OIDFED-3");
				// No federation_entity metadata in subordinate statements
				callAndContinueOnFailure(ValidateAbsenceOfFederationEntityMetadata.class, Condition.ConditionResult.FAILURE, "OIDFED-8.1");
				// Only Subordinate Statements may include this claim.
				callAndContinueOnFailure(ValidateEntityStatementMetadataPolicy.class, Condition.ConditionResult.FAILURE, "OIDFED-5.1.1");

				eventLog.endBlock();
			}
		}
	}

	protected List<String> findPath(String fromEntity, String toTrustAnchor) throws CyclicPathException {
		List<String> path = findPath(fromEntity, toTrustAnchor, new ArrayList<>());
		eventLog.log(getName(), "Path to trust anchor: %s".formatted(String.join(" → ", path)));
		return path;
	}

	protected List<String> findPath(String fromEntity, String toTrustAnchor, List<String> path) throws CyclicPathException {

		if (path.isEmpty()) {
			env.mapKey("federation_response_body", "primary_entity_statement_body");
		} else {
			env.unmapKey("federation_response_body");
			String currentWellKnownUrl = appendWellKnown(fromEntity);
			env.putString("federation_endpoint_url", currentWellKnownUrl);
			callAndStopOnFailure(CallFederationEndpoint.class, Condition.ConditionResult.FAILURE, "OIDFED-9");
		}

		if (path.contains(fromEntity)) {
			throw new CyclicPathException("Cyclic path detected. Entity %s already exists in the path: %s".formatted(fromEntity, String.join(" → ", path)));
		}

		path.add(fromEntity);

		if (EntityUtils.equals(fromEntity, toTrustAnchor)) {
			return path;
		}

		JsonElement authorityHintsElement = env.getElementFromObject("federation_response_body", "authority_hints");
		if (authorityHintsElement == null) {
			return null;
		}
		JsonArray authorityHints = authorityHintsElement.getAsJsonArray();
		if (authorityHints.isJsonNull() || authorityHints.isEmpty()) {
			return null;
		}

		for (JsonElement authorityHintElement : authorityHints) {
			String authorityHint = OIDFJSON.getString(authorityHintElement);
			List<String> result = findPath(authorityHint, toTrustAnchor, new ArrayList<>(path));
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	public static class CyclicPathException extends Exception {

		private static final long serialVersionUID = 1L;

		public CyclicPathException(String message) {
			super(message);
		}

	}

}
