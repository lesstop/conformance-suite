package io.fintechlabs.testframework.condition.client;

import java.util.Arrays;

import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.testmodule.Environment;

public class FAPIOBCheckDiscEndpointGrantTypesSupported extends ValidateJsonArray {

	private static final String environmentVariable = "grant_types_supported";

	private static final String[] SET_VALUES = new String[] { "authorization_code", "client_credentials" };
	private static final int minimumMatchesRequired = 2;

	private static final String errorMessageNotEnough = "The server does not support enough of the required grant types.";

	@Override
	@PreEnvironment(required = "server")
	public Environment evaluate(Environment env) {

		return validate(env, environmentVariable, Arrays.asList(SET_VALUES), minimumMatchesRequired,
				errorMessageNotEnough);

	}

}
