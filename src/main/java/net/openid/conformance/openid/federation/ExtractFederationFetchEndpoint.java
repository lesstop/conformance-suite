package net.openid.conformance.openid.federation;

import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;

public class ExtractFederationFetchEndpoint extends AbstractCondition {

	@Override
	@PreEnvironment(required = { "federation_response_body" })
	@PostEnvironment(strings = "federation_endpoint_url")
	public Environment evaluate(Environment env) {

		String fetchEndpoint = OIDFJSON.getString(env.getElementFromObject("federation_response_body", "metadata.federation_entity.federation_fetch_endpoint"));
		env.putString("federation_endpoint_url", fetchEndpoint);

		logSuccess("Extracted federation fetch endpoint", args("federation_endpoint_url", fetchEndpoint));

		return env;
	}

}
