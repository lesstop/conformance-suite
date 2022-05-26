package net.openid.conformance.openbanking_brasil.testmodules.support;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;

public class RemoveConsentScope extends AbstractCondition {

	@Override
	@PreEnvironment(required = "client")
	@PostEnvironment(required = "client")
	public Environment evaluate(Environment env) {

		JsonObject client = env.getObject("client");

		JsonElement scopeElement = client.get("scope");
		if (scopeElement == null) {
			throw error("scope missing in client object");
		}

		String scope = OIDFJSON.getString(scopeElement);
		if (Strings.isNullOrEmpty(scope)) {
			throw error("scope empty in client object");
		}

		if(!scope.contains("consents")) {
			throw error("consents is not in scope");
		}
		scope = scope.replace("consents ", "");
		client.addProperty("scope", scope);

		logSuccess("Removed scope from client's scope", args("scope_removed", scope));
		return env;
	}
}
