package net.openid.conformance.openbanking_brasil.testmodules.support;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.client.jsonAsserting.AbstractJsonAssertingCondition;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.testmodule.OIDFJSON;

public class EnsureConsentReasonConsentMaxDateReached extends AbstractJsonAssertingCondition {


	@Override
	@PreEnvironment(strings = "resource_endpoint_response")
	public Environment evaluate(Environment env) {

		JsonElement consentResponse = bodyFrom(env);
		if (consentResponse.getAsJsonObject().entrySet().isEmpty()) {
			throw error("Consent response was not found.");
		}
		String expectedResult = "CONSENT_MAX_DATE_REACHED";
		JsonElement rejectionElement = findByPath(consentResponse, "$.data.rejection.reason.code");
		String rejection = OIDFJSON.getString(rejectionElement);
		if(Strings.isNullOrEmpty(rejection)) {
			throw error("Consent Reason was not found.");
		}

		if(!rejection.equals(expectedResult)) {
			throw error(String.format("Expected Reason to be %s but it was not.", expectedResult), args("Reason", rejection));
		}

		 logSuccess(String.format("Reason is %s as expected", expectedResult), args("Reason", expectedResult));

		return env;
	}

}
