package io.fintechlabs.testframework.condition.client;

import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.testmodule.Environment;

public class CheckDiscEndpointTokenEndpoint extends ValidateJsonUri {

	private static final String environmentVariable = "token_endpoint";

	@Override
	@PreEnvironment(required = "server")
	public Environment evaluate(Environment env) {

		return validate(env, environmentVariable);

	}
}
