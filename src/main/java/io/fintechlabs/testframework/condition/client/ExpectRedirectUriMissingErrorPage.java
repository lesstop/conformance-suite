package io.fintechlabs.testframework.condition.client;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.testmodule.Environment;

public class ExpectRedirectUriMissingErrorPage extends AbstractCondition {

	@Override
	@PostEnvironment(strings = "redirect_uri_missing_error")
	public Environment evaluate(Environment env) {

		String placeholder = createBrowserInteractionPlaceholder("Show redirect URI error page");
		env.putString("redirect_uri_missing_error", placeholder);

		return env;
	}

}
