package net.openid.conformance.condition.client;

import com.google.gson.*;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.JsonUtils;

import java.lang.reflect.Type;

public class FAPIBrazilCreateBadCurrenyPaymentConsentRequest extends AbstractCondition {
	@Override
	@PreEnvironment(required = "config" )
	@PostEnvironment(required = "consent_endpoint_request")
	public Environment evaluate(Environment env) {
		JsonElement paymentConsent = env.getElementFromObject("resource", "brazilPaymentConsent");
		if(paymentConsent == null) {
			throw error("As 'payments' is included in the 'scope' within the test configuration, a payment consent request JSON object must also be provided in the test configuration.");
		}
		JsonObject paymentRequestObject = paymentConsent.getAsJsonObject();
		log(paymentRequestObject);
		Gson GSON = JsonUtils.createBigDecimalAwareGson();

		JsonObject details = GSON.fromJson(
			"{    \"consentId\": \"urn:banco-santander:ebb41433-7fe6-49db-826d-850acd3c452c\",\n" +
				"    \"creationDateTime\": \"2021-10-06T13:16:53Z\",\n" +
				"    \"expirationDateTime\": \"2021-10-06T13:21:53Z\",\n" +
				"    \"statusUpdateDateTime\": \"2021-10-06T13:16:53Z\",\n" +
				"    \"status\": \"AWAITING_AUTHORISATION\",\n" +
				"    \"loggedUser\": {\n" +
				"      \"document\": {\n" +
				"        \"identification\": \"59788323073\",\n" +
				"        \"rel\": \"CPF\"\n" +
				"      }\n" +
				"    },\n" +
				"    \"businessEntity\": {\n" +
				"      \"document\": {\n" +
				"        \"identification\": \"04231407000180\",\n" +
				"        \"rel\": \"CNPJ\"\n" +
				"      }\n" +
				"    },\n" +
				"    \"creditor\": {\n" +
				"      \"personType\": \"PESSOA_JURIDICA\",\n" +
				"      \"cpfCnpj\": \"24900081000144\",\n" +
				"      \"name\": \"MARCELO FRIAS\"\n" +
				"    },\n" +
				"    \"payment\": {\n" +
				"      \"type\": \"PIX\",\n" +
				"      \"date\": \"2021-10-05\",\n" +
				"      \"currency\": \"BRL\",\n" +
				"      \"amount\": \"1.05\",\n" +
				"      \"details\": {\n" +
				"        \"localInstrument\": \"MANU\",\n" +
				"        \"creditorAccount\": {\n" +
				"          \"ispb\": \"60746948\",\n" +
				"          \"issuer\": \"3861\",\n" +
				"          \"number\": \"3980\",\n" +
				"          \"accountType\": \"CACC\"\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  }", JsonObject.class);
		paymentRequestObject = new JsonObject();
		paymentRequestObject.add("data", details);
		log(paymentRequestObject);
		validate(paymentRequestObject);
		env.putObject("consent_endpoint_request", paymentRequestObject);

		logSuccess(args("consent_endpoint_request", paymentConsent));
		return env;
	}

	private void validate(JsonObject consentConfig) {
		JsonElement element = validate("data", consentConfig);
		validate("loggedUser", element.getAsJsonObject());
		validate("creditor", element.getAsJsonObject());
		validate("payment", element.getAsJsonObject());
	}

	private JsonElement validate(String element, JsonObject object) {
		if(!object.has(element)) {
			throw error("Consent object must have " + element + " field");
		}
		return object.get(element);
	}
}
