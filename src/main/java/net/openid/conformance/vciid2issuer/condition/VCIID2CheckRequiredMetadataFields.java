package net.openid.conformance.vciid2issuer.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

import java.util.LinkedHashSet;
import java.util.Set;

public class VCIID2CheckRequiredMetadataFields extends AbstractCondition {

	protected Set<String> getRequiredFieldNames() {
		return Set.of("credential_issuer", "credential_endpoint", "credential_configurations_supported");
	}

	@PreEnvironment(required = {"vci"})
	@Override
	public Environment evaluate(Environment env) {

		JsonElement credentialIssuerMetadataEl = env.getElementFromObject("vci", "credential_issuer_metadata");
		JsonObject credentialIssuerMetadata = credentialIssuerMetadataEl.getAsJsonObject();

		Set<String> missingRequiredFields = new LinkedHashSet<>();
		Set<String> requiredFieldNames = getRequiredFieldNames();
		for (String requiredField : requiredFieldNames) {
			if (!credentialIssuerMetadata.has(requiredField)) {
				missingRequiredFields.add(requiredField);
				continue;
			}
			log("Found required field " + requiredField, args(requiredField, credentialIssuerMetadata.get(requiredField)));
		}

		if (!missingRequiredFields.isEmpty()) {
			throw error("Missing required fields ", args("required_fields_missing", missingRequiredFields));
		}

		logSuccess("Found required fields ", args("required_fields", requiredFieldNames));

		return env;
	}
}
