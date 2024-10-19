package net.openid.conformance.openid.federation;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

import java.util.HashSet;
import java.util.Set;

public class ValidateEntityStatementMetadata extends AbstractCondition {

	@Override
	@PreEnvironment(required = { "federation_response_body" } )
	public Environment evaluate(Environment env) {

		JsonElement metadataClaim = env.getElementFromObject("federation_response_body", "metadata");
		if (metadataClaim == null) {
			logSuccess("Entity statement does not contain the metadata claim");
			return env;
		}

		JsonObject metadata = metadataClaim.getAsJsonObject();

		Set<String> validEntityTypes = ImmutableSet.of(
			"federation_entity",
			"openid_relying_party",
			"openid_provider",
			"oauth_authorization_server",
			"oauth_client",
			"oauth_resource"
		);

		Set<String> keys = metadata.keySet();
		Set<String> difference = new HashSet<>(keys);
		difference.removeAll(validEntityTypes);
		if (!difference.isEmpty()) {
			throw error("The metadata claim contains invalid entity types", args("expected", validEntityTypes, "actual", keys));
		}

		logSuccess("Entity statement contains a valid metadata claim", args("metadata", metadata));
		return env;
	}

}
