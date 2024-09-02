package net.openid.conformance.condition.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

public class SetAuthorizationEndpointRequestResponseMode extends AbstractCondition {

	@Override
	@PreEnvironment(required = "authorization_endpoint_request", strings = "response_mode")
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		String responseMode = env.getString("response_mode");
		if (Strings.isNullOrEmpty(responseMode)) {
			throw error("No response_mode found in environment");
		}
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");

		authorizationEndpointRequest.addProperty("response_mode", responseMode);

		log("Added response_mode parameter to request", authorizationEndpointRequest);

		return env;
	}

}
