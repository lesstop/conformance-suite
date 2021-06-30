package net.openid.conformance.condition.client;

import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;

import java.util.Arrays;

public class FAPIBrazilCheckDiscEndpointCpfOrCnpjClaimSupported extends AbstractValidateJsonArray {

	private static final String environmentVariable = "claims_supported";

	private static final String[] SET_VALUES = new String[] { "cpf", "cnpj" };
	private static final int minimumMatchesRequired = 1;

	private static final String errorMessageNotEnough = "The server must support at least one of the 'cpf' or 'cnpj' claims.";

	@Override
	@PreEnvironment(required = "server")
	public Environment evaluate(Environment env) {

		return validate(env, environmentVariable, Arrays.asList(SET_VALUES), minimumMatchesRequired,
				errorMessageNotEnough);
	}


}
