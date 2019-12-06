package net.openid.conformance.condition.client;

import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

public class AddAddressScopeToAuthorizationEndpointRequest extends AbstractAddScopeToAuthorizationEndpointRequest {

	@Override
	@PreEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		return addScopeToAuthorizationEndpointRequest(env, "address");
	}

}
