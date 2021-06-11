package net.openid.conformance.condition.client;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.testmodule.Environment;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;
import org.openqa.selenium.json.Json;

import java.util.Arrays;

public class CreateBrazilConsentRequest extends AbstractCondition {

	@Override
	@PreEnvironment()
	@PostEnvironment(required = "consent_endpoint_request")
	public Environment evaluate(Environment env) {

		String cpf = env.getString("config", "resource.brazilCpf");
		if (Strings.isNullOrEmpty(cpf)) {
			throw error("CPF value missing from test configuration");
		}

		// see https://openbanking-brasil.github.io/areadesenvolvedor/#direitos-creditorios-descontados-parcelas-do-contrato

		JsonObject consentRequest = new ConsentRequest(cpf, "ACCOUNTS_READ").getPayload();

		env.putObject("consent_endpoint_request", consentRequest);

		logSuccess(args("consent_endpoint_request", consentRequest));

		return env;
	}

	private static class ConsentRequest {

		private JsonObject payload;
		ConsentRequest(String cpf, String...permissions) {
			JsonObject data = new JsonObject();
			JsonArray permissionsArray = new JsonArray();
			Arrays.stream(permissions).forEach(p -> permissionsArray.add(p));
			data.add("permissions", permissionsArray);
			JsonObject loggedUser = new JsonObject();
			JsonObject document = new JsonObject();
			document.addProperty("identification", cpf);
			document.addProperty("rel", "CPF");
			loggedUser.add("document", document);
			data.add("loggedUser", loggedUser);
			payload = new JsonObject();
			payload.add("data", data);
		}

		JsonObject getPayload() {
			return payload;
		}

	}

}
